package com.dgarcp10.backend.service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.EstadoHabitacion;
import com.dgarcp10.backend.model.EstadoReserva;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.TareaLimpieza;
import com.dgarcp10.backend.model.TipoTareaLimpieza;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.ReservaRepository;
import com.dgarcp10.backend.repository.TareaLimpiezaRepository;
import com.dgarcp10.backend.repository.UsuarioRepository;
@Service
public class LimpiezaService {
    private final TareaLimpiezaRepository tareaRepo;
    private final HabitacionRepository habitacionRepo;
    private final ReservaRepository reservaRepo;
    private final UsuarioRepository usuarioRepo;
    public LimpiezaService(TareaLimpiezaRepository tareaRepo,
                           HabitacionRepository habitacionRepo,
                           ReservaRepository reservaRepo,
                           UsuarioRepository usuarioRepo) {
        this.tareaRepo = tareaRepo;
        this.habitacionRepo = habitacionRepo;
        this.reservaRepo = reservaRepo;
        this.usuarioRepo = usuarioRepo;
    }
    public List<TareaLimpieza> tareasPendientes() {
        return tareaRepo.findByCompletadaEnIsNullOrderByCreadoEnDesc();
    }
    public List<Habitacion> previstasManana() {
        return habitacionRepo.findByProximaLimpieza(LocalDate.now().plusDays(1));
    }
    @Transactional
    public void programarLimpieza(Integer numeroHabitacion) {
        Habitacion habitacion = habitacionRepo.findByNumero(numeroHabitacion)
            .orElseThrow(() -> new NoSuchElementException("Habitación no encontrada: " + numeroHabitacion));
        if (tareaRepo.findByHabitacionIdAndCompletadaEnIsNull(habitacion.getId()).isPresent()) {
            throw new IllegalStateException("La habitación ya tiene una tarea de limpieza pendiente");
        }
        TipoTareaLimpieza tipo = switch (habitacion.getEstado()) {
            case LIBRE -> TipoTareaLimpieza.REPASO_VACIA;
            case OCUPADA -> TipoTareaLimpieza.REPASO_ESTANCIA;
            case BLOQUEADA -> TipoTareaLimpieza.REPASO_VACIA;
        };
        TareaLimpieza tarea = new TareaLimpieza();
        tarea.setHabitacion(habitacion);
        tarea.setTipo(tipo);
        tarea.setAccionable(true);
        tarea.setCreadoEn(Instant.now());
        habitacion.setPendienteLimpieza(true);
        habitacion.setProximaLimpieza(null);
        tareaRepo.save(tarea);
        habitacionRepo.save(habitacion);
    }
    @Transactional
    public void completarTarea(Long tareaId, Long usuarioId) {
        TareaLimpieza tarea = tareaRepo.findById(tareaId)
            .orElseThrow(() -> new NoSuchElementException("Tarea de limpieza no encontrada: " + tareaId));
        if (tarea.getCompletadaEn() != null) {
            throw new IllegalStateException("La tarea ya fue completada");
        }
        Usuario usuario = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        Habitacion habitacion = tarea.getHabitacion();
        habitacion.setPendienteLimpieza(false);
        if (habitacion.getEstado() == EstadoHabitacion.OCUPADA) {
            reservaRepo.findByHabitacionIdAndEstado(habitacion.getId(), EstadoReserva.EN_CURSO)
                .ifPresent(reserva -> {
                    long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), reserva.getFechaSalida());
                    if (diasRestantes >= 6) {
                        habitacion.setProximaLimpieza(LocalDate.now().plusDays(4));
                    } else {
                        habitacion.setProximaLimpieza(null);
                    }
                });
        } else if (habitacion.getEstado() == EstadoHabitacion.LIBRE) {
            habitacion.setProximaLimpieza(LocalDate.now().plusDays(7));
        } else {
            habitacion.setProximaLimpieza(null);
        }
        tarea.setCompletadoPor(usuario);
        tarea.setCompletadaEn(Instant.now());
        tareaRepo.save(tarea);
        habitacionRepo.save(habitacion);
    }
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void generarTareasDiarias() {
        List<Habitacion> habitaciones = habitacionRepo
            .findByPendienteLimpiezaTrueOrProximaLimpiezaLessThanEqual(LocalDate.now());
        for (Habitacion h : habitaciones) {
            if (tareaRepo.findByHabitacionIdAndCompletadaEnIsNull(h.getId()).isPresent()) {
                continue;
            }
            TipoTareaLimpieza tipo;
            if (h.getPendienteLimpieza()) {
                tipo = h.getEstado() == EstadoHabitacion.OCUPADA
                    ? TipoTareaLimpieza.REPASO_ESTANCIA
                    : TipoTareaLimpieza.CHECKOUT;
            } else {
                tipo = h.getEstado() == EstadoHabitacion.OCUPADA
                    ? TipoTareaLimpieza.REPASO_ESTANCIA
                    : TipoTareaLimpieza.REPASO_VACIA;
            }
            TareaLimpieza tarea = new TareaLimpieza();
            tarea.setHabitacion(h);
            tarea.setTipo(tipo);
            tarea.setAccionable(true);
            tarea.setCreadoEn(Instant.now());
            tareaRepo.save(tarea);
        }
    }
}