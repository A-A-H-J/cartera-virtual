/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.model;

import jakarta.persistence.*;
import lombok.Data; 
import java.time.LocalDateTime;
/**
 *
 * @author perez
 */
@Entity
@Table(name = "transacciones")
@Data // Genera automáticamente getters, setters, toString, equals y hashCode
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransaccion;

    @ManyToOne
    @JoinColumn(name = "cartera_id", nullable = false)
    private Cartera cartera;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransaccion tipo;

    private Double monto;

    private LocalDateTime fecha = LocalDateTime.now();

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "realizado_por_id", nullable = false)
    private Usuario realizadoPor;
}