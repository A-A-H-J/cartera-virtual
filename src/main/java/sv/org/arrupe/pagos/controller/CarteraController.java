/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import sv.org.arrupe.pagos.model.Cartera;
import sv.org.arrupe.pagos.repository.CarteraRepository;

/**
 *
 * @author perez
 */
@RestController
@RequestMapping("/api/carteras")
public class CarteraController {

    private final CarteraRepository repo;

    public CarteraController(CarteraRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Cartera> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Cartera getById(@PathVariable Long id) {
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Cartera create(@RequestBody Cartera cartera) {
        return repo.save(cartera);
    }

    @PutMapping("/{id}")
    public Cartera update(@PathVariable Long id, @RequestBody Cartera cartera) {
        cartera.setIdCartera(id);
        return repo.save(cartera);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
    
    @GetMapping("/usuario/{alumnoId}") 
    public Cartera getByAlumnoId(@PathVariable Long alumnoId) {
        return repo.findByAlumnoIdUsuario(alumnoId).orElse(null); 
    }
}