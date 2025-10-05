/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.org.arrupe.pagos.dto.dashboard.EstadisticasGeneralesDTO;
import sv.org.arrupe.pagos.dto.dashboard.FlujoDiarioDTO;
import sv.org.arrupe.pagos.dto.dashboard.RegistrosMensualesDTO;
import sv.org.arrupe.pagos.service.DashboardService;

import java.util.List;
/**
 *
 * @author perez
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/estadisticas-generales")
    public ResponseEntity<EstadisticasGeneralesDTO> getEstadisticasGenerales() {
        return ResponseEntity.ok(dashboardService.getEstadisticasGenerales());
    }

    @GetMapping("/flujo-ultimos-7-dias")
    public ResponseEntity<List<FlujoDiarioDTO>> getFlujoUltimos7Dias() {
        return ResponseEntity.ok(dashboardService.getFlujoUltimos7Dias());
    }

    @GetMapping("/registros-por-mes")
    public ResponseEntity<List<RegistrosMensualesDTO>> getRegistrosPorMes() {
        return ResponseEntity.ok(dashboardService.getRegistrosPorMes());
    }
}
