import java.awt.GridLayout;
import javax.swing.*;
public class EditarObjetoView {
    private final JPanel panel = new JPanel(new GridLayout(4, 2, 6, 6));
    private final JTextField nombre, descripcion, lugar, fecha;
    private final JFrame padre;
    public EditarObjetoView(JFrame padre, Objeto objeto) {
        this.padre = padre;
        nombre = new JTextField(objeto.getNombre());
        descripcion = new JTextField(objeto.getDescripcion());
        lugar = new JTextField(objeto.getLugarEncontrado());
        fecha = new JTextField(objeto.getFechaEncontrado().toString());
        panel.add(new JLabel("Nombre:")); panel.add(nombre);
        panel.add(new JLabel("Descripcion:")); panel.add(descripcion);
        panel.add(new JLabel("Lugar:")); panel.add(lugar);
        panel.add(new JLabel("Fecha (AAAA-MM-DD):")); panel.add(fecha);
    }
    public boolean confirmar() { return JOptionPane.showConfirmDialog(padre, panel,
        "Editar objeto (el estado no se modifica)", JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION; }
    public String getNombre() { return nombre.getText(); }
    public String getDescripcion() { return descripcion.getText(); }
    public String getLugar() { return lugar.getText(); }
    public String getFecha() { return fecha.getText(); }
}
