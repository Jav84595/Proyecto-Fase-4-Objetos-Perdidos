import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

public class DevolucionController {
    private final ObjetoController objetoController;
    private final DevolucionDAO devolucionDAO;
    private final DevolucionView view;
    private final Supplier<Usuario> usuarioActual;

    public DevolucionController(
            ObjetoController objetoController,
            DevolucionDAO devolucionDAO,
            DevolucionView view, Supplier<Usuario> usuarioActual) {
        this.usuarioActual = usuarioActual;
        this.objetoController = objetoController;
        this.devolucionDAO = devolucionDAO;
        this.view = view;
        view.agregarDevolucionListener(e -> registrarDesdeVista());
    }

    // Valida la existencia y disponibilidad del objeto y que se aportaron datos.
    // No constituye una comprobacion independiente de la identidad del propietario.
    public boolean verificarPropietario(int objetoId, String verificacion) {
        if (objetoId <= 0 || esVacio(verificacion)) {
            return false;
        }
        Objeto objeto = objetoController.buscarPorId(objetoId);
        return objeto != null && Objeto.ESTADO_DISPONIBLE.equals(objeto.getEstado());
    }

    public boolean registrarDevolucion(int objetoId, String propietario, String verificacion) {
        return false; // La confirmacion explicita es obligatoria.
    }

    public boolean registrarDevolucion(int objetoId, String propietario, String verificacion, boolean confirmado) {
        Usuario responsable = usuarioActual == null ? null : usuarioActual.get();
        if (responsable == null || !confirmado || esVacio(propietario)
                || !verificarPropietario(objetoId, verificacion)) {
            return false;
        }

        Devolucion devolucion = new Devolucion(
                0,
                objetoId,
                propietario.trim(),
                verificacion.trim(),
                LocalDate.now(),
                responsable.getNombreUsuario()
        );
        return devolucionDAO.registrarDevolucion(devolucion);
    }

    public List<Devolucion> consultarDevoluciones() {
        return usuarioActual != null && usuarioActual.get() != null
                ? devolucionDAO.consultarTodas() : java.util.Collections.emptyList();
    }

    public Devolucion buscarDevolucionPorId(int id) {
        return usuarioActual != null && usuarioActual.get() != null && id > 0
                ? devolucionDAO.buscarPorId(id) : null;
    }

    private static boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private void registrarDesdeVista() {
        if (usuarioActual == null || usuarioActual.get() == null) {
            view.mostrarMensaje("Debe iniciar sesion para registrar devoluciones.");
            return;
        }
        String idIngresado = view.getObjetoId();
        String propietario = view.getPropietario();
        String verificacion = view.getVerificacion();

        if (esVacio(idIngresado) || esVacio(propietario) || esVacio(verificacion)) {
            view.mostrarMensaje("Complete el ID, el propietario y los datos de verificacion.");
            return;
        }

        final int objetoId;
        try {
            objetoId = Integer.parseInt(idIngresado);
        } catch (NumberFormatException e) {
            view.mostrarMensaje("El ID del objeto debe ser un numero entero valido.");
            return;
        }

        if (objetoId <= 0) {
            view.mostrarMensaje("El ID del objeto debe ser mayor que cero.");
            return;
        }

        Objeto objeto = objetoController.buscarPorId(objetoId);
        if (objeto == null) {
            view.mostrarMensaje("No existe un objeto registrado con ese ID.");
            return;
        }
        if (!Objeto.ESTADO_DISPONIBLE.equals(objeto.getEstado())) {
            view.mostrarMensaje("El objeto no esta disponible para devolucion.");
            return;
        }

        if (!view.confirmarVerificacion(objeto)) {
            view.mostrarMensaje("Devolucion cancelada: no se confirmo la verificacion del reclamante.");
            return;
        }

        boolean registrada = registrarDevolucion(objetoId, propietario, verificacion, true);
        view.mostrarMensaje(registrada
                ? "Devolucion registrada correctamente. El objeto ahora esta DEVUELTO."
                : "No se pudo registrar la devolucion. Compruebe que el objeto siga disponible.");
        if (registrada) {
            view.limpiarCampos();
        }
    }
}
