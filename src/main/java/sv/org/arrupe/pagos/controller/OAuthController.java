/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import sv.org.arrupe.pagos.model.Usuario;
import sv.org.arrupe.pagos.service.UsuarioService;
import sv.org.arrupe.pagos.util.JwtUtil; 

import java.io.IOException;
/**
 *
 * @author perez
 */
@RestController
public class OAuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil; // ¡Inyectar JwtUtil!

    @GetMapping("/login/success")
    public void getLoginInfo(@AuthenticationPrincipal OAuth2User principal, HttpServletResponse response) throws IOException {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");

        // Buscamos o creamos el usuario
        Usuario usuario = usuarioService.processOAuthPostLogin(email, name);

        // Generamos un token JWT para este usuario
        String token = jwtUtil.generateToken(usuario);

        // Redirigimos al frontend con el token en la URL
        String targetUrl = "http://localhost:5173/oauth/callback?token=" + token;
        
        response.sendRedirect(targetUrl);
    }
}