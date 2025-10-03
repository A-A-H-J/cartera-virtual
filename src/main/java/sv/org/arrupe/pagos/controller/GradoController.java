/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.org.arrupe.pagos.model.Grado;
import sv.org.arrupe.pagos.service.GradoService;

import java.util.List;
/**
 *
 * @author perez
 */
@RestController
@RequestMapping("/api/grados")
@CrossOrigin(origins = "http://localhost:5173")
public class GradoController {

    @Autowired
    private GradoService gradoService;

    @GetMapping
    public List<Grado> getAll() {
        return gradoService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grado> getById(@PathVariable Long id) {
        return gradoService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Grado grado) {
        try {
            Grado newGrado = gradoService.create(grado);
            return ResponseEntity.status(HttpStatus.CREATED).body(newGrado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Grado grado) {
        try {
            Grado updatedGrado = gradoService.update(id, grado);
            return ResponseEntity.ok(updatedGrado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        gradoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}