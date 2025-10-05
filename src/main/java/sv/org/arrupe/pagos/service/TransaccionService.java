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
import sv.org.arrupe.pagos.model.TipoTransaccion;

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
    
    @Transactional
    public Transaccion realizarPagoEntreUsuarios(String carnetBeneficiario, Long idPagador, Double monto, String descripcion) {
        // 1. Validar que el monto sea positivo
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo.");
        }

        // 2. Buscar al pagador y su cartera
        Usuario pagador = usuarioRepository.findById(idPagador)
                .orElseThrow(() -> new RuntimeException("Usuario pagador no encontrado."));
        Cartera carteraPagador = carteraRepository.findByAlumnoIdUsuario(pagador.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Cartera del pagador no encontrada."));

        // 3. Verificar si el pagador tiene saldo suficiente
        if (carteraPagador.getSaldo() < monto) {
            throw new RuntimeException("Saldo insuficiente para realizar el pago.");
        }

        // 4. Buscar al beneficiario y su cartera
        Usuario beneficiario = usuarioRepository.findByCarnet(carnetBeneficiario);
        if (beneficiario == null) {
            throw new RuntimeException("Usuario beneficiario no encontrado.");
        }
        Cartera carteraBeneficiario = carteraRepository.findByAlumnoIdUsuario(beneficiario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Cartera del beneficiario no encontrada."));

        // 5. Actualizar saldos
        carteraPagador.setSaldo(carteraPagador.getSaldo() - monto);
        carteraBeneficiario.setSaldo(carteraBeneficiario.getSaldo() + monto);

        carteraRepository.save(carteraPagador);
        carteraRepository.save(carteraBeneficiario);

        // 6. Registrar la transacción de PAGO
        Transaccion transaccionPago = new Transaccion();
        transaccionPago.setCartera(carteraPagador);
        transaccionPago.setTipo(TipoTransaccion.PAGO);
        transaccionPago.setMonto(monto);
        transaccionPago.setDescripcion(descripcion);
        transaccionPago.setRealizadoPor(pagador);
        transaccionRepository.save(transaccionPago);
        
        // 7. Registrar la transacción de RECARGA para el beneficiario
        Transaccion transaccionRecarga = new Transaccion();
        transaccionRecarga.setCartera(carteraBeneficiario);
        transaccionRecarga.setTipo(TipoTransaccion.RECARGA);
        transaccionRecarga.setMonto(monto);
        transaccionRecarga.setDescripcion("Recibido de: " + pagador.getPrimerNombre());
        transaccionRecarga.setRealizadoPor(pagador); // La acción la sigue realizando el pagador

        return transaccionRepository.save(transaccionRecarga);
    }
}