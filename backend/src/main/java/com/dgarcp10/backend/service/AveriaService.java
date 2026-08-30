package com.dgarcp10.backend.service;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.Averia;
import com.dgarcp10.backend.model.EstadoAveria;
import com.dgarcp10.backend.model.EstadoBloqueo;
import com.dgarcp10.backend.model.GravedadAveria;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.AveriaRepository;
import com.dgarcp10.backend.repository.BloqueoHabitacionRepository;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.UsuarioRepository;
@Service
public class AveriaService {
    private final AveriaRepository averiaRepo;
    private final HabitacionRepository habitacionRepo;
    private final UsuarioRepository usuarioRepo;
    private final BloqueoService bloqueoService;
    private final BloqueoHabitacionRepository bloqueoRepo;
    public AveriaService(AveriaRepository averiaRepo,
                         HabitacionRepository habitacionRepo,
                         UsuarioRepository usuarioRepo,
                         BloqueoService bloqueoService,
                         BloqueoHabitacionRepository bloqueoRepo) {
        this.averiaRepo = averiaRepo;
        this.habitacionRepo = habitacionRepo;
        this.usuarioRepo = usuarioRepo;
        this.bloqueoService = bloqueoService;
        this.bloqueoRepo = bloqueoRepo;
    }
    public List<Averia> listar(EstadoAveria estado) {
        return estado == null ? averiaRepo.findAll() : averiaRepo.findByEstado(estado);
    }
    public Averia obtener(Long id) {
        return averiaRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Avería no encontrada: " + id));
    }
    @Transactional
    public Averia crear(Long habitacionId, GravedadAveria gravedad, String notas, Long usuarioId) {
        return crear(habitacionId, gravedad, notas, usuarioId, null, null, false);
    }
    @Transactional
    public Averia crear(Long habitacionId, GravedadAveria gravedad, String notas, Long usuarioId,
                        LocalDate fechaInicio, LocalDate fechaFin, boolean confirmarSinReubicacion) {
        if (gravedad == null) {
            throw new IllegalArgumentException("Debe especificar la gravedad de la avería");
        }
        Habitacion habitacion = habitacionRepo.findById(habitacionId)
            .orElseThrow(() -> new NoSuchElementException("Habitación no encontrada: " + habitacionId));
        if (averiaRepo.findByHabitacionIdAndEstado(habitacion.getId(), EstadoAveria.ABIERTA).isPresent()) {
            throw new IllegalStateException("La habitación ya tiene una avería abierta");
        }
        Usuario reportadoPor = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + usuarioId));
        Averia averia = new Averia();
        averia.setHabitacion(habitacion);
        averia.setReportadoPor(reportadoPor);
        averia.setGravedad(gravedad);
        averia.setNotas(notas);
        averia.setEstado(EstadoAveria.ABIERTA);
        averia.setCreadoEn(Instant.now());
        averia = averiaRepo.save(averia);
        habitacion.setAveriada(true);
        habitacionRepo.save(habitacion);
        if (gravedad == GravedadAveria.GRAVE) {
            averia.setBloqueo(bloqueoService.bloquearPorAveria(habitacion, reportadoPor,
                fechaInicio, fechaFin, confirmarSinReubicacion));
            return averiaRepo.save(averia);
        }
        return averia;
    }
    public Averia actualizar(Long id, GravedadAveria gravedad, String notas,
                            LocalDate fechaInicio, LocalDate fechaFin,
                            boolean confirmarSinReubicacion, Long usuarioId) {
        Averia averia = obtener(id);
        if (notas != null) averia.setNotas(notas);
        if (gravedad != null) averia.setGravedad(gravedad);
        if (gravedad == GravedadAveria.GRAVE) {
            if (fechaInicio == null || fechaFin == null) {
                throw new IllegalArgumentException("Debe indicar fecha inicio y fin para una avería grave");
            }
            if (fechaFin.isBefore(fechaInicio)) {
                throw new IllegalArgumentException("La fecha de fin debe ser posterior o igual a la de inicio");
            }
            if (averia.getBloqueo() == null) {
                Usuario creadoPor = usuarioRepo.findById(usuarioId)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + usuarioId));
                averia.setBloqueo(bloqueoService.bloquearPorAveria(
                    averia.getHabitacion(), creadoPor, fechaInicio, fechaFin, confirmarSinReubicacion));
            } else {
                averia.getBloqueo().setFechaInicio(fechaInicio);
                averia.getBloqueo().setFechaFin(fechaFin);
                bloqueoRepo.save(averia.getBloqueo());
            }
        } else if (averia.getBloqueo() != null) {
            bloqueoService.levantarPorResolucionDeAveria(averia.getBloqueo(), averia.getHabitacion());
        }
        return averiaRepo.save(averia);
    }
    @Transactional
    public Averia resolver(Long id) {
        Averia averia = obtener(id);
        if (averia.getEstado() == EstadoAveria.RESUELTA) {
            throw new IllegalStateException("La avería ya está resuelta");
        }
        averia.setEstado(EstadoAveria.RESUELTA);
        averia.getHabitacion().setAveriada(false);
        habitacionRepo.save(averia.getHabitacion());
        if (averia.getBloqueo() != null && averia.getBloqueo().getEstado() == EstadoBloqueo.ACTIVO) {
            bloqueoService.levantarPorResolucionDeAveria(averia.getBloqueo(), averia.getHabitacion());
        }
        return averiaRepo.save(averia);
    }
}