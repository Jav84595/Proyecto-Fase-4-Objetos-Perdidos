import java.time.LocalDate;

public class Devolucion {
    private int id;
    private int objetoId;
    private String nombrePropietario;
    private String informacionVerificacion;
    private LocalDate fechaDevolucion;
    private final String usuarioResponsable;

    public Devolucion(int id, int objetoId, String nombrePropietario,
            String informacionVerificacion, LocalDate fechaDevolucion) {
        this(id, objetoId, nombrePropietario, informacionVerificacion, fechaDevolucion, null);
    }

    public Devolucion(int id, int objetoId, String nombrePropietario,
            String informacionVerificacion, LocalDate fechaDevolucion, String usuarioResponsable) {
        this.usuarioResponsable = usuarioResponsable;
        this.id = id;
        this.objetoId = objetoId;
        this.nombrePropietario = nombrePropietario;
        this.informacionVerificacion = informacionVerificacion;
        this.fechaDevolucion = fechaDevolucion;
    }

    public String getUsuarioResponsable() { return usuarioResponsable; }

    public int getId() {
        return id;
    }

    public int getObjetoId() {
        return objetoId;
    }

    public String getNombrePropietario() {
        return nombrePropietario;
    }

    public String getInformacionVerificacion() {
        return informacionVerificacion;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }
}