import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.swing.JOptionPane;

public class ObjetoController {
    private final ObjetoDAO objetoDAO;
    private final RegistroObjetoView registroView;
    private final ConsultaObjetoView consultaView;
    private final BooleanSupplier autorizador;

    // Constructor conservado para compatibilidad con el Main existente.
    // La autorizacion efectiva se activara al conectar el segundo constructor desde Main.
    public ObjetoController(RegistroObjetoView registroView,
            ConsultaObjetoView consultaView, ObjetoDAO objetoDAO) {
        this(registroView, consultaView, objetoDAO, () -> true);
    }

    public ObjetoController(RegistroObjetoView registroView,
            ConsultaObjetoView consultaView, ObjetoDAO objetoDAO,
            BooleanSupplier autorizador) {
        this.registroView = registroView;
        this.consultaView = consultaView;
        this.objetoDAO = objetoDAO;
        this.autorizador = autorizador == null ? () -> false : autorizador;

        registroView.agregarRegistroListener(e -> registrarDesdeVista());
        consultaView.agregarBusquedaListener(e -> buscarDesdeVista());
        consultaView.agregarConsultaListener(e -> consultarDesdeVista());
        consultaView.agregarDetalleListener(e -> mostrarDetalle());
        consultaView.agregarEditarListener(e -> editarSeleccionado());
        consultaView.agregarDisponiblesListener(e -> mostrarTodosDesdeVista());
        consultaView.agregarDevueltosListener(e -> mostrarTodosDesdeVista());
    }

    private boolean autorizado() {
        return autorizador.getAsBoolean();
    }

    public boolean registrarObjeto(String nombre, String descripcion, String lugar, String fecha) {
        if (!autorizado() || !validarCampos(nombre, lugar, fecha)) return false;
        try {
            LocalDate encontrado = LocalDate.parse(fecha.trim());
            if (encontrado.isAfter(LocalDate.now())) return false;
            return objetoDAO.guardar(new Objeto(0, nombre.trim(),
                    descripcion == null ? "" : descripcion.trim(), lugar.trim(), encontrado));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public List<Objeto> consultarObjetos() {
        return autorizado() ? objetoDAO.consultarTodos() : java.util.Collections.emptyList();
    }

    public List<Objeto> consultarNoReclamados() {
        return autorizado() ? objetoDAO.consultarPorEstado(Objeto.ESTADO_DISPONIBLE)
                : java.util.Collections.emptyList();
    }

    public List<Objeto> consultarDevueltos() {
        return autorizado() ? objetoDAO.consultarPorEstado(Objeto.ESTADO_DEVUELTO)
                : java.util.Collections.emptyList();
    }

    public List<Objeto> buscarObjetos(String texto) {
        if (!autorizado()) return java.util.Collections.emptyList();
        return texto == null || texto.trim().isEmpty() ? consultarObjetos() : objetoDAO.buscar(texto.trim());
    }

    public Objeto buscarPorId(int id) {
        return autorizado() && id > 0 ? objetoDAO.buscarPorId(id) : null;
    }

    public boolean modificarObjeto(int id, String nombre, String descripcion, String lugar, String fecha) {
        if (!autorizado() || id <= 0 || !validarCampos(nombre, lugar, fecha)) return false;
        try {
            LocalDate encontrada = LocalDate.parse(fecha.trim());
            if (encontrada.isAfter(LocalDate.now())) return false;
            return objetoDAO.modificarDatos(id, nombre, descripcion, lugar, encontrada);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public boolean actualizarEstado(int id, String nuevoEstado) {
        if (!autorizado() || id <= 0 || nuevoEstado == null) return false;
        String estado = nuevoEstado.trim().toUpperCase(java.util.Locale.ROOT);
        // El estado DEVUELTO solo puede establecerse desde DevolucionDAO.
        if (!Objeto.ESTADO_DISPONIBLE.equals(estado)) return false;
        // No hay transiciones directas permitidas desde este controlador.
        return false;
    }

    private boolean validarCampos(String nombre, String lugar, String fecha) {
        return nombre != null && !nombre.trim().isEmpty()
                && lugar != null && !lugar.trim().isEmpty()
                && fecha != null && !fecha.trim().isEmpty();
    }

    private boolean exigirSesionEnVista() {
        if (autorizado()) return true;
        consultaView.mostrarMensaje("Debe iniciar sesion para realizar esta operacion.");
        return false;
    }

    private void registrarDesdeVista() {
        if (!autorizado()) {
            registroView.mostrarMensaje("Debe iniciar sesion para registrar objetos.");
            return;
        }
        boolean registrado = registrarObjeto(registroView.getNombreObjeto(),
                registroView.getDescripcion(), registroView.getLugar(), registroView.getFecha());
        registroView.mostrarMensaje(registrado ? "Objeto registrado correctamente."
                : "No se pudo registrar el objeto. Revise el nombre, lugar y fecha (AAAA-MM-DD). La fecha no puede ser futura.");
        if (registrado) {
            registroView.limpiarCampos();
            consultaView.mostrarObjetos(consultarObjetos());
        }
    }

    private void mostrarDetalle() {
        if (!exigirSesionEnVista()) return;
        Objeto seleccionado = consultaView.getObjetoSeleccionado();
        if (seleccionado == null) { consultaView.mostrarMensaje("Seleccione un objeto de la lista."); return; }
        Objeto actual = buscarPorId(seleccionado.getId());
        if (actual == null) { consultaView.mostrarMensaje("El objeto ya no existe."); return; }
        JOptionPane.showMessageDialog(consultaView, actual.toString(), "Detalle del objeto", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editarSeleccionado() {
        if (!exigirSesionEnVista()) return;
        Objeto seleccionado = consultaView.getObjetoSeleccionado();
        if (seleccionado == null) { consultaView.mostrarMensaje("Seleccione un objeto de la lista."); return; }
        Objeto actual = buscarPorId(seleccionado.getId());
        if (actual == null) { consultaView.mostrarMensaje("El objeto ya no existe."); return; }
        EditarObjetoView editor = new EditarObjetoView(consultaView, actual);
        if (!editor.confirmar()) return;
        if (modificarObjeto(actual.getId(), editor.getNombre(), editor.getDescripcion(), editor.getLugar(), editor.getFecha())) {
            consultaView.mostrarObjetos(consultarObjetos());
            consultaView.mostrarMensaje("Objeto actualizado correctamente.");
        } else {
            consultaView.mostrarMensaje("No se pudo actualizar. Verifique los campos y la fecha.");
        }
    }

    private void buscarDesdeVista() { mostrarTodosDesdeVista(); }
    private void consultarDesdeVista() { mostrarTodosDesdeVista(); }
    private void mostrarTodosDesdeVista() {
        if (exigirSesionEnVista()) consultaView.mostrarObjetos(consultarObjetos());
    }
}
