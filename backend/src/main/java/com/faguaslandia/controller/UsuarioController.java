package com.faguaslandia.controller;

import com.faguaslandia.model.EstadoAmigo;
import com.faguaslandia.model.EstadoUsuario;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.model.Amigo;
import com.faguaslandia.repository.AmigoRepository;
import com.faguaslandia.repository.CompraRepository;
import com.faguaslandia.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final AmigoRepository amigoRepository;
    private final CompraRepository compraRepository;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            AmigoRepository amigoRepository,
            CompraRepository compraRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.amigoRepository = amigoRepository;
        this.compraRepository = compraRepository;
    }

    // -------------------------------------------------------
    // CREAR USUARIO
    // -------------------------------------------------------
    @PostMapping("/crear")
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El email ya está en uso"
            );
        }
        return usuarioRepository.save(usuario);
    }

    @GetMapping("/listar")
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    // -------------------------------------------------------
    // ACTUALIZAR PERFIL
    // -------------------------------------------------------
    @PutMapping(value = "/actualizar/{id}", consumes = {"multipart/form-data"})
    public Usuario actualizarUsuario(
            @PathVariable Long id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) MultipartFile foto
    ) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"
                ));

        if (nombre != null && !nombre.isBlank()) usuario.setNombre(nombre);
        if (email != null && !email.isBlank())   usuario.setEmail(email);

        if (foto != null && !foto.isEmpty()) {
            try {
                if (!foto.getContentType().startsWith("image/")) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Solo se permiten imágenes"
                    );
                }
                String folder = "uploads/";
                File directory = new File(folder);
                if (!directory.exists()) directory.mkdirs();

                // Borrar foto anterior
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().startsWith("user_" + id)) f.delete();
                    }
                }

                String filename = "user_" + id + ".jpg";
                Path path = Paths.get(folder + filename);
                BufferedImage original = ImageIO.read(foto.getInputStream());
                if (original == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Formato de imagen no soportado"
                    );
                }
                BufferedImage rgbImage = new BufferedImage(
                        original.getWidth(), original.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );
                rgbImage.getGraphics().drawImage(original, 0, 0, null);
                ImageIO.write(rgbImage, "jpg", path.toFile());
                usuario.setFoto(filename);
            } catch (Exception e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Error guardando imagen"
                );
            }
        }

        return usuarioRepository.save(usuario);
    }

    // -------------------------------------------------------
    // STATS REALES DEL PERFIL
    // GET /usuarios/{id}/stats
    // -------------------------------------------------------
    @GetMapping("/{id}/stats")
    public Map<String, Object> getStats(@PathVariable Long id) {
        long numJuegos = compraRepository.findByUsuarioId(id).size();
        // Horas y logros reales: por ahora calculados desde las compras.
        // Cuando implementes sesiones de juego, actualiza aquí.
        long horas   = numJuegos * 5L;   // placeholder realista: ~5h por juego
        long logros  = numJuegos * 2L;   // placeholder: ~2 logros por juego
        return Map.of(
                "juegos", numJuegos,
                "horas",  horas,
                "logros", logros
        );
    }

    // -------------------------------------------------------
    // AMIGOS — ENVIAR SOLICITUD
    // -------------------------------------------------------
    @PostMapping("/{id1}/agregar/{id2}")
    public String enviarSolicitud(
            @PathVariable Long id1,
            @PathVariable Long id2
    ) {
        if (id1.equals(id2)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No puedes agregarte a ti mismo"
            );
        }

        // Evitar duplicados
        List<Amigo> existentes = amigoRepository.findByUsuario1IdOrUsuario2Id(id1, id2);
        boolean yaSolicitud = existentes.stream().anyMatch(a ->
                (a.getUsuario1().getId().equals(id1) && a.getUsuario2().getId().equals(id2)) ||
                        (a.getUsuario1().getId().equals(id2) && a.getUsuario2().getId().equals(id1))
        );
        if (yaSolicitud) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe una relación con ese usuario"
            );
        }

        Usuario u1 = usuarioRepository.findById(id1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario emisor no encontrado"));
        Usuario u2 = usuarioRepository.findById(id2)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario receptor no encontrado"));

        amigoRepository.save(new Amigo(u1, u2));
        return "Solicitud enviada";
    }

    // -------------------------------------------------------
    // AMIGOS — VER SOLICITUDES PENDIENTES
    // -------------------------------------------------------
    @GetMapping("/{id}/solicitudes")
    public List<Amigo> verSolicitudes(@PathVariable Long id) {
        return amigoRepository.findByUsuario2IdAndEstado(id, EstadoAmigo.pendiente);
    }

    // -------------------------------------------------------
    // AMIGOS — ACEPTAR SOLICITUD
    // -------------------------------------------------------
    @PutMapping("/solicitud/{id}/aceptar")
    public String aceptarSolicitud(@PathVariable Long id) {
        Amigo sol = amigoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        sol.setEstado(EstadoAmigo.aceptado);
        amigoRepository.save(sol);
        return "Solicitud aceptada";
    }

    // -------------------------------------------------------
    // AMIGOS — RECHAZAR SOLICITUD  (NUEVO)
    // -------------------------------------------------------
    @DeleteMapping("/solicitud/{id}/rechazar")
    public String rechazarSolicitud(@PathVariable Long id) {
        Amigo sol = amigoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        amigoRepository.delete(sol);
        return "Solicitud rechazada";
    }

    // -------------------------------------------------------
    // AMIGOS — ELIMINAR AMIGO  (NUEVO)
    // -------------------------------------------------------
    @DeleteMapping("/{id}/amigos/{amigoId}")
    public String eliminarAmigo(
            @PathVariable Long id,
            @PathVariable Long amigoId
    ) {
        List<Amigo> relaciones = amigoRepository.findByUsuario1IdOrUsuario2Id(id, id);
        Amigo relacion = relaciones.stream()
                .filter(a -> a.getEstado() == EstadoAmigo.aceptado &&
                        (a.getUsuario1().getId().equals(amigoId) || a.getUsuario2().getId().equals(amigoId)))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relación no encontrada"));
        amigoRepository.delete(relacion);
        return "Amigo eliminado";
    }

    // -------------------------------------------------------
    // AMIGOS — BLOQUEAR  (NUEVO)
    // -------------------------------------------------------
    @PutMapping("/{id}/bloquear/{amigoId}")
    public String bloquearAmigo(
            @PathVariable Long id,
            @PathVariable Long amigoId
    ) {
        List<Amigo> relaciones = amigoRepository.findByUsuario1IdOrUsuario2Id(id, id);
        Amigo relacion = relaciones.stream()
                .filter(a ->
                        (a.getUsuario1().getId().equals(id) && a.getUsuario2().getId().equals(amigoId)) ||
                                (a.getUsuario1().getId().equals(amigoId) && a.getUsuario2().getId().equals(id)))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relación no encontrada"));
        relacion.setEstado(EstadoAmigo.bloqueado);
        amigoRepository.save(relacion);
        return "Usuario bloqueado";
    }

    // -------------------------------------------------------
    // AMIGOS — LISTAR AMIGOS ACEPTADOS
    // -------------------------------------------------------
    @GetMapping("/{id}/amigos")
    public List<Amigo> verAmigos(@PathVariable Long id) {
        return amigoRepository.findByUsuario1IdOrUsuario2Id(id, id);
    }
}