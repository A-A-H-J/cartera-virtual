/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.org.arrupe.pagos.model.Cartera;
import sv.org.arrupe.pagos.model.Transaccion;
import sv.org.arrupe.pagos.model.Usuario;
import sv.org.arrupe.pagos.repository.CarteraRepository;
import sv.org.arrupe.pagos.repository.TransaccionRepository;
import sv.org.arrupe.pagos.repository.UsuarioRepository;

/**
 *
 * @author perez
 */
@Service
public class TransaccionService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private CarteraRepository carteraRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Añade el repositorio de usuarios

    @Transactional
    public Transaccion realizarTransaccion(Transaccion transaccion) {
        // 1. Busca la cartera y el usuario por sus IDs
        Cartera cartera = carteraRepository.findById(transaccion.getCartera().getIdCartera())
                                          .orElseThrow(() -> new RuntimeException("Cartera no encontrada"));
        
        Usuario usuario = usuarioRepository.findById(transaccion.getRealizadoPor().getIdUsuario())
                                           .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // 2. Establece los objetos completos en la transacción antes de guardar
        transaccion.setCartera(cartera);
        transaccion.setRealizadoPor(usuario);

        // 3. Verifica el tipo de transacción y actualiza el saldo
        if ("RECARGA".equalsIgnoreCase(transaccion.getTipo().name()) || "reca".equalsIgnoreCase(transaccion.getTipo().name())) {
            cartera.setSaldo(cartera.getSaldo() + transaccion.getMonto());
        } else if ("PAGO".equalsIgnoreCase(transaccion.getTipo().name())) {
            cartera.setSaldo(cartera.getSaldo() - transaccion.getMonto());
        }
        
        // 4. Guarda la cartera actualizada
        carteraRepository.save(cartera);
        
        // 5. Guarda la transacción
        return transaccionRepository.save(transaccion);
    }
}