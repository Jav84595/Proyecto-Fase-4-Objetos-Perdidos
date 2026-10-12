import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DevolucionView extends JFrame {
    private final JTextField txtObjetoId = new JTextField();
    private final JTextField txtPropietario = new JTextField();
    private final JTextField txtVerificacion = new JTextField();
    private final JButton btnDevolver = new JButton("Registrar devolución");

    public DevolucionView() {
        setTitle("Devolución de objeto");
        setSize(440, 230);
        setLayout(new GridLayout(4, 2, 5, 5));
        add(new JLabel("ID del objeto:"));
        add(txtObjetoId);
        add(new JLabel("Propietario:"));
        add(txtPropietario);
        add(new JLabel("Datos de verificación:"));
        add(txtVerificacion);
        add(new JLabel());
        add(btnDevolver);

        txtObjetoId.setToolTipText("Ingrese el ID numérico del objeto disponible.");
        txtPropietario.setToolTipText("Ingrese el nombre de quien reclama el objeto.");
        txtVerificacion.setToolTipText("Ingrese la información utilizada para verificar la propiedad.");
        btnDevolver.setToolTipText("Complete todos los campos para registrar la devolución.");

        DocumentListener actualizarBoton = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { actualizarDisponibilidad(); }
            @Override
            public void removeUpdate(DocumentEvent e) { actualizarDisponibilidad(); }
            @Override
            public void changedUpdate(DocumentEvent e) { actualizarDisponibilidad(); }
        };
        txtObjetoId.getDocument().addDocumentListener(actualizarBoton);
        txtPropietario.getDocument().addDocumentListener(actualizarBoton);
        txtVerificacion.getDocument().addDocumentListener(actualizarBoton);
        actualizarDisponibilidad();
        getRootPane().setDefaultButton(btnDevolver);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void actualizarDisponibilidad() {
        btnDevolver.setEnabled(!getObjetoId().isEmpty()
                && !getPropietario().isEmpty()
                && !getVerificacion().isEmpty());
    }

    public String getObjetoId() {
        return txtObjetoId.getText().trim();
    }

    public String getPropietario() {
        return txtPropietario.getText().trim();
    }

    public String getVerificacion() {
        return txtVerificacion.getText().trim();
    }

    public void agregarDevolucionListener(ActionListener listener) {
        btnDevolver.addActionListener(listener);
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
    }

    public void limpiarCampos() {
        txtObjetoId.setText("");
        txtPropietario.setText("");
        txtVerificacion.setText("");
        txtObjetoId.requestFocusInWindow();
    }
}
