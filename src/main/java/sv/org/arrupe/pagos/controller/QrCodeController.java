package sv.org.arrupe.pagos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sv.org.arrupe.pagos.dto.PaymentRequest;
import sv.org.arrupe.pagos.model.Usuario;
import sv.org.arrupe.pagos.repository.UsuarioRepository;
import sv.org.arrupe.pagos.service.QrCodeService;
import sv.org.arrupe.pagos.service.TransaccionService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/qr")
@CrossOrigin(origins = "http://localhost:5173")
public class QrCodeController {

    @Autowired
    private QrCodeService qrCodeService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransaccionService transaccionService;

    @GetMapping(value = "/generate/{userId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCode(@PathVariable Long userId) {
        try {
            Usuario usuario = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // El QR contendrá el carnet del usuario para ser escaneado
            String qrContent = usuario.getCarnet();
            
            byte[] qrImage = qrCodeService.generateQrCodeImage(qrContent, 250, 250);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para que un VENDEDOR solicite un pago a un CLIENTE.
     * Reemplaza el antiguo "process-payment".
     */
    @PostMapping("/request-payment")
    public ResponseEntity<?> requestPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            // idPagador aquí es el Vendedor, carnetBeneficiario es el Cliente.
            transaccionService.solicitarPago(
                paymentRequest.getCarnetBeneficiario(),
                paymentRequest.getIdPagador(),
                paymentRequest.getMonto(),
                paymentRequest.getDescripcion()
            );
            return ResponseEntity.ok(Map.of("message", "Solicitud de pago enviada con éxito."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint para que un CLIENTE confirme un pago pendiente usando su rostro.
     */
    @PostMapping("/confirm-payment/{transactionId}")
    public ResponseEntity<?> confirmPayment(@PathVariable Long transactionId, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("El archivo de imagen no puede estar vacío.");
            }
            byte[] rostroClienteBytes = file.getBytes();
            transaccionService.confirmarPago(transactionId, rostroClienteBytes);
            return ResponseEntity.ok(Map.of("message", "Pago confirmado y procesado exitosamente."));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la imagen.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}