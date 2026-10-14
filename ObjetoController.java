import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.JOptionPane;

public class ObjetoController {
    private ObjetoDAO objetoDAO;
    private RegistroObjetoView registroView;
    private ConsultaObjetoView consultaView;

    public ObjetoController(RegistroObjetoView registroView,
            ConsultaObjetoView consultaView, ObjetoDAO objetoDAO) {
        this.registroView = registroView;
        this.consultaView = consultaView;
        this.objetoDAO = objetoDAO;
        registroView.agregarRegistroListener(e -> registrarDesdeVista());
        consultaView.agregarBusquedaListener(e -> buscarDesdeVista());
        consultaView.agregarConsultaListener(e -> consultarDesdeVista());

        consultaView.agregarDetalleListener(e -> mostrarDetalle());
        consultaView.agregarEditarListener(e -> editarSeleccionado());
        consultaView.agregarDisponiblesListener(
                e -> consultaView.mostrarObjetos(consultarObjetos()));

        consultaView.agregarDevueltosListener(
                e -> consultaView.mostrarObjetos(consultarObjetos()));

    }

    public boolean registrarObjeto(String nombre, String descripcion, String lugar, String fecha) {
        if (!validarCampos(nombre, lugar, fecha)) return false;
        try {
            LocalDate fechaEncontrado = LocalDate.parse(fecha);
            if (fechaEncontrado.isAfter(LocalDate.now())) return false;
            Objeto objeto = new Objeto(0, nombre.trim(),
                    descripcion == null ? "" : descripcion.trim(),
                    lugar.trim(), fechaEncontrado);
            return objetoDAO.guardar(objeto);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public List<Objeto> consultarObjetos() { return objetoDAO.consultarTodos(); }


    public List<Objeto> consultarNoReclamados() {
        return objetoDAO.consultarPorEstado(Objeto.ESTADO_DISPONIBLE);
    }

    public List<Objeto> consultarDevueltos() {
        return objetoDAO.consultarPorEstado(Objeto.ESTADO_DEVUELTO);
    }


    public List<Objeto> buscarObjetos(String texto) {
        if (texto == null || texto.trim().isEmpty()) return consultarObjetos();
        return objetoDAO.buscar(texto.trim());
    }

    public Objeto buscarPorId(int id) {
        return id > 0 ? objetoDAO.buscarPorId(id) : null;
    }

    public boolean modificarObjeto(int id, String nombre, String descripcion, String lugar, String fecha) {
        if (!validarCampos(nombre, lugar, fecha) || id <= 0) return false;
        try {
            LocalDate parsed = LocalDate.parse(fecha.trim());
            return objetoDAO.modificarDatos(id, nombre, descripcion, lugar, parsed);
        } catch (DateTimeParseException e) { return false; }
    }

    private void mostrarDetalle() {
        Objeto objeto = consultaView.getObjetoSeleccionado();
        if (objeto == null) { consultaView.mostrarMensaje("Seleccione un objeto de la lista."); return; }
        Objeto actual = buscarPorId(objeto.getId());
        if (actual == null) { consultaView.mostrarMensaje("El objeto ya no existe."); return; }
        JOptionPane.showMessageDialog(consultaView, actual.toString(), "Detalle del objeto", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editarSeleccionado() {
        Objeto seleccionado = consultaView.getObjetoSeleccionado();
        if (seleccionado == null) { consultaView.mostrarMensaje("Seleccione un objeto de la lista."); return; }
        Objeto actual = buscarPorId(seleccionado.getId());
        if (actual == null) { consultaView.mostrarMensaje("El objeto ya no existe."); return; }
        EditarObjetoView editor = new EditarObjetoView(consultaView, actual);
        if (!editor.confirmar()) return;
        if (modificarObjeto(actual.getId(), editor.getNombre(), editor.getDescripcion(), editor.getLugar(), editor.getFecha())) {
            consultaView.mostrarObjetos(consultarObjetos());
            consultaView.mostrarMensaje("Objeto actualizado correctamente.");
        } else consultaView.mostrarMensaje("No se pudo actualizar. Verifique los campos y la fecha.");
    }

    public boolean actualizarEstado(int id, String nuevoEstado) {
        if (id <= 0 || nuevoEstado == null) return false;
        String estado = nuevoEstado.trim().toUpperCase();
        if (!estado.equals(Objeto.ESTADO_DISPONIBLE)
                && !estado.equals(Objeto.ESTADO_DEVUELTO)) return false;
        if (Objeto.ESTADO_DEVUELTO.equals(estado)) return false;
        return buscarPorId(id) != null && objetoDAO.actualizarEstado(id, estado);
    }

    private boolean validarCampos(String nombre, String lugar, String fecha) {
        return nombre != null && !nombre.trim().isEmpty()
                && lugar != null && !lugar.trim().isEmpty()
                && fecha != null && !fecha.trim().isEmpty();
    }

    private void registrarDesdeVista() {
        boolean registrado = registrarObjeto(registroView.getNombreObjeto(),
                registroView.getDescripcion(), registroView.getLugar(), registroView.getFecha());
        registroView.mostrarMensaje(registrado
                ? "Objeto registrado correctamente."
                : "No se pudo registrar el objeto. Revise el nombre, lugar y fecha "
                  + "(AAAA-MM-DD). La fecha no puede ser futura.");
        if (registrado) {
            registroView.limpiarCampos();
            consultaView.mostrarObjetos(consultarObjetos());
        }
    }

    private void buscarDesdeVista() {
        consultaView.mostrarObjetos(consultarObjetos());
    }

    private void consultarDesdeVista() {
        consultaView.mostrarObjetos(consultarObjetos());
    }
}
