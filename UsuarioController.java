import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class UsuarioController {
    private final UsuarioDAO usuarioDAO;
    private final RegistroUsuarioView registroView;
    private final LoginView loginView;
    private Usuario usuarioActivo;

    public UsuarioController(
            UsuarioDAO usuarioDAO,
            RegistroUsuarioView registroView,
            LoginView loginView) {
        this.usuarioDAO = usuarioDAO;
        this.registroView = registroView;
        this.loginView = loginView;

        registroView.agregarRegistroListener(e -> registrarDesdeVista());
        loginView.agregarLoginListener(e -> iniciarSesionDesdeVista());
        loginView.agregarCerrarSesionListener(e -> cerrarSesionDesdeVista());
    }

    public boolean registrarUsuario(String nombre, String nombreUsuario, String contrasena) {
        if (!datosRegistroValidos(nombre, nombreUsuario, contrasena)) {
            return false;
        }

        String nombreLimpio = nombre.trim();
        String usuarioLimpio = nombreUsuario.trim();

        try {
            if (usuarioDAO.existeNombreUsuario(usuarioLimpio)) {
                return false;
            }

            Usuario usuario = new Usuario(
                    nombreLimpio,
                    usuarioLimpio,
                    generarHash(contrasena)
            );
            return usuarioDAO.registrarUsuario(usuario);
        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean iniciarSesion(String nombreUsuario, String contrasena) {
        if (haySesionActiva()){
            return false;
        }
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()
                || contrasena == null || contrasena.isEmpty()) {
            return false;
        }

        try {
            Usuario usuario = usuarioDAO.buscarPorNombreUsuario(nombreUsuario.trim());
            if (usuario == null) {
                return false;
            }

            if (usuario.getContrasenaHash().equals(generarHash(contrasena))) {
                usuarioActivo = usuario;
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al iniciar sesión: " + e.getMessage());
        }
        return false;
    }

    public void cerrarSesion() {
        usuarioActivo = null;
    }

    public boolean haySesionActiva() {
        return usuarioActivo != null;
    }

    public Usuario getUsuarioActivo() {
        return usuarioActivo;
    }

    private boolean datosRegistroValidos(String nombre, String nombreUsuario, String contrasena) {
        return nombre != null && !nombre.trim().isEmpty()
                && nombreUsuario != null && !nombreUsuario.trim().isEmpty()
                && contrasena != null && !contrasena.isEmpty();
    }

    private void registrarDesdeVista() {
        String nombre = registroView.getNombre();
        String nombreUsuario = registroView.getNombreUsuario();
        String contrasena = registroView.getContrasena();

        if (!datosRegistroValidos(nombre, nombreUsuario, contrasena)) {
            registroView.mostrarMensaje("Todos los campos son obligatorios.");
            return;
        }

        try {
            if (usuarioDAO.existeNombreUsuario(nombreUsuario.trim())) {
                registroView.mostrarMensaje("El nombre de usuario ya está registrado.");
                return;
            }
        } catch (SQLException e) {
            registroView.mostrarMensaje("Error al acceder a la base de datos.");
            return;
        }

        if (registrarUsuario(nombre, nombreUsuario, contrasena)) {
            registroView.mostrarMensaje("Usuario registrado correctamente.");
            registroView.limpiarCampos();
        } else {
            registroView.mostrarMensaje("No se pudo registrar el usuario.");
        }
    }

    private void iniciarSesionDesdeVista() {
        String nombreUsuario = loginView.getNombreUsuario();
        String contrasena = loginView.getContrasena();

        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            loginView.mostrarMensaje("Ingrese usuario y contraseña.");
            return;
        }

        if (iniciarSesion(nombreUsuario, contrasena)) {
            loginView.mostrarMensaje("Inicio de sesión correcto.");
            loginView.mostrarSesionActiva(usuarioActivo.getNombreUsuario());
            loginView.limpiarCampos();
        } else {
            loginView.mostrarMensaje("Credenciales incorrectas.");
        }
    }

    private void cerrarSesionDesdeVista() {
        if (!haySesionActiva()) {
            loginView.mostrarMensaje("No hay una sesión activa.");
            return;
        }

        cerrarSesion();
        loginView.mostrarSesionCerrada();
        loginView.mostrarMensaje("Sesión cerrada correctamente.");
    }

    private String generarHash(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] resultado = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte valor : resultado) {
                hash.append(String.format("%02x", valor));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo proteger la contraseña.", e);
        }
    }
}
