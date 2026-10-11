import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class LoginView extends JFrame {
    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtContrasena = new JPasswordField();
    private final JButton btnIngresar = new JButton("Ingresar");
    private final JButton btnCerrarSesion = new JButton("Cerrar sesión");
    private final JLabel lblSesion = new JLabel("Sin sesión activa");

    public LoginView() {
        setTitle("Inicio de sesión");
        setSize(350, 220);
        setLayout(new GridLayout(5, 2, 5, 5));
        add(new JLabel("Usuario:"));
        add(txtUsuario);
        add(new JLabel("Contraseña:"));
        add(txtContrasena);
        add(new JLabel());
        add(btnIngresar);
        add(new JLabel("Estado:"));
        add(lblSesion);
        add(new JLabel());
        add(btnCerrarSesion);

        btnCerrarSesion.setEnabled(false);
        getRootPane().setDefaultButton(btnIngresar);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public String getNombreUsuario() {
        return txtUsuario.getText().trim();
    }

    public String getContrasena() {
        return new String(txtContrasena.getPassword());
    }

    public void agregarLoginListener(ActionListener listener) {
        btnIngresar.addActionListener(listener);
    }

    public void agregarCerrarSesionListener(ActionListener listener) {
        btnCerrarSesion.addActionListener(listener);
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
    }

    public void mostrarSesionActiva(String nombreUsuario) {
        lblSesion.setText("Sesión: " + nombreUsuario);
        btnCerrarSesion.setEnabled(true);
        btnIngresar.setEnabled(false);
        txtUsuario.setEnabled(false);
        txtContrasena.setEnabled(false);
    }

    public void mostrarSesionCerrada() {
        lblSesion.setText("Sin sesión activa");
        btnCerrarSesion.setEnabled(false);
        btnIngresar.setEnabled(true);
        txtUsuario.setEnabled(true);
        txtContrasena.setEnabled(true);
        limpiarCampos();
    }

    public void limpiarCampos() {
        txtUsuario.setText("");
        txtContrasena.setText("");
    }
}
