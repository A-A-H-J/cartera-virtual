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
import sv.org.arrupe.pagos.model.Cartera; // Importa el modelo Cartera
import sv.org.arrupe.pagos.repository.UsuarioRepository;
import sv.org.arrupe.pagos.repository.CarteraRepository; // Importa el repositorio de Cartera
import java.util.*;

/**
 *
 * @author perez
 */
@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CarteraRepository carteraRepository; // Inyecta el repositorio de cartera

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional // Asegura que ambas operaciones se completen o fallen juntas
    public Usuario saveUser(Usuario usuario) {
        // Encripta la contraseña antes de guardarla
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        
        // 1. Guardar el usuario primero para obtener su ID
        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        
        // 2. Crear una cartera asociada al nuevo usuario
        Cartera nuevaCartera = new Cartera();
        nuevaCartera.setAlumno(nuevoUsuario); // Relaciona la cartera con el nuevo usuario usando el campo "alumno"
        nuevaCartera.setSaldo(0.00); // Establece el saldo inicial en 0
        
        // 3. Guardar la nueva cartera
        carteraRepository.save(nuevaCartera);
        
        return nuevoUsuario;
    }
    
    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }
    
    public Usuario getById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }
    
    // Método para eliminar un usuario por su ID
    public void deleteById(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario != null && usuario.getRol().getIdRol() == 2) {
            throw new IllegalArgumentException("No se puede eliminar al usuario administrador.");
        }
        usuarioRepository.deleteById(id);
    }
    
    public Usuario suspenderUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        
        // No permitir suspender al administrador
        if (usuario.getRol().getIdRol() == 2) {
            throw new IllegalArgumentException("No se puede suspender al usuario administrador.");
        }
        
        usuario.setActivo(!usuario.isActivo()); // Cambia el estado de activo a inactivo y viceversa
        return usuarioRepository.save(usuario);
    }
    
    public void updateUserEmail(Long id, String newEmail) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);

        if (usuarioOptional.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOptional.get();
        usuario.setCorreo(newEmail);
        usuarioRepository.save(usuario);
    }
}
