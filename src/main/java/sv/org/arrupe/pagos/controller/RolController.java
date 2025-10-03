/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import sv.org.arrupe.pagos.model.Rol;
import sv.org.arrupe.pagos.repository.RolRepository;
/**
 *
 * @author perez
 */
@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final RolRepository repo;

    public RolController(RolRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Rol> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Rol getById(@PathVariable Long id) {
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Rol create(@RequestBody Rol rol) {
        return repo.save(rol);
    }

    @PutMapping("/{id}")
    public Rol update(@PathVariable Long id, @RequestBody Rol rol) {
        rol.setIdRol(id);
        return repo.save(rol);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
