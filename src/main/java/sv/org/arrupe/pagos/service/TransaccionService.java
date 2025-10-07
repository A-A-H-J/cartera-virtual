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

import java.util.List;

@Service
public class TransaccionService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private CarteraRepository carteraRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RekognitionService rekognitionService; // Inyecta el servicio de Rekognition

    /**
     * Procesa una transacción simple como una recarga directa.
     * @param transaccion La transacción a procesar.
     * @return La transacción guardada.
     */
    @Transactional
    public Transaccion realizarTransaccion(Transaccion transaccion) {
        Cartera cartera = carteraRepository.findById(transaccion.getCartera().getIdCartera())
                                          .orElseThrow(() -> new RuntimeException("Cartera no encontrada"));
        
        Usuario usuario = usuarioRepository.findById(transaccion.getRealizadoPor().getIdUsuario())
                                           .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        transaccion.setCartera(cartera);
        transaccion.setRealizadoPor(usuario);

        if (transaccion.getTipo() == TipoTransaccion.RECARGA) {
            cartera.setSaldo(cartera.getSaldo() + transaccion.getMonto());
        } else if (transaccion.getTipo() == TipoTransaccion.PAGO) {
            if (cartera.getSaldo() < transaccion.getMonto()) {
                throw new RuntimeException("Saldo insuficiente.");
            }
            cartera.setSaldo(cartera.getSaldo() - transaccion.getMonto());
        }
        
        carteraRepository.save(cartera);
        return transaccionRepository.save(transaccion);
    }
    
    /**
     * Crea una solicitud de pago de un vendedor a un cliente, dejándola en estado pendiente.
     * @param carnetCliente Carnet del cliente al que se le cobrará.
     * @param idVendedor ID del vendedor que está solicitando el pago.
     * @param monto El monto a cobrar.
     * @param descripcion Descripción de la venta o cobro.
     * @return La transacción en estado PAGO_PENDIENTE.
     */
    @Transactional
    public Transaccion solicitarPago(String carnetCliente, Long idVendedor, Double monto, String descripcion) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo.");
        }

        Usuario cliente = usuarioRepository.findByCarnet(carnetCliente);
        if (cliente == null) {
            throw new RuntimeException("Cliente con carnet '" + carnetCliente + "' no encontrado.");
        }
        
        Usuario vendedor = usuarioRepository.findById(idVendedor)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado."));

        Cartera carteraCliente = carteraRepository.findByAlumnoIdUsuario(cliente.getIdUsuario())
            .orElseThrow(() -> new RuntimeException("La cartera del cliente no fue encontrada."));

        Transaccion solicitud = new Transaccion();
        solicitud.setCartera(carteraCliente); // La transacción se asocia a quien debe pagar
        solicitud.setRealizadoPor(vendedor);  // Guarda quién está solicitando el cobro
        solicitud.setTipo(TipoTransaccion.PAGO_PENDIENTE);
        solicitud.setMonto(monto);
        solicitud.setDescripcion(descripcion != null ? descripcion : "Solicitud de pago");

        return transaccionRepository.save(solicitud);
    }

    /**
     * Confirma un pago pendiente después de una verificación facial exitosa.
     * @param idTransaccion ID de la transacción pendiente.
     * @param rostroClienteBytes Imagen del rostro del cliente para verificación.
     * @return La transacción de RECARGA generada para el vendedor.
     */
    @Transactional
    public Transaccion confirmarPago(Long idTransaccion, byte[] rostroClienteBytes) {
        // 1. Validar la transacción pendiente
        Transaccion transaccionPendiente = transaccionRepository.findById(idTransaccion)
                .filter(t -> t.getTipo() == TipoTransaccion.PAGO_PENDIENTE)
                .orElseThrow(() -> new RuntimeException("Solicitud de pago no encontrada o ya procesada."));

        Usuario cliente = transaccionPendiente.getCartera().getAlumno();
        Usuario vendedor = transaccionPendiente.getRealizadoPor();
        Double monto = transaccionPendiente.getMonto();

        // 2. Verificación Facial con AWS Rekognition
        String faceIdReferencia = cliente.getFaceId();
        if (faceIdReferencia == null || faceIdReferencia.isEmpty()) {
            throw new RuntimeException("El cliente no tiene un rostro de referencia registrado para la verificación.");
        }

        String faceIdDetectado = rekognitionService.searchFaceByImage(rostroClienteBytes);
        if (faceIdDetectado == null || !faceIdReferencia.equals(faceIdDetectado)) {
            throw new RuntimeException("Verificación facial fallida. No se pudo confirmar la identidad.");
        }

        // 3. Si la verificación es exitosa, ejecutar el pago
        Cartera carteraCliente = transaccionPendiente.getCartera();
        if (carteraCliente.getSaldo() < monto) {
            throw new RuntimeException("Saldo insuficiente para completar el pago.");
        }
        Cartera carteraVendedor = carteraRepository.findByAlumnoIdUsuario(vendedor.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Cartera del vendedor no encontrada."));

        // 4. Actualizar saldos
        carteraCliente.setSaldo(carteraCliente.getSaldo() - monto);
        carteraVendedor.setSaldo(carteraVendedor.getSaldo() + monto);

        carteraRepository.save(carteraCliente);
        carteraRepository.save(carteraVendedor);

        // 5. Actualizar la transacción original de "PENDIENTE" a "PAGO"
        transaccionPendiente.setTipo(TipoTransaccion.PAGO);
        transaccionPendiente.setDescripcion("Pago a " + vendedor.getPrimerNombre() + " (" + transaccionPendiente.getDescripcion() + ")");
        transaccionRepository.save(transaccionPendiente);

        // 6. Registrar la transacción de RECARGA para el vendedor (beneficiario)
        Transaccion transaccionRecarga = new Transaccion();
        transaccionRecarga.setCartera(carteraVendedor);
        transaccionRecarga.setTipo(TipoTransaccion.RECARGA);
        transaccionRecarga.setMonto(monto);
        transaccionRecarga.setDescripcion("Recibido de: " + cliente.getPrimerNombre() + " " + cliente.getPrimerApellido());
        transaccionRecarga.setRealizadoPor(cliente); // El cliente es el originador del fondo

        return transaccionRepository.save(transaccionRecarga);
    }
    
    /**
     * Obtiene todas las transacciones pendientes de un usuario específico.
     * @param idUsuario El ID del usuario (cliente) que debe aprobar los pagos.
     * @return Una lista de transacciones con estado PAGO_PENDIENTE.
     */
    public List<Transaccion> getPagosPendientes(Long idUsuario) {
        Cartera cartera = carteraRepository.findByAlumnoIdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Cartera del usuario no encontrada."));
        return transaccionRepository.findByCarteraIdCarteraAndTipo(cartera.getIdCartera(), TipoTransaccion.PAGO_PENDIENTE);
    }
}