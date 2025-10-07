/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data; 
import java.time.LocalDateTime;

/**
 *
 * @author perez
 */
@Entity
@Table(name = "usuarios")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(nullable = false, length = 50)
    private String primerNombre;

    @Column(nullable = false, length = 50)
    private String primerApellido;

    @Column(nullable = false, unique = true, length = 20)
    private String carnet; 
    
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false, length = 255)
    private String contrasena;
    
    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @ManyToOne(optional = true)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @ManyToOne(optional = true)
    @JoinColumn(name = "grado_id")
    private Grado grado;

    private LocalDateTime fechaRegistro = LocalDateTime.now();
    
    @Column(name = "face_id", length = 255) 
    private String faceId;
}