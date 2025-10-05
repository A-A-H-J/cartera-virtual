/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sv.org.arrupe.pagos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.org.arrupe.pagos.model.Transaccion;
/**
 *
 * @author perez
 */
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    List<Transaccion> findByCarteraIdCartera(Long carteraId);
}
