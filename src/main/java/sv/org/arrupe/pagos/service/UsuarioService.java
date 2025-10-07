/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.org.arrupe.pagos.model.Usuario;
import sv.org.arrupe.pagos.model.Cartera; 
import sv.org.arrupe.pagos.repository.UsuarioRepository;
import sv.org.arrupe.pagos.repository.CarteraRepository; 
import java.util.*;
import sv.org.arrupe.pagos.model.Rol;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author perez
 */
@Service
public class UsuarioService {
    
    @Autowired
    private RekognitionService rekognitionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CarteraRepository carteraRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // --- Integración del servicio de Email ---
    @Autowired
    private EmailService emailService;

    @Value("${admin.email}")
    private String adminEmail;
    // -----------------------------------------

    @Transactional
    public Usuario saveUser(Usuario usuario) {
        // Encripta la contraseña antes de guardarla
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        
        // 1. Guardar el usuario primero para obtener su ID
        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        
        // 2. Crear una cartera asociada al nuevo usuario
        Cartera nuevaCartera = new Cartera();
        nuevaCartera.setAlumno(nuevoUsuario);
        nuevaCartera.setSaldo(0.00);
        
        // 3. Guardar la nueva cartera
        carteraRepository.save(nuevaCartera);

        // --- ¡ENVÍO DE NOTIFICACIONES POR CORREO! ---
        // Notificación de bienvenida para el nuevo usuario
        String subjectBienvenida = "¡Bienvenido a Cartera Virtual MarketCup!";
        String textBienvenida = "Hola " + nuevoUsuario.getPrimerNombre() + ",\n\n" +
                                "Tu cuenta ha sido creada exitosamente. ¡Ya puedes empezar a usar tu cartera virtual en nuestra plataforma!";
        emailService.sendSimpleMessage(nuevoUsuario.getCorreo(), subjectBienvenida, textBienvenida);

        // Notificación para el administrador
        String subjectAdmin = "Nuevo Usuario Registrado en MarketCup";
        String textAdmin = "Se ha registrado un nuevo usuario en la plataforma:\n\n" +
                           "Nombre: " + nuevoUsuario.getPrimerNombre() + " " + nuevoUsuario.getPrimerApellido() + "\n" +
                           "Correo: " + nuevoUsuario.getCorreo() + "\n" +
                           "Carné: " + (nuevoUsuario.getCarnet() != null ? nuevoUsuario.getCarnet() : "N/A");
        emailService.sendSimpleMessage(adminEmail, subjectAdmin, textAdmin);
        
        return nuevoUsuario;
    }
    
    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }
    
    public Usuario getById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }
    
    public void deleteById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (usuario.getRol().getIdRol() == 2) {
            throw new IllegalArgumentException("No se puede eliminar al usuario administrador.");
        }
        usuarioRepository.deleteById(id);
    }
    
    public Usuario suspenderUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        
        if (usuario.getRol().getIdRol() == 2) {
            throw new IllegalArgumentException("No se puede suspender al usuario administrador.");
        }
        
        usuario.setActivo(!usuario.isActivo());
        return usuarioRepository.save(usuario);
    }
    
    public void updateUserEmail(Long id, String newEmail) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        
        usuario.setCorreo(newEmail);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario processOAuthPostLogin(String email, String nombre) {
        // Usamos una sintaxis más moderna y segura con orElseGet
        return usuarioRepository.findByCorreo(email).orElseGet(() -> {
            // Si el usuario no existe, se ejecuta este código para crearlo
            Usuario newUser = new Usuario();
            newUser.setCorreo(email);
            
            String[] names = nombre.split(" ", 2);
            newUser.setPrimerNombre(names[0]);
            newUser.setPrimerApellido(names.length > 1 ? names[1] : " ");
            
            newUser.setCarnet("G-" + System.currentTimeMillis()); // Carnet temporal
            newUser.setContrasena(passwordEncoder.encode("google-provided-password"));
            
            Rol estudianteRol = new Rol();
            estudianteRol.setIdRol(1L); // Asumiendo que 1L es el ID del rol ESTUDIANTE
            newUser.setRol(estudianteRol);
            
            // Reutilizamos el método saveUser que ya crea la cartera y envía los correos
            return this.saveUser(newUser);
        });
    }
    
    @Transactional
    public Usuario registrarRostro(Long usuarioId, byte[] imageBytes) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        String faceId = rekognitionService.indexFace(imageBytes);
        usuario.setFaceId(faceId);

        return usuarioRepository.save(usuario);
    }
}
