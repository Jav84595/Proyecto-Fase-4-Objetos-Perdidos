import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

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

        consultaView.agregarDisponiblesListener(
                e -> consultaView.mostrarObjetos(consultarNoReclamados()));

        consultaView.agregarDevueltosListener(
                e -> consultaView.mostrarObjetos(consultarDevueltos()));

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

    public boolean actualizarEstado(int id, String nuevoEstado) {
        if (id <= 0 || nuevoEstado == null) return false;
        String estado = nuevoEstado.trim().toUpperCase();
        if (!estado.equals(Objeto.ESTADO_DISPONIBLE)
                && !estado.equals(Objeto.ESTADO_DEVUELTO)) return false;
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
        consultaView.mostrarObjetos(buscarObjetos(consultaView.getTextoBusqueda()));
    }

    private void consultarDesdeVista() {
        consultaView.mostrarObjetos(consultarObjetos());
    }
}
