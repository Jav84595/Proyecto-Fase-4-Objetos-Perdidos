import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DevolucionDAO {

    public DevolucionDAO() {
        crearTabla();
    }

    private void crearTabla() {
        String sql =
                "CREATE TABLE IF NOT EXISTS devoluciones ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "objeto_id INTEGER NOT NULL UNIQUE, "
                + "nombre_propietario TEXT NOT NULL, "
                + "informacion_verificacion TEXT NOT NULL, "
                + "fecha_devolucion TEXT NOT NULL, "
                + "FOREIGN KEY (objeto_id) REFERENCES objetos(id)"
                + ");";

        try (Connection conexion = ConexionBD.obtenerConexion();
             Statement statement = conexion.createStatement()) {
            statement.execute(sql);
            boolean existe = false;
            try (ResultSet columnas = statement.executeQuery("PRAGMA table_info(devoluciones)")) {
                while (columnas.next()) {
                    if ("usuario_responsable".equalsIgnoreCase(columnas.getString("name"))) {
                        existe = true;
                        break;
                    }
                }
            }
            if (!existe) {
                statement.execute("ALTER TABLE devoluciones ADD COLUMN usuario_responsable TEXT");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo crear la tabla de devoluciones.", e);
        }
    }

    public boolean registrarDevolucion(Devolucion devolucion) {
        // La capa de persistencia tambien valida sus entradas: puede invocarse
        // desde otros controladores o pruebas sin pasar por la interfaz.
        if (devolucion == null || devolucion.getObjetoId() <= 0
                || devolucion.getNombrePropietario() == null
                || devolucion.getNombrePropietario().trim().isEmpty()
                || devolucion.getInformacionVerificacion() == null
                || devolucion.getInformacionVerificacion().trim().isEmpty()
                || devolucion.getUsuarioResponsable() == null
                || devolucion.getUsuarioResponsable().trim().isEmpty()
                || devolucion.getFechaDevolucion() == null
                || devolucion.getFechaDevolucion().isAfter(LocalDate.now())) {
            return false;
        }
        String consultarObjeto = "SELECT estado FROM objetos WHERE id = ?";
        String insertarDevolucion =
                "INSERT INTO devoluciones "
                + "(objeto_id, nombre_propietario, informacion_verificacion, fecha_devolucion, usuario_responsable) "
                + "VALUES (?, ?, ?, ?, ?)";
        String actualizarObjeto =
                "UPDATE objetos SET estado = ? WHERE id = ? AND estado = ?";

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement consulta = conexion.prepareStatement(consultarObjeto)) {
                    consulta.setInt(1, devolucion.getObjetoId());
                    try (ResultSet resultado = consulta.executeQuery()) {
                        if (!resultado.next()
                                || !Objeto.ESTADO_DISPONIBLE.equals(resultado.getString("estado"))) {
                            conexion.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement insercion = conexion.prepareStatement(insertarDevolucion)) {
                    insercion.setInt(1, devolucion.getObjetoId());
                    insercion.setString(2, devolucion.getNombrePropietario().trim());
                    insercion.setString(3, devolucion.getInformacionVerificacion().trim());
                    insercion.setString(4, devolucion.getFechaDevolucion().toString());
                    insercion.setString(5, devolucion.getUsuarioResponsable().trim());
                    if (insercion.executeUpdate() != 1) {
                        conexion.rollback();
                        return false;
                    }
                }

                try (PreparedStatement actualizacion = conexion.prepareStatement(actualizarObjeto)) {
                    actualizacion.setString(1, Objeto.ESTADO_DEVUELTO);
                    actualizacion.setInt(2, devolucion.getObjetoId());
                    actualizacion.setString(3, Objeto.ESTADO_DISPONIBLE);
                    if (actualizacion.executeUpdate() != 1) {
                        conexion.rollback();
                        return false;
                    }
                }

                conexion.commit();
                return true;
            } catch (SQLException e) {
                conexion.rollback();
                System.err.println("Error al registrar devolución: " + e.getMessage());
                return false;
            } finally {
                try {
                    conexion.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("No se pudo restaurar autoCommit: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de conexión al registrar devolución: " + e.getMessage());
            return false;
        }
    }

    public List<Devolucion> consultarTodas() {
        List<Devolucion> devoluciones = new ArrayList<>();
        String sql =
                "SELECT id, objeto_id, nombre_propietario, informacion_verificacion, fecha_devolucion, usuario_responsable "
                + "FROM devoluciones ORDER BY id";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                devoluciones.add(crearDevolucion(resultado));
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar devoluciones: " + e.getMessage());
        }
        return devoluciones;
    }

    public Devolucion buscarPorId(int id) {
        if (id <= 0) return null;
        String sql =
                "SELECT id, objeto_id, nombre_propietario, informacion_verificacion, fecha_devolucion, usuario_responsable "
                + "FROM devoluciones WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return crearDevolucion(resultado);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar devolución por ID: " + e.getMessage());
        }
        return null;
    }

    private Devolucion crearDevolucion(ResultSet resultado) throws SQLException {
        return new Devolucion(
                resultado.getInt("id"),
                resultado.getInt("objeto_id"),
                resultado.getString("nombre_propietario"),
                resultado.getString("informacion_verificacion"),
                LocalDate.parse(resultado.getString("fecha_devolucion")),
                resultado.getString("usuario_responsable")
        );
    }
}
