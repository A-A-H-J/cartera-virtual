/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

/**
 *
 * @author perez
 */
@Entity
@Table(name = "cartera")
@Data 
public class Cartera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCartera;

    @OneToOne
    @JoinColumn(name = "alumno_id", nullable = false)
    private Usuario alumno;

    private Double saldo = 0.0;

    private LocalDateTime fechaActualizacion = LocalDateTime.now();
}