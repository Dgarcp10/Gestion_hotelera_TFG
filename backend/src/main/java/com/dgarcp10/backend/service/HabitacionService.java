package com.dgarcp10.backend.service;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.dgarcp10.backend.model.EstadoHabitacion;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.TipoHabitacionRepository;

@Service
public class HabitacionService {
    private final HabitacionRepository habitacionRepo;
    private final TipoHabitacionRepository tipoHabitacionRepo;
    public HabitacionService(HabitacionRepository habitacionRepo, TipoHabitacionRepository tipoHabitacionRepo) {
        this.habitacionRepo = habitacionRepo;
        this.tipoHabitacionRepo = tipoHabitacionRepo;
    }
    public List<Habitacion> listarTodos() {
        return habitacionRepo.findAll();
    }
    public List<Habitacion> listarPorTipo(Long tipoId) {
        return habitacionRepo.findByTipoHabitacionId(tipoId);
    }
    public Habitacion obtenerPorId(Long id) {
        return habitacionRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Habitación no encontrada: " + id));
    }
    public Habitacion crear(Habitacion habitacion) {
        if (habitacion.getNumero().toString().isBlank()) {
            throw new IllegalArgumentException("El número de habitación no puede estar vacío");
        }
        if (habitacionRepo.findByNumero(habitacion.getNumero()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una habitación con el número " + habitacion.getNumero());
        }
        if (habitacion.getTipoHabitacion() == null || habitacion.getTipoHabitacion().getId() == null) {
            throw new IllegalArgumentException("Debe especificar un tipo de habitación");
        }
        TipoHabitacion tipo = tipoHabitacionRepo.findById(habitacion.getTipoHabitacion().getId())
            .orElseThrow(() -> new NoSuchElementException("Tipo de habitación no encontrado"));
        habitacion.setTipoHabitacion(tipo);
        habitacion.setEstado(EstadoHabitacion.LIBRE);
        if (habitacion.getPendienteLimpieza() == null) habitacion.setPendienteLimpieza(false);
        if (habitacion.getAveriada() == null) habitacion.setAveriada(false);
        return habitacionRepo.save(habitacion);
    }
    public Habitacion actualizar(Long id, Habitacion datos) {
        Habitacion existente = obtenerPorId(id);
        if (datos.getNumero() != null && !datos.getNumero().equals(existente.getNumero())) {
            if (habitacionRepo.findByNumero(datos.getNumero()).isPresent()) {
                throw new IllegalArgumentException("Ya existe una habitación con el número " + datos.getNumero());
            }
            existente.setNumero(datos.getNumero());
        }
        if (datos.getTipoHabitacion() != null && datos.getTipoHabitacion().getId() != null) {
            TipoHabitacion tipo = tipoHabitacionRepo.findById(datos.getTipoHabitacion().getId())
                .orElseThrow(() -> new NoSuchElementException("Tipo de habitación no encontrado"));
            existente.setTipoHabitacion(tipo);
        }
        if (datos.getEstado() != null) existente.setEstado(datos.getEstado());
        if (datos.getPendienteLimpieza() != null) existente.setPendienteLimpieza(datos.getPendienteLimpieza());
        if (datos.getAveriada() != null) existente.setAveriada(datos.getAveriada());
        if (datos.getProximaLimpieza() != null) existente.setProximaLimpieza(datos.getProximaLimpieza());
        return habitacionRepo.save(existente);
    }
    public void eliminar(Long id) {
        if (!habitacionRepo.existsById(id)) {
            throw new NoSuchElementException("Habitación no encontrada: " + id);
        }
        habitacionRepo.deleteById(id);
    }
}