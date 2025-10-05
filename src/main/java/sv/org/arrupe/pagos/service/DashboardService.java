package sv.org.arrupe.pagos.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import sv.org.arrupe.pagos.dto.dashboard.EstadisticasGeneralesDTO;
import sv.org.arrupe.pagos.dto.dashboard.FlujoDiarioDTO;
import sv.org.arrupe.pagos.dto.dashboard.RegistrosMensualesDTO;

import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    public EstadisticasGeneralesDTO getEstadisticasGenerales() {
        EstadisticasGeneralesDTO dto = new EstadisticasGeneralesDTO();

        Query qUsuarios = entityManager.createNativeQuery("SELECT COUNT(*) FROM usuarios WHERE activo = TRUE AND rol_id = 1");
        dto.setTotalUsuariosActivos(((Number) qUsuarios.getSingleResult()).longValue());

        Query qIngresos = entityManager.createNativeQuery("SELECT COALESCE(SUM(monto), 0) FROM transacciones WHERE tipo = 'RECARGA'");
        dto.setIngresosTotales(((Number) qIngresos.getSingleResult()).doubleValue());

        Query qTransacciones = entityManager.createNativeQuery("SELECT COUNT(*) FROM transacciones WHERE DATE(fecha) = CURDATE()");
        dto.setTransaccionesHoy(((Number) qTransacciones.getSingleResult()).longValue());

        Query qNuevosUsuarios = entityManager.createNativeQuery("SELECT COUNT(*) FROM usuarios WHERE YEAR(fecha_registro) = YEAR(CURDATE()) AND MONTH(fecha_registro) = MONTH(CURDATE())");
        dto.setNuevosUsuariosMes(((Number) qNuevosUsuarios.getSingleResult()).longValue());

        return dto;
    }

    public List<FlujoDiarioDTO> getFlujoUltimos7Dias() {
        String sql = "SELECT CAST(d.fecha AS CHAR), COALESCE(ingresos, 0), COALESCE(gastos, 0) FROM " +
                     "(SELECT CURDATE() - INTERVAL (a.a + (10 * b.a) + (100 * c.a)) DAY as fecha " +
                     "FROM (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6) AS a " +
                     "CROSS JOIN (SELECT 0 AS a UNION ALL SELECT 1) AS b " +
                     "CROSS JOIN (SELECT 0 AS a UNION ALL SELECT 1) AS c) d " +
                     "LEFT JOIN (SELECT DATE(fecha) as dia, SUM(monto) as ingresos FROM transacciones WHERE tipo = 'RECARGA' GROUP BY dia) i ON d.fecha = i.dia " +
                     "LEFT JOIN (SELECT DATE(fecha) as dia, SUM(monto) as gastos FROM transacciones WHERE tipo = 'PAGO' GROUP BY dia) g ON d.fecha = g.dia " +
                     "WHERE d.fecha BETWEEN CURDATE() - INTERVAL 6 DAY AND CURDATE() ORDER BY d.fecha ASC";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> new FlujoDiarioDTO((String) row[0], ((BigDecimal) row[1]).doubleValue(), ((BigDecimal) row[2]).doubleValue()))
                .collect(Collectors.toList());
    }

    public List<RegistrosMensualesDTO> getRegistrosPorMes() {
        String sql = "SELECT MONTH(fecha_registro) as mes_num, COUNT(*) as cantidad FROM usuarios " +
                     "WHERE YEAR(fecha_registro) = YEAR(CURDATE()) GROUP BY MONTH(fecha_registro) ORDER BY mes_num ASC";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(row -> {
                    String monthName = java.time.Month.of((Integer) row[0]).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                    monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
                    
                    // --- ¡LA CORRECCIÓN ESTÁ AQUÍ! ---
                    // En lugar de castear a BigInteger, casteamos a Number, que es más genérico.
                    long cantidad = ((Number) row[1]).longValue();
                    
                    return new RegistrosMensualesDTO(monthName, cantidad);
                })
                .collect(Collectors.toList());
    }
}