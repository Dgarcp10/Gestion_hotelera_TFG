package com.dgarcp10.backend.service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.EstadoHabitacion;
import com.dgarcp10.backend.model.EstadoReserva;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.Reserva;
import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.ReservaRepository;
import com.dgarcp10.backend.repository.TipoHabitacionRepository;
import com.dgarcp10.backend.repository.UsuarioRepository;
@Service
public class ReservaService {
    private final ReservaRepository reservaRepo;
    private final UsuarioRepository usuarioRepo;
    private final TipoHabitacionRepository tipoHabitacionRepo;
    private final HabitacionRepository habitacionRepo;
    public ReservaService(ReservaRepository reservaRepo,
                          UsuarioRepository usuarioRepo,
                          TipoHabitacionRepository tipoHabitacionRepo,
                          HabitacionRepository habitacionRepo) {
        this.reservaRepo = reservaRepo;
        this.usuarioRepo = usuarioRepo;
        this.tipoHabitacionRepo = tipoHabitacionRepo;
        this.habitacionRepo = habitacionRepo;
    }
    public List<Reserva> misReservas(Long usuarioId) {
        return reservaRepo.findByUsuarioIdOrderByCreadoEnDesc(usuarioId);
    }
    public Reserva obtenerReserva(Long id) {
        return reservaRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Reserva no encontrada: " + id));
    }
    @Transactional
    public Reserva crearReserva(Long usuarioId, Long tipoHabitacionId,
                                 LocalDate fechaEntrada, LocalDate fechaSalida) {
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada");
        }
        if (fechaEntrada.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de entrada no puede ser pasada");
        }
        Usuario usuario = usuarioRepo.findById(usuarioId)
            .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        TipoHabitacion tipo = tipoHabitacionRepo.findById(tipoHabitacionId)
            .orElseThrow(() -> new NoSuchElementException("Tipo de habitación no encontrado"));
        long totalHabitaciones = habitacionRepo.countByTipoHabitacionId(tipoHabitacionId);
        long reservadas = reservaRepo.countReservasActivasEnRango(tipoHabitacionId, fechaEntrada, fechaSalida);
        if (reservadas >= totalHabitaciones) {
            throw new IllegalStateException("No hay habitaciones disponibles para este tipo en las fechas seleccionadas");
        }
        long noches = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        BigDecimal precioTotal = tipo.getPrecioBase().multiply(BigDecimal.valueOf(noches));
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setTipoHabitacion(tipo);
        reserva.setFechaEntrada(fechaEntrada);
        reserva.setFechaSalida(fechaSalida);
        reserva.setPrecioTotal(precioTotal);
        reserva.setImporteCobrado(precioTotal);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setCreadoEn(Instant.now());
        return reservaRepo.save(reserva);
    }
    @Transactional
    public Reserva checkIn(Long reservaId) {
        Reserva reserva = obtenerReserva(reservaId);
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo se puede hacer check-in de reservas pendientes");
        }
        Habitacion habitacion = habitacionRepo.findByTipoHabitacionId(reserva.getTipoHabitacion().getId())
            .stream()
            .filter(h -> h.getEstado() == EstadoHabitacion.LIBRE
                      && !h.getPendienteLimpieza())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No hay habitaciones libres disponibles"));
        habitacion.setEstado(EstadoHabitacion.OCUPADA);
        habitacionRepo.save(habitacion);
        reserva.setHabitacion(habitacion);
        reserva.setEstado(EstadoReserva.EN_CURSO);
        return reservaRepo.save(reserva);
    }
    @Transactional
    public Reserva checkOut(Long reservaId) {
        Reserva reserva = obtenerReserva(reservaId);
        if (reserva.getEstado() != EstadoReserva.EN_CURSO) {
            throw new IllegalStateException("Solo se puede hacer check-out de reservas en curso");
        }
        Habitacion habitacion = reserva.getHabitacion();
        if (habitacion != null) {
            habitacion.setEstado(EstadoHabitacion.LIBRE);
            habitacion.setPendienteLimpieza(true);
            habitacionRepo.save(habitacion);
        }
        reserva.setEstado(EstadoReserva.FINALIZADA);
        return reservaRepo.save(reserva);
    }
    public List<Reserva> reservasPendientes() {
        return reservaRepo.findByEstadoAndFechaEntradaLessThanEqualOrderByCreadoEnDesc(
            EstadoReserva.PENDIENTE, LocalDate.now());
    }
    public List<Reserva> estanciasParaCheckOut() {
        return reservaRepo.findByEstadoAndFechaSalidaLessThanEqualOrderByFechaEntradaAsc(
            EstadoReserva.EN_CURSO, LocalDate.now());
    }
}