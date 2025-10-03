/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import sv.org.arrupe.pagos.model.Transaccion;
import sv.org.arrupe.pagos.repository.TransaccionRepository;
import sv.org.arrupe.pagos.service.TransaccionService; 

/**
 *
 * @author perez
 */
@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionRepository transaccionRepo;
    private final TransaccionService transaccionService; 

    public TransaccionController(TransaccionRepository transaccionRepo, TransaccionService transaccionService) {
        this.transaccionRepo = transaccionRepo;
        this.transaccionService = transaccionService;
    }

    @GetMapping
    public List<Transaccion> getAll() {
        return transaccionRepo.findAll();
    }

    @GetMapping("/{id}")
    public Transaccion getById(@PathVariable Long id) {
        return transaccionRepo.findById(id).orElse(null);
    }

    @PostMapping
    public Transaccion create(@RequestBody Transaccion transaccion) {
        // Usa el servicio para manejar la lógica de negocio
        return transaccionService.realizarTransaccion(transaccion);
    }
    
    @PutMapping("/{id}")
    public Transaccion update(@PathVariable Long id, @RequestBody Transaccion transaccion) {
        transaccion.setIdTransaccion(id);
        return transaccionRepo.save(transaccion);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        transaccionRepo.deleteById(id);
    }
}