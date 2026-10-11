import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public boolean existeNombreUsuario(String nombreUsuario) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE nombre_usuario = ? COLLATE NOCASE LIMIT 1";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, nombreUsuario);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }
        }
    }

    public boolean registrarUsuario(Usuario usuario) throws SQLException {
        if (usuario == null
        || usuario.getNombre() == null
        || usuario.getNombre().trim().isEmpty()
        || usuario.getNombreUsuario() == null
        || usuario.getNombreUsuario().trim().isEmpty()
        || usuario.getContrasenaHash() == null
        || usuario.getContrasenaHash().trim().isEmpty()) {
    return false;
}
        String sql = "INSERT INTO usuarios (nombre, nombre_usuario, contrasena_hash) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getNombreUsuario());
            statement.setString(3, usuario.getContrasenaHash());
            return statement.executeUpdate() == 1;
        }
    }

    public Usuario buscarPorNombreUsuario(String nombreUsuario) throws SQLException {
        String sql = "SELECT id, nombre, nombre_usuario, contrasena_hash "
                + "FROM usuarios WHERE nombre_usuario = ? COLLATE NOCASE";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, nombreUsuario);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return new Usuario(
                            resultado.getInt("id"),
                            resultado.getString("nombre"),
                            resultado.getString("nombre_usuario"),
                            resultado.getString("contrasena_hash")
                    );
                }
            }
        }
        return null;
    }
}
