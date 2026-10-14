import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class ConsultaObjetoView extends JFrame {
    private final JTextField txtBusqueda = new JTextField(14);
    private final JComboBox<String> criterio = new JComboBox<>(new String[] {
        "Todos los campos", "ID", "Nombre", "Descripcion", "Lugar", "Fecha (AAAA-MM-DD)"});
    private final JComboBox<String> estado = new JComboBox<>(new String[] {"Todos los estados", "DISPONIBLE", "DEVUELTO"});
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnMostrarTodos = new JButton("Mostrar todos");
    private final JButton btnMostrarDisponibles = new JButton("Mostrar disponibles");
    private final JButton btnMostrarDevueltos = new JButton("Mostrar devueltos");
    private final JButton btnDetalle = new JButton("Ver detalle");
    private final JButton btnEditar = new JButton("Editar seleccionado");
    private final DefaultListModel<Objeto> modelo = new DefaultListModel<>();
    private final JList<Objeto> lista = new JList<>(modelo);
    private final JLabel contador = new JLabel("0 resultados");
    private List<Objeto> datos = new ArrayList<>();

    public ConsultaObjetoView() {
        setTitle("Consulta de objetos"); setSize(850, 430);
        JPanel superior = new JPanel(new GridLayout(2, 1));
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Buscar:")); filtros.add(criterio); filtros.add(txtBusqueda);
        filtros.add(new JLabel("Estado:")); filtros.add(estado); filtros.add(btnBuscar);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnMostrarTodos); botones.add(btnMostrarDisponibles);
        botones.add(btnMostrarDevueltos); botones.add(btnDetalle); botones.add(btnEditar);
        superior.add(filtros); superior.add(botones); add(superior, BorderLayout.NORTH);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(lista), BorderLayout.CENTER); add(contador, BorderLayout.SOUTH);
        txtBusqueda.addActionListener(e -> filtrar());
        // El controlador actualiza la lista y aplica los filtros al buscar.
        btnMostrarTodos.addActionListener(e -> { txtBusqueda.setText(""); criterio.setSelectedIndex(0); estado.setSelectedIndex(0); });
        btnMostrarDisponibles.addActionListener(e -> estado.setSelectedItem("DISPONIBLE"));
        btnMostrarDevueltos.addActionListener(e -> estado.setSelectedItem("DEVUELTO"));
        criterio.addActionListener(e -> filtrar());
        estado.addActionListener(e -> filtrar());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
    public String getTextoBusqueda() { return txtBusqueda.getText().trim(); }
    public void agregarBusquedaListener(ActionListener l) { btnBuscar.addActionListener(l); }
    public void agregarConsultaListener(ActionListener l) { btnMostrarTodos.addActionListener(l); }
    public void agregarDisponiblesListener(ActionListener l) { btnMostrarDisponibles.addActionListener(l); }
    public void agregarDevueltosListener(ActionListener l) { btnMostrarDevueltos.addActionListener(l); }
    public void agregarDetalleListener(ActionListener l) { btnDetalle.addActionListener(l); }
    public void agregarEditarListener(ActionListener l) { btnEditar.addActionListener(l); }
    public Objeto getObjetoSeleccionado() { return lista.getSelectedValue(); }
    public void mostrarMensaje(String msg) { JOptionPane.showMessageDialog(this, msg); }
    public void mostrarObjetos(List<Objeto> objetos) {
        datos = objetos == null ? new ArrayList<>() : new ArrayList<>(objetos);
        filtrar();
    }
    private void filtrar() {
        String texto = txtBusqueda.getText().trim(); int opcion = criterio.getSelectedIndex();
        if (opcion == 1 && !texto.isEmpty()) {
            try { if (Integer.parseInt(texto) <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException e) { mostrarMensaje("El ID debe ser un entero positivo."); return; }
        }
        if (opcion == 5 && !texto.isEmpty()) {
            try { LocalDate.parse(texto); }
            catch (DateTimeParseException e) { mostrarMensaje("Fecha invalida. Use AAAA-MM-DD."); return; }
        }
        modelo.clear();
        for (Objeto o : datos) {
            if (!"Todos los estados".equals(estado.getSelectedItem())
                    && !o.getEstado().equals(estado.getSelectedItem())) continue;
            if (coincide(o, opcion, texto)) modelo.addElement(o);
        }
        contador.setText(modelo.size() + " resultado(s) de " + datos.size());
    }
    private boolean coincide(Objeto o, int opcion, String t) {
        if (t.isEmpty()) return true;
        switch (opcion) {
            case 1: return String.valueOf(o.getId()).equals(t);
            case 2: return contiene(o.getNombre(), t);
            case 3: return contiene(o.getDescripcion(), t);
            case 4: return contiene(o.getLugarEncontrado(), t);
            case 5: return o.getFechaEncontrado().toString().equals(t);
            default: return contiene(String.valueOf(o.getId()), t) || contiene(o.getNombre(), t)
                || contiene(o.getDescripcion(), t) || contiene(o.getLugarEncontrado(), t)
                || contiene(o.getFechaEncontrado().toString(), t) || contiene(o.getEstado(), t);
        }
    }
    private boolean contiene(String a, String b) {
        return a != null && a.toLowerCase(Locale.ROOT).contains(b.toLowerCase(Locale.ROOT));
    }
}
