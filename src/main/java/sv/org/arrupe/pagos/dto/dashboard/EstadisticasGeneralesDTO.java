/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.dto.dashboard;

import lombok.Data;
/**
 *
 * @author perez
 */
@Data
public class EstadisticasGeneralesDTO {
    private long totalUsuariosActivos;
    private double ingresosTotales;
    private long transaccionesHoy;
    private long nuevosUsuariosMes;
}
