@RestController
@RequestMapping("/juegos")
@CrossOrigin(origins = "*")
public class JuegoController {

    @Autowired
    private JuegoRepository repo;

    @GetMapping
    public List<Juego> getJuegos() {
        return repo.findAll();
    }

    @GetMapping("/destacado")
    public Juego getDestacado() {
        return repo.findAll().get(0); // simple para el TFG
    }
}
