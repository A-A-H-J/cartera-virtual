/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.org.arrupe.pagos.dto.PaymentRequest;
import sv.org.arrupe.pagos.model.Usuario;
import sv.org.arrupe.pagos.repository.UsuarioRepository;
import sv.org.arrupe.pagos.service.QrCodeService;
import sv.org.arrupe.pagos.service.TransaccionService;

/**
 *
 * @author perez
 */
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
            
            // El QR contendrá el carnet del usuario
            String qrContent = usuario.getCarnet();
            
            byte[] qrImage = qrCodeService.generateQrCodeImage(qrContent, 250, 250);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            transaccionService.realizarPagoEntreUsuarios(
                paymentRequest.getCarnetBeneficiario(),
                paymentRequest.getIdPagador(),
                paymentRequest.getMonto(),
                paymentRequest.getDescripcion()
            );
            return ResponseEntity.ok().body("Pago realizado con éxito.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
