/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author perez
 */
@Data
@AllArgsConstructor
public class RegistrosMensualesDTO {
    private String mes;
    private long cantidad;
}
