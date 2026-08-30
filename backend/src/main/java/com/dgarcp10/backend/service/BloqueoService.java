package com.dgarcp10.backend.service;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.BloqueoHabitacion;
import com.dgarcp10.backend.model.EstadoBloqueo;
import com.dgarcp10.backend.model.EstadoHabitacion;
import com.dgarcp10.backend.model.EstadoReserva;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.Reserva;
import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.BloqueoHabitacionRepository;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.ReservaRepository;
@Service
public class BloqueoService {
    private static final List<RolUsuario> ROLES_AFECTADOS = List.of(RolUsuario.JEFE, RolUsuario.RECEPCION, RolUsuario.MANTENIMIENTO, RolUsuario.LIMPIEZA);
    private final BloqueoHabitacionRepository bloqueoRepo;
    private final HabitacionRepository habitacionRepo;
    private final ReservaRepository reservaRepo;
    private final NotificacionService notificacionService;
    public BloqueoService(BloqueoHabitacionRepository bloqueoRepo,
                          HabitacionRepository habitacionRepo,
                          ReservaRepository reservaRepo,
                          NotificacionService notificacionService) {
        this.bloqueoRepo = bloqueoRepo;
        this.habitacionRepo = habitacionRepo;
        this.reservaRepo = reservaRepo;
        this.notificacionService = notificacionService;
    }
    public List<BloqueoHabitacion> listar(EstadoBloqueo estado) {
        return estado == null ? bloqueoRepo.findAll() : bloqueoRepo.findByEstado(estado);
    }
    @Transactional
    public BloqueoHabitacion bloquearPorAveria(Habitacion habitacion, Usuario creadoPor,
                                               LocalDate fechaInicio, LocalDate fechaFin,
                                               boolean confirmarSinReubicacion) {
        LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.now();
        LocalDate fin = fechaFin != null ? fechaFin : inicio.plusDays(1);
        return crearBloqueo(habitacion, inicio, fin, "Avería grave", confirmarSinReubicacion, creadoPor);
    }
    public ReubicacionPreview previewReubicacion(Long habitacionId, LocalDate fechaInicio, LocalDate fechaFin) {
        Habitacion habitacion = habitacionRepo.findById(habitacionId)
            .orElseThrow(() -> new NoSuchElementException("Habitación no encontrada: " + habitacionId));
        LocalDate inicio = fechaInicio != null ? fechaInicio : LocalDate.now();
        LocalDate fin = fechaFin != null ? fechaFin : inicio.plusDays(1);
        if (bloqueoRepo.existsActivoSolapado(habitacion.getId(), inicio, fin)) {
            return new ReubicacionPreview(false, null, null, false, false, true);
        }
        Reserva huespedActual = reservaRepo.findByHabitacionIdAndEstadoIn(habitacion.getId(),
                List.of(EstadoReserva.EN_CURSO)).stream()
            .filter(r -> !r.getFechaEntrada().isAfter(fin) && !r.getFechaSalida().isBefore(inicio))
            .findFirst().orElse(null);
        if (huespedActual == null) {
            return new ReubicacionPreview(false, null, null, false, false, false);
        }
        Optional<Habitacion> destino = buscarHabitacionParaReubicar(huespedActual);
        if (destino.isEmpty()) {
            return new ReubicacionPreview(true, nombreCompleto(huespedActual), null, false, false, false);
        }
        boolean categoriaSuperior = destino.get().getTipoHabitacion().getCapacidad()
            > huespedActual.getTipoHabitacion().getCapacidad();
        return new ReubicacionPreview(true, nombreCompleto(huespedActual), destino.get().getNumero(),
            categoriaSuperior, true, false);
    }
    private String nombreCompleto(Reserva reserva) {
        return reserva.getUsuario().getNombre() + " " + reserva.getUsuario().getApellido();
    }
    @Transactional
    public BloqueoHabitacion crear(Long habitacionId, LocalDate fechaInicio, LocalDate fechaFin, String motivo,
                                   Boolean confirmarSinReubicacion, Usuario creadoPor) {
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior o igual a la de inicio");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del bloqueo es obligatorio");
        }
        Habitacion habitacion = habitacionRepo.findById(habitacionId)
            .orElseThrow(() -> new NoSuchElementException("Habitación no encontrada: " + habitacionId));
        return crearBloqueo(habitacion, fechaInicio, fechaFin, motivo,
            Boolean.TRUE.equals(confirmarSinReubicacion), creadoPor);
    }
    private BloqueoHabitacion crearBloqueo(Habitacion habitacion, LocalDate inicio, LocalDate fin,
                                           String motivo, boolean confirmado, Usuario creadoPor) {
        if (bloqueoRepo.existsActivoSolapado(habitacion.getId(), inicio, fin)) {
            throw new IllegalStateException("La habitación ya tiene un bloqueo activo en ese periodo");
        }
        Reserva huespedActual = reservaRepo.findByHabitacionIdAndEstadoIn(habitacion.getId(),
                List.of(EstadoReserva.EN_CURSO)).stream()
            .filter(r -> !r.getFechaEntrada().isAfter(fin) && !r.getFechaSalida().isBefore(inicio))
            .findFirst().orElse(null);
        boolean categoriaSuperior = false;
        boolean limpiezaPendienteDestino = false;
        if (huespedActual != null && !confirmado) {
            Optional<Habitacion> destino = buscarHabitacionParaReubicar(huespedActual);
            if (destino.isEmpty()) {
                throw new IllegalStateException("No hay habitación disponible para reubicar al huésped. " +
                    "Confirma el bloqueo sin reubicación para aplicarlo igualmente.");
            }
            categoriaSuperior = destino.get().getTipoHabitacion().getCapacidad()
                > huespedActual.getTipoHabitacion().getCapacidad();
            limpiezaPendienteDestino = destino.get().getPendienteLimpieza();
            reubicarHuesped(huespedActual, destino.get());
        }
        BloqueoHabitacion bloqueo = new BloqueoHabitacion();
        bloqueo.setHabitacion(habitacion);
        bloqueo.setFechaInicio(inicio);
        bloqueo.setFechaFin(fin);
        bloqueo.setMotivo(motivo);
        bloqueo.setCreadoPor(creadoPor);
        bloqueo.setConfirmadoSinReubicacion(confirmado);
        bloqueo.setCreadoEn(Instant.now());
        BloqueoHabitacion guardado = aplicarBloqueo(bloqueo);
        notificarImpacto(guardado, huespedActual, categoriaSuperior, limpiezaPendienteDestino);
        return guardado;
    }
    private BloqueoHabitacion aplicarBloqueo(BloqueoHabitacion bloqueo) {
        Habitacion habitacion = bloqueo.getHabitacion();
        if (bloqueo.getConfirmadoSinReubicacion() || habitacion.getEstado() != EstadoHabitacion.OCUPADA) {
            habitacion.setEstado(EstadoHabitacion.BLOQUEADA);
            habitacionRepo.save(habitacion);
        }
        return bloqueoRepo.save(bloqueo);
    }
    
    private Optional<Habitacion> buscarHabitacionParaReubicar(Reserva reserva) {
        TipoHabitacion tipoOriginal = reserva.getTipoHabitacion();
        LocalDate desde = LocalDate.now();
        LocalDate hasta = reserva.getFechaSalida().isAfter(desde)
            ? reserva.getFechaSalida() : desde.plusDays(1);
        return habitacionRepo.findAll().stream()
            .filter(h -> h.getEstado() == EstadoHabitacion.LIBRE)
            .filter(h -> !bloqueoRepo.existsActivoSolapado(h.getId(), desde, hasta))
            .filter(h -> h.getTipoHabitacion().getCapacidad() >= tipoOriginal.getCapacidad())
            .filter(h -> tipoTieneHueco(h.getTipoHabitacion().getId(), desde, hasta))
            .sorted(Comparator
                .comparing((Habitacion h) -> h.getTipoHabitacion().getId().equals(tipoOriginal.getId()) ? 0 : 1)
                .thenComparing(h -> h.getPendienteLimpieza() ? 1 : 0)
                .thenComparing(h -> h.getTipoHabitacion().getCapacidad())
                .thenComparing(h -> h.getTipoHabitacion().getPrecioBase()))
            .findFirst();
    }
    private boolean tipoTieneHueco(Long tipoId, LocalDate desde, LocalDate hasta) {
        long total = habitacionRepo.countByTipoHabitacionId(tipoId);
        long ocupacion = reservaRepo.countReservasActivasEnRango(tipoId, desde, hasta)
                       + bloqueoRepo.countActivosEnRango(tipoId, desde, hasta);
        return ocupacion < total;
    }

    private void reubicarHuesped(Reserva reserva, Habitacion destino) {
        Habitacion origen = reserva.getHabitacion();
        destino.setEstado(EstadoHabitacion.OCUPADA);
        destino.setProximaLimpieza(
            reserva.getFechaSalida().isAfter(LocalDate.now().plusDays(1))
                ? LocalDate.now().plusDays(1) : null);
        if (origen != null) origen.setEstado(EstadoHabitacion.LIBRE);
        reserva.setHabitacion(destino);
        reserva.setTipoHabitacion(destino.getTipoHabitacion());
        habitacionRepo.save(destino);
        if (origen != null) habitacionRepo.save(origen);
        reservaRepo.save(reserva);
    }
    private void notificarImpacto(BloqueoHabitacion bloqueo, Reserva huespedActual,
                                  boolean categoriaSuperior, boolean limpiezaPendienteDestino) {
        Habitacion habitacion = bloqueo.getHabitacion();
        Habitacion destino = huespedActual != null ? huespedActual.getHabitacion() : null;
        String mensaje;
        if (huespedActual != null && destino != null
                && !destino.getId().equals(habitacion.getId())) {
            String nombreHuesped = huespedActual.getUsuario().getNombre() + " " + huespedActual.getUsuario().getApellido();
            mensaje = "Bloqueo de la habitación " + habitacion.getNumero() + ". Huésped " +
                nombreHuesped + " reubicado de la habitación " + habitacion.getNumero() +
                " a la habitación " + destino.getNumero() +
                (categoriaSuperior ? " (categoría superior)" : "") + ".";
            if (limpiezaPendienteDestino) {
                mensaje += " Atención: asignada con limpieza pendiente.";
                notificacionService.notificarRoles(List.of(RolUsuario.LIMPIEZA), "BLOQUEO",
                    "La habitación " + destino.getNumero() +
                        " ha sido ocupada con limpieza pendiente. Priorizar su limpieza.",
                    bloqueo);
            }
        } else if (huespedActual != null) {
            String nombreHuesped = huespedActual.getUsuario().getNombre() + " " + huespedActual.getUsuario().getApellido();
            mensaje = "Bloqueo de la habitación " + habitacion.getNumero() +
                " con huésped " + nombreHuesped + " dentro (debe realizarse una reubicación manual).";
        } else {
            mensaje = "Bloqueo de la habitación " + habitacion.getNumero() +
                " (" + bloqueo.getFechaInicio() + " al " + bloqueo.getFechaFin() + ").";
            long futuras = reservaRepo.countReservasActivasEnRango(
                habitacion.getTipoHabitacion().getId(), bloqueo.getFechaInicio(), bloqueo.getFechaFin());
            if (futuras > 0) {
                mensaje += " Afecta a " + futuras + " reserva(s) futuras del tipo " +
                    habitacion.getTipoHabitacion().getNombre() + ".";
            }
        }
        notificacionService.notificarRoles(ROLES_AFECTADOS, "BLOQUEO", mensaje, bloqueo);
    }
    @Transactional
    public void levantarPorResolucionDeAveria(BloqueoHabitacion bloqueo, Habitacion habitacion) {
        bloqueo.setEstado(EstadoBloqueo.CANCELADO);
        bloqueoRepo.save(bloqueo);
        if (habitacion.getEstado() == EstadoHabitacion.BLOQUEADA) {
            habitacion.setEstado(EstadoHabitacion.LIBRE);
            habitacionRepo.save(habitacion);
        }
    }
    @Transactional
    public BloqueoHabitacion cancelar(Long id) {
        BloqueoHabitacion bloqueo = bloqueoRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Bloqueo no encontrado: " + id));
        if (bloqueo.getEstado() != EstadoBloqueo.ACTIVO) {
            throw new IllegalStateException("El bloqueo ya no está activo");
        }
        bloqueo.setEstado(EstadoBloqueo.CANCELADO);
        bloqueoRepo.save(bloqueo);
        Habitacion habitacion = bloqueo.getHabitacion();
        if (habitacion.getEstado() == EstadoHabitacion.BLOQUEADA) {
            boolean hayOtroActivo = bloqueoRepo.findByHabitacionIdAndEstado(
                    habitacion.getId(), EstadoBloqueo.ACTIVO).stream()
                .anyMatch(b -> !b.getId().equals(bloqueo.getId()));
            if (!hayOtroActivo) {
                habitacion.setEstado(EstadoHabitacion.LIBRE);
                habitacionRepo.save(habitacion);
            }
        }
        return bloqueo;
    }
}