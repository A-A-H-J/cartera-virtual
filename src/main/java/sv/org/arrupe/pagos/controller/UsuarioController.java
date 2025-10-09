package sv.org.arrupe.pagos.controller;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.org.arrupe.pagos.model.Usuario;
import sv.org.arrupe.pagos.service.UsuarioService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;


/**
 *
 * @author perez
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> getAll() {
        return usuarioService.getAll();
    }

    @GetMapping("/{id}")
    public Usuario getById(@PathVariable Long id) {
        return usuarioService.getById(id);
    }

    @PostMapping
    public Usuario create(@RequestBody Usuario usuario) {
        return usuarioService.saveUser(usuario);
    }

    @PutMapping("/{id}")
    public Usuario update(@PathVariable Long id, @RequestBody Usuario usuario) {
        usuario.setIdUsuario(id);
        return usuarioService.saveUser(usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            usuarioService.deleteById(id);
            return ResponseEntity.ok("Usuario eliminado exitosamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
    
    @PutMapping("/suspender/{id}")
    public ResponseEntity<?> suspenderUsuario(@PathVariable Long id) {
        try {
            Usuario usuarioActualizado = usuarioService.suspenderUsuario(id);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/email")
    public ResponseEntity<Object> updateEmail(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String newEmail = requestBody.get("newEmail");

        if (newEmail == null || !newEmail.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Correo electrónico no válido."));
        }

        try {
            usuarioService.updateUserEmail(id, newEmail);
            return ResponseEntity.ok(Map.of("message", "Correo electrónico actualizado correctamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }
    
    /**
     * MODIFICADO: Acepta múltiples imágenes para un registro facial más robusto.
     * El frontend debe enviar los archivos bajo el nombre "files".
     */
    @PostMapping("/{id}/registrar-rostro")
    public ResponseEntity<?> registrarRostro(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            return ResponseEntity.badRequest().body("Debe proporcionar al menos una imagen.");
        }
        
        try {
            usuarioService.registrarRostro(id, files);
            return ResponseEntity.ok(Map.of("message", "Rostro(s) registrado(s) exitosamente."));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al leer las imágenes.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}