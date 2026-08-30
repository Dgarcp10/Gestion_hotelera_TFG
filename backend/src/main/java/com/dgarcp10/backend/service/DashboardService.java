package com.dgarcp10.backend.service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.model.EstadoReserva;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.Notificacion;
import com.dgarcp10.backend.model.Reserva;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.AveriaRepository;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.NotificacionRepository;
import com.dgarcp10.backend.repository.ReservaRepository;
import com.dgarcp10.backend.repository.TareaLimpiezaRepository;
@Service
public class DashboardService {
    private final HabitacionRepository habitacionRepo;
    private final ReservaRepository reservaRepo;
    private final TareaLimpiezaRepository tareaRepo;
    private final AveriaRepository averiaRepo;
    private final NotificacionRepository notificacionRepo;
    public DashboardService(HabitacionRepository habitacionRepo,
                            ReservaRepository reservaRepo,
                            TareaLimpiezaRepository tareaRepo,
                            AveriaRepository averiaRepo,
                            NotificacionRepository notificacionRepo) {
        this.habitacionRepo = habitacionRepo;
        this.reservaRepo = reservaRepo;
        this.tareaRepo = tareaRepo;
        this.averiaRepo = averiaRepo;
        this.notificacionRepo = notificacionRepo;
    }
    @Transactional(readOnly = true)
    public DashboardGraficos resumenGraficos(int anio, int mes) {
        if (anio <= 0) throw new IllegalArgumentException("Año inválido: " + anio);
        if (mes < 1 || mes > 12) throw new IllegalArgumentException("Mes inválido: " + mes);
        long nHab = habitacionRepo.count();
        List<Reserva> reservas = reservaRepo.findAll().stream()
            .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
            .toList();
        List<OcupacionPunto> ocupacionAnual = new ArrayList<>();
        List<GananciasPunto> gananciasAnuales = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            int dias = YearMonth.of(anio, m).lengthOfMonth();
            double pct = nHab > 0 ? redondear1((double) ocupadasEnMes(reservas, anio, m) / (nHab * dias) * 100) : 0;
            ocupacionAnual.add(new OcupacionPunto(m, pct));
            gananciasAnuales.add(new GananciasPunto(m, gananciasEnMes(reservas, anio, m)));
        }
        YearMonth ym = YearMonth.of(anio, mes);
        int diasMes = ym.lengthOfMonth();
        List<OcupacionPunto> ocupacionMensual = new ArrayList<>();
        List<GananciasPunto> gananciasMensuales = new ArrayList<>();
        for (int dia = 1; dia <= diasMes; dia++) {
            LocalDate fecha = ym.atDay(dia);
            double pct = nHab > 0 ? redondear1(ocupadasDia(reservas, fecha) / (double) nHab * 100) : 0;
            ocupacionMensual.add(new OcupacionPunto(dia, pct));
            gananciasMensuales.add(new GananciasPunto(dia, gananciasDia(reservas, fecha)));
        }
        return new DashboardGraficos(anio, mes, ocupacionAnual, ocupacionMensual, gananciasAnuales, gananciasMensuales);
    }
    @Transactional(readOnly = true)
    public DashboardSeguimiento seguimiento(LocalDate desde, LocalDate hasta,
                                            Long tipoHabitacionId, Integer numero) {
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha 'hasta' debe ser posterior o igual a 'desde'");
        }
        Instant inicio = desde.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant fin = hasta.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<TrazaLimpieza> tareas = tareaRepo.findEnRango(inicio, fin).stream()
            .filter(t -> cumpleFiltroHabitacion(t.getHabitacion(), tipoHabitacionId, numero))
            .map(t -> new TrazaLimpieza(t.getId(),
                t.getHabitacion().getNumero(),
                t.getHabitacion().getTipoHabitacion().getNombre(),
                t.getTipo(),
                t.getCompletadoPor() != null ? nombreUsuario(t.getCompletadoPor()) : null,
                t.getCompletadaEn(),
                t.getCreadoEn()))
            .toList();
        List<TrazaAveria> averias = averiaRepo.findEnRango(inicio, fin).stream()
            .filter(a -> cumpleFiltroHabitacion(a.getHabitacion(), tipoHabitacionId, numero))
            .map(a -> new TrazaAveria(a.getId(),
                a.getHabitacion().getNumero(),
                a.getHabitacion().getTipoHabitacion().getNombre(),
                a.getGravedad(),
                a.getEstado(),
                nombreUsuario(a.getReportadoPor()),
                a.getCreadoEn(),
                a.getResueltaPor() != null ? nombreUsuario(a.getResueltaPor()) : null,
                a.getResueltaEn()))
            .toList();
        boolean filtroActivo = tipoHabitacionId != null || numero != null;
        List<TrazaNotificacion> conHabitacion = new ArrayList<>();
        List<TrazaNotificacion> sinHabitacion = new ArrayList<>();
        for (Notificacion n : notificacionRepo.findByCreadoEnBetweenOrderByCreadoEnDesc(inicio, fin)) {
            Habitacion hab = n.getBloqueo() != null ? n.getBloqueo().getHabitacion() : null;
            if (hab == null) {
                if (!filtroActivo) {
                    sinHabitacion.add(toTrazaNotificacion(n, null, null));
                }
            } else if (cumpleFiltroHabitacion(hab, tipoHabitacionId, numero)) {
                conHabitacion.add(toTrazaNotificacion(n, hab.getId(), hab.getNumero()));
            }
        }
        return new DashboardSeguimiento(tareas, averias, conHabitacion, sinHabitacion);
    }
    private long ocupadasDia(List<Reserva> reservas, LocalDate fecha) {
        long n = 0;
        for (Reserva r : reservas) {
            if (!r.getFechaEntrada().isAfter(fecha) && r.getFechaSalida().isAfter(fecha)) n++;
        }
        return n;
    }
    private long ocupadasEnMes(List<Reserva> reservas, int anio, int mes) {
        YearMonth ym = YearMonth.of(anio, mes);
        long n = 0;
        for (int d = 1; d <= ym.lengthOfMonth(); d++) n += ocupadasDia(reservas, ym.atDay(d));
        return n;
    }
    private BigDecimal gananciasDia(List<Reserva> reservas, LocalDate fecha) {
        BigDecimal total = BigDecimal.ZERO;
        for (Reserva r : reservas) {
            if (r.getFechaEntrada().isAfter(fecha) || !r.getFechaSalida().isAfter(fecha)) continue;
            long noches = ChronoUnit.DAYS.between(r.getFechaEntrada(), r.getFechaSalida());
            if (noches <= 0) continue;
            total = total.add(r.getImporteCobrado().divide(BigDecimal.valueOf(noches), 6, RoundingMode.HALF_UP));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    private BigDecimal gananciasEnMes(List<Reserva> reservas, int anio, int mes) {
        YearMonth ym = YearMonth.of(anio, mes);
        BigDecimal total = BigDecimal.ZERO;
        for (int d = 1; d <= ym.lengthOfMonth(); d++) total = total.add(gananciasDia(reservas, ym.atDay(d)));
        return total;
    }
    private double redondear1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
    private boolean cumpleFiltroHabitacion(Habitacion hab, Long tipoId, Integer numero) {
        if (tipoId != null && !hab.getTipoHabitacion().getId().equals(tipoId)) return false;
        if (numero != null && !hab.getNumero().equals(numero)) return false;
        return true;
    }
    private TrazaNotificacion toTrazaNotificacion(Notificacion n, Long habId, Integer habNum) {
        return new TrazaNotificacion(n.getId(), n.getTipo(), n.getMensaje(), habId, habNum, n.getCreadoEn());
    }
    private String nombreUsuario(Usuario u) {
        return (u.getNombre() + " " + u.getApellido()).trim();
    }
}