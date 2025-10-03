/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sv.org.arrupe.pagos.model.Usuario;
import sv.org.arrupe.pagos.repository.UsuarioRepository;

/**
 *
 * @author perez
 */
@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Usuario authenticate(String correo, String contrasena) {
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        // Verifica si el usuario existe, la contraseña es correcta Y si la cuenta está activa
        if (usuario != null && passwordEncoder.matches(contrasena, usuario.getContrasena()) && usuario.isActivo()) {
            return usuario;
        }
        return null; 
    }
}