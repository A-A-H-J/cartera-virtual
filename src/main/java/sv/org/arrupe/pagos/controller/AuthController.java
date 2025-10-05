/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.org.arrupe.pagos.dto.AuthenticationResponse;
import sv.org.arrupe.pagos.dto.LoginRequest;
import sv.org.arrupe.pagos.model.Usuario;
import sv.org.arrupe.pagos.service.AuthService;
import sv.org.arrupe.pagos.util.JwtUtil;


/**
 *
 * @author perez
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") 
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Usuario usuario = authService.authenticate(loginRequest.getCorreo(), loginRequest.getContrasena());
        if (usuario != null) {
            final String jwt = jwtUtil.generateToken(usuario);
            return ResponseEntity.ok(new AuthenticationResponse(jwt));
        }
        return ResponseEntity.status(401).body("Credenciales incorrectas o cuenta inactiva");
    }
}