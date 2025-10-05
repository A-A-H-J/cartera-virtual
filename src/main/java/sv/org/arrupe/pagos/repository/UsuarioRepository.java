/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sv.org.arrupe.pagos.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.org.arrupe.pagos.model.Usuario;

/**
 *
 * @author perez
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByCarnet(String carnet);
    Optional<Usuario> findByCorreo(String correo);
}
