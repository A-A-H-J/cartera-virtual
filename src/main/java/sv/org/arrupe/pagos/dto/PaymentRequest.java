/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.dto;

import lombok.Data;
/**
 *
 * @author perez
 */
@Data
public class PaymentRequest {
    private String carnetBeneficiario; 
    private Long idPagador;           
    private Double monto;
    private String descripcion;
}
