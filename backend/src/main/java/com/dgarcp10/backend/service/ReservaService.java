package com.dgarcp10.backend.service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.dgarcp10.backend.model.EstadoHabitacion;
import com.dgarcp10.backend.model.EstadoPago;
import com.dgarcp10.backend.model.EstadoReserva;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.Pago;
import com.dgarcp10.backend.model.Reserva;
import com.dgarcp10.backend.model.TareaLimpieza;
import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.model.TipoTareaLimpieza;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.BloqueoHabitacionRepository;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.PagoRepository;
import com.dgarcp10.backend.repository.ReservaRepository;
import com.dgarcp10.backend.repository.TareaLimpiezaRepository;
import com.dgarcp10.backend.repository.TipoHabitacionRepository;
import com.dgarcp10.backend.repository.UsuarioRepository;

@Service
public class ReservaService {
    private static final int PENALIZACION_CANCELACION = 10;

    private final ReservaRepository reservaRepo;
    private final UsuarioRepository usuarioRepo;
    private final TipoHabitacionRepository tipoHabitacionRepo;
    private final HabitacionRepository habitacionRepo;
    private final TareaLimpiezaRepository tareaLimpiezaRepo;
    private final BloqueoHabitacionRepository bloqueoRepo;
    private final PasarelaService pasarelaService;
    private final PagoRepository pagoRepo;
    public ReservaService(ReservaRepository reservaRepo,
                          UsuarioRepository usuarioRepo,
                          TipoHabitacionRepository tipoHabitacionRepo,
                          HabitacionRepository habitacionRepo,
                          TareaLimpiezaRepository tareaLimpiezaRepo,
                          BloqueoHabitacionRepository bloqueoRepo,
                          PasarelaService pasarelaService,
                          PagoRepository pagoRepo) {
        this.reservaRepo = reservaRepo;
        this.usuarioRepo = usuarioRepo;
        this.tipoHabitacionRepo = tipoHabitacionRepo;
        this.habitacionRepo = habitacionRepo;
        this.tareaLimpiezaRepo = tareaLimpiezaRepo;
        this.bloqueoRepo = bloqueoRepo;
        this.pasarelaService = pasarelaService;
        this.pagoRepo = pagoRepo;
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
                                 LocalDate fechaEntrada, LocalDate fechaSalida,
                                 String numeroTarjeta, String caducidad, String cvv) {
        pasarelaService.validarFormato(numeroTarjeta, caducidad, cvv);
        if (!pasarelaService.procesar(numeroTarjeta)) {
            throw new IllegalStateException("El pago ha sido rechazado. No se ha creado la reserva.");
        }
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
        long bloqueadas = bloqueoRepo.countActivosEnRango(tipoHabitacionId, fechaEntrada, fechaSalida);
        if (reservadas + bloqueadas >= totalHabitaciones) {
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
        reserva = reservaRepo.save(reserva);
        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setImporte(precioTotal);
        pago.setEstado(EstadoPago.COMPLETADO);
        pago.setReferencia(generarReferencia());
        pago.setCreadoEn(Instant.now());
        reserva.setPago(pagoRepo.save(pago));
        return reserva;
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
        habitacion.setProximaLimpieza(
            reserva.getFechaSalida().isAfter(LocalDate.now().plusDays(1))
                ? LocalDate.now().plusDays(1)
                : null);
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
            habitacion.setEstado(
                    bloqueoRepo.existsActivoSolapado(habitacion.getId(), LocalDate.now(), LocalDate.now().plusDays(1))
                    ? EstadoHabitacion.BLOQUEADA
                    : EstadoHabitacion.LIBRE
            );
            habitacion.setPendienteLimpieza(true);
            if (tareaLimpiezaRepo.findByHabitacionIdAndCompletadaEnIsNull(habitacion.getId()).isEmpty()) {
                TareaLimpieza tarea = new TareaLimpieza();
                tarea.setHabitacion(habitacion);
                tarea.setTipo(TipoTareaLimpieza.CHECKOUT);
                tarea.setAccionable(true);
                tarea.setCreadoEn(Instant.now());
                tareaLimpiezaRepo.save(tarea);
            }
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
    private String generarReferencia() {
        return "PAY-" + UUID.randomUUID().toString()
            .replace("-", "").substring(0, 8).toUpperCase();
    }
    
    public InfoCancelacion infoCancelacion(Long reservaId, Long usuarioId) {
        Reserva reserva = reservaPendienteFuturaPropia(reservaId, usuarioId);
        BigDecimal importeCobrado = reserva.getPrecioTotal()
            .multiply(BigDecimal.valueOf(PENALIZACION_CANCELACION))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new InfoCancelacion(PENALIZACION_CANCELACION, importeCobrado,
            reserva.getPrecioTotal().subtract(importeCobrado));
    }
    @Transactional
    public Reserva cancelar(Long reservaId, Long usuarioId) {
        Reserva reserva = reservaPendienteFuturaPropia(reservaId, usuarioId);
        BigDecimal importeCobrado = reserva.getPrecioTotal()
            .multiply(BigDecimal.valueOf(PENALIZACION_CANCELACION))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        reserva.setImporteCobrado(importeCobrado);
        reserva.setEstado(EstadoReserva.CANCELADA);
        if (reserva.getPago() != null) {
            reserva.getPago().setEstado(EstadoPago.REEMBOLSADO);
        }
        return reservaRepo.save(reserva);
    }
    private Reserva reservaPendienteFuturaPropia(Long reservaId, Long usuarioId) {
        Reserva reserva = obtenerReserva(reservaId);
        if (!reserva.getUsuario().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes gestionar una reserva que no es tuya");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden cancelar reservas pendientes");
        }
        if (!reserva.getFechaEntrada().isAfter(LocalDate.now())) {
            throw new IllegalStateException("La reserva debe tener una fecha de entrada futura para cancelarse");
        }
        return reserva;
    }
    @Transactional
    public Reserva modificar(Long reservaId, Long usuarioId,
                            Long tipoHabitacionId, LocalDate fechaEntrada, LocalDate fechaSalida,
                            String numeroTarjeta, String caducidad, String cvv) {
        Reserva reserva = reservaPendienteFuturaPropia(reservaId, usuarioId);
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada");
        }
        if (fechaEntrada.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de entrada no puede ser pasada");
        }
        TipoHabitacion tipo = tipoHabitacionRepo.findById(tipoHabitacionId)
            .orElseThrow(() -> new NoSuchElementException("Tipo de habitación no encontrado"));
        long totalHabitaciones = habitacionRepo.countByTipoHabitacionId(tipoHabitacionId);
        long reservadas = reservaRepo.countReservasActivasEnRangoExcluyendo(
            tipoHabitacionId, reservaId, fechaEntrada, fechaSalida);
        long bloqueadas = bloqueoRepo.countActivosEnRango(tipoHabitacionId, fechaEntrada, fechaSalida);
        if (reservadas + bloqueadas >= totalHabitaciones) {
            throw new IllegalStateException("No hay habitaciones disponibles para este tipo en las fechas seleccionadas");
        }
        long noches = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        BigDecimal nuevoTotal = tipo.getPrecioBase().multiply(BigDecimal.valueOf(noches));
        int comparacion = nuevoTotal.compareTo(reserva.getPrecioTotal());
        if (comparacion > 0) {
            pasarelaService.validarFormato(numeroTarjeta, caducidad, cvv);
            if (!pasarelaService.procesar(numeroTarjeta)) {
                throw new IllegalStateException("El pago de la diferencia ha sido rechazado. No se ha modificado la reserva.");
            }
        }
        reserva.setTipoHabitacion(tipo);
        reserva.setFechaEntrada(fechaEntrada);
        reserva.setFechaSalida(fechaSalida);
        reserva.setPrecioTotal(nuevoTotal);
        reserva.setImporteCobrado(nuevoTotal);
        if (reserva.getPago() != null) {
            reserva.getPago().setImporte(nuevoTotal);
            reserva.getPago().setEstado(comparacion < 0 ? EstadoPago.REEMBOLSADO : EstadoPago.COMPLETADO);
        }
        return reservaRepo.save(reserva);
    }
}