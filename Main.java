import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

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
                    objetoDAO
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
            // Las operaciones protegidas no se muestran sin una sesion activa.
            // Este control de interfaz debe complementarse con autorizacion
            // en los controladores antes de considerar completo el requisito.
            final boolean[] sesionAnterior = {false};
            Timer controlSesion = new Timer(200, e -> {
                boolean sesionActiva = usuarioController.haySesionActiva();
                if (sesionActiva != sesionAnterior[0]) {
                    registroObjetoView.setVisible(sesionActiva);
                    consultaObjetoView.setVisible(sesionActiva);
                    devolucionView.setVisible(sesionActiva);
                    if (!sesionActiva) historialView.setVisible(false);
                    sesionAnterior[0] = sesionActiva;
                }
            });
            controlSesion.start();
        });
    }
}
