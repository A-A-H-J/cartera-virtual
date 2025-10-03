/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.org.arrupe.pagos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sv.org.arrupe.pagos.model.Grado;
import sv.org.arrupe.pagos.repository.GradoRepository;

import java.util.List;
import java.util.Optional;
/**
 *
 * @author perez
 */
@Service
public class GradoService {

    @Autowired
    private GradoRepository gradoRepository;

    public List<Grado> getAll() {
        return gradoRepository.findAll();
    }

    public Grado create(Grado grado) {
        String nombreNormalizado = grado.getNombre().trim().toLowerCase();
        Optional<Grado> existingGrado = gradoRepository.findByNombreIgnoreCase(nombreNormalizado);

        if (existingGrado.isPresent()) {
            throw new IllegalArgumentException("Ya existe un grado con este nombre.");
        }
        
        grado.setNombre(nombreNormalizado);
        return gradoRepository.save(grado);
    }
    
    public Optional<Grado> getById(Long id) {
        return gradoRepository.findById(id);
    }

    public Grado update(Long id, Grado gradoDetails) {
        Grado grado = gradoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grado no encontrado con el ID: " + id));

        // Normaliza el nuevo nombre para la validación
        String nuevoNombreNormalizado = gradoDetails.getNombre().trim().toLowerCase();

        // Si el nombre ha cambiado, verifica que no sea un duplicado
        if (!grado.getNombre().equalsIgnoreCase(nuevoNombreNormalizado)) {
            Optional<Grado> existingGrado = gradoRepository.findByNombreIgnoreCase(nuevoNombreNormalizado);
            if (existingGrado.isPresent()) {
                throw new IllegalArgumentException("Ya existe un grado con este nombre.");
            }
        }
        
        // Actualiza el nombre del grado y guarda
        grado.setNombre(nuevoNombreNormalizado);
        return gradoRepository.save(grado);
    }

    public void delete(Long id) {
        gradoRepository.deleteById(id);
    }
}