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
    private final LimpiezaService limpiezaService;
    public HabitacionService(HabitacionRepository habitacionRepo, TipoHabitacionRepository tipoHabitacionRepo, LimpiezaService limpiezaService) {
        this.habitacionRepo = habitacionRepo;
        this.tipoHabitacionRepo = tipoHabitacionRepo;
        this.limpiezaService = limpiezaService;
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
        Habitacion guardada = habitacionRepo.save(habitacion);
        if (Boolean.TRUE.equals(guardada.getPendienteLimpieza())) {
            limpiezaService.crearTareaPendiente(guardada);
        }
        return guardada;
    }
    public Habitacion actualizar(Long id, Habitacion datos, Long usuarioId) {
        Habitacion existente = obtenerPorId(id);
        boolean completandoLimpieza = Boolean.TRUE.equals(existente.getPendienteLimpieza())
            && Boolean.FALSE.equals(datos.getPendienteLimpieza());
        boolean marcandoPendiente = Boolean.FALSE.equals(existente.getPendienteLimpieza())
            && Boolean.TRUE.equals(datos.getPendienteLimpieza());
        if (completandoLimpieza) {
            limpiezaService.completarHabitacion(existente.getNumero(), usuarioId);
        }
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
        Habitacion guardada = habitacionRepo.save(existente);
        if (marcandoPendiente) {
            limpiezaService.crearTareaPendiente(guardada);
        }
        return guardada;
    }
    public void eliminar(Long id) {
        if (!habitacionRepo.existsById(id)) {
            throw new NoSuchElementException("Habitación no encontrada: " + id);
        }
        habitacionRepo.deleteById(id);
    }
}