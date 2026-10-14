import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try {
            ConexionBD.inicializarBaseDatos();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "No se pudo iniciar la base de datos.\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        SwingUtilities.invokeLater(() -> {
            RegistroUsuarioView registroUsuarioView = new RegistroUsuarioView();
            LoginView loginView = new LoginView();
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            UsuarioController usuarioController =
                    new UsuarioController(usuarioDAO, registroUsuarioView, loginView);

            RegistroObjetoView registroObjetoView = new RegistroObjetoView();
            ConsultaObjetoView consultaObjetoView = new ConsultaObjetoView();
            ObjetoDAO objetoDAO = new ObjetoDAO();
            ObjetoController objetoController = new ObjetoController(
                    registroObjetoView,
                    consultaObjetoView,
                    objetoDAO,
                    usuarioController::autorizarOperacion
            );

            DevolucionView devolucionView = new DevolucionView();
            DevolucionDAO devolucionDAO = new DevolucionDAO();
            DevolucionController devolucionController = new DevolucionController(
                    objetoController,
                    devolucionDAO,
                    devolucionView,
                    usuarioController::getUsuarioActivo
            );

            ConsultaDevolucionesView historialView = new ConsultaDevolucionesView();
            historialView.agregarActualizarListener(e ->
                    historialView.mostrarDevoluciones(devolucionController.consultarDevoluciones()));
            devolucionView.agregarHistorialListener(e -> {
                historialView.mostrarDevoluciones(devolucionController.consultarDevoluciones());
                historialView.setLocationRelativeTo(devolucionView);
                historialView.setVisible(true);
            });

            registroUsuarioView.setLocation(20, 20);
            loginView.setLocation(390, 20);
            registroObjetoView.setLocation(20, 280);
            consultaObjetoView.setLocation(440, 280);
            devolucionView.setLocation(760, 20);

            registroUsuarioView.setVisible(true);
            loginView.setVisible(true);
            // Mantener los modulos protegidos ocultos hasta autenticar.
            registroObjetoView.setVisible(false);
            consultaObjetoView.setVisible(false);
            devolucionView.setVisible(false);
            historialView.setVisible(false);

            // Reaccionar inmediatamente a los cambios de sesion.
            usuarioController.agregarObservadorSesion(usuario -> {
                boolean activa = usuario != null;
                registroObjetoView.setVisible(activa);
                consultaObjetoView.setVisible(activa);
                devolucionView.setVisible(activa);
                if (!activa) {
                    historialView.setVisible(false);
                }
            });
        });
    }
}
