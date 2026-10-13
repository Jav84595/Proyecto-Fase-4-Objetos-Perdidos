import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class ConsultaDevolucionesView extends JFrame {
    private final JTextArea area = new JTextArea();
    private final JButton actualizar = new JButton("Mostrar todos");
    private final JButton buscar = new JButton("Buscar");
    private final JTextField texto = new JTextField(18);
    private final JComboBox<String> tipo = new JComboBox<>(new String[] {
        "Todos los campos", "ID devolucion", "ID objeto", "Propietario",
        "Responsable", "Fecha (AAAA-MM-DD)", "Evidencia"
    });
    private final JLabel contador = new JLabel("0 resultados");
    private List<Devolucion> registros = new ArrayList<>();

    public ConsultaDevolucionesView() {
        setTitle("Historial de devoluciones");
        setSize(900, 420);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(new JLabel("Filtrar por:"));
        barra.add(tipo);
        barra.add(texto);
        barra.add(buscar);
        barra.add(actualizar);
        add(barra, BorderLayout.NORTH);
        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);
        add(contador, BorderLayout.SOUTH);
        buscar.addActionListener(e -> filtrar());
        texto.addActionListener(e -> filtrar());
        actualizar.addActionListener(e -> { texto.setText(""); tipo.setSelectedIndex(0); });
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    public void agregarActualizarListener(ActionListener listener) {
        actualizar.addActionListener(listener);
    }

    public void mostrarDevoluciones(List<Devolucion> devoluciones) {
        registros = devoluciones == null ? new ArrayList<>() : new ArrayList<>(devoluciones);
        filtrar();
    }

    private void filtrar() {
        String valor = texto.getText().trim();
        int opcion = tipo.getSelectedIndex();
        if ((opcion == 1 || opcion == 2) && !valor.isEmpty()) {
            try { if (Integer.parseInt(valor) <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingrese un ID entero positivo."); return;
            }
        }
        if (opcion == 5 && !valor.isEmpty()) {
            try { LocalDate.parse(valor); }
            catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Fecha invalida. Use AAAA-MM-DD."); return;
            }
        }
        List<Devolucion> filtradas = new ArrayList<>();
        for (Devolucion d : registros) if (coincide(d, opcion, valor)) filtradas.add(d);
        presentar(filtradas);
    }

    private boolean coincide(Devolucion d, int opcion, String valor) {
        if (valor.isEmpty()) return true;
        switch (opcion) {
            case 1: return String.valueOf(d.getId()).equals(valor);
            case 2: return String.valueOf(d.getObjetoId()).equals(valor);
            case 3: return contiene(d.getNombrePropietario(), valor);
            case 4: return contiene(d.getUsuarioResponsable(), valor);
            case 5: return d.getFechaDevolucion().toString().equals(valor);
            case 6: return contiene(d.getInformacionVerificacion(), valor);
            default: return contiene(String.valueOf(d.getId()), valor)
                || contiene(String.valueOf(d.getObjetoId()), valor)
                || contiene(d.getNombrePropietario(), valor)
                || contiene(d.getUsuarioResponsable(), valor)
                || contiene(d.getFechaDevolucion().toString(), valor)
                || contiene(d.getInformacionVerificacion(), valor);
        }
    }

    private boolean contiene(String origen, String valor) {
        return origen != null && origen.toLowerCase(Locale.ROOT).contains(valor.toLowerCase(Locale.ROOT));
    }

    private void presentar(List<Devolucion> lista) {
        area.setText("");
        contador.setText(lista.size() + " resultado(s) de " + registros.size() + " devoluciones");
        if (lista.isEmpty()) { area.setText("No se encontraron devoluciones."); return; }
        for (Devolucion d : lista) {
            String responsable = d.getUsuarioResponsable() == null
                ? "No registrado (dato anterior)" : d.getUsuarioResponsable();
            area.append("Devolucion #" + d.getId() + " | Objeto #" + d.getObjetoId()
                + " | Propietario: " + d.getNombrePropietario()
                + " | Fecha: " + d.getFechaDevolucion()
                + " | Responsable: " + responsable
                + "\nEvidencia: " + d.getInformacionVerificacion() + "\n\n");
        }
        area.setCaretPosition(0);
    }
}
