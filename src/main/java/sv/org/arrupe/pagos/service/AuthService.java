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

import java.util.Optional; 
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
        // Buscamos el usuario y obtenemos un Optional
        Optional<Usuario> usuarioOptional = usuarioRepository.findByCorreo(correo);

        // Si el usuario existe, procedemos a verificar la contraseña y el estado
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            if (passwordEncoder.matches(contrasena, usuario.getContrasena()) && usuario.isActivo()) {
                return usuario;
            }
        }
        
        // Si no se encuentra el usuario o la contraseña no coincide, devolvemos null
        return null; 
    }
}