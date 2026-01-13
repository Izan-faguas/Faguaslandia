@Entity
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(length = 500)
    private String descripcion;

    private double precio;
    private String imagen;
    private int descuento;

    // GETTERS Y SETTERS
}
