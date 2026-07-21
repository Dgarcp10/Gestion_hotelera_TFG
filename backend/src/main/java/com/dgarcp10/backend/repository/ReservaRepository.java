package com.dgarcp10.backend.repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dgarcp10.backend.model.EstadoReserva;
import com.dgarcp10.backend.model.Reserva;
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);
    @Query("SELECT COUNT(r) FROM Reserva r " +
           "WHERE r.tipoHabitacion.id = :tipoId " +
           "AND r.estado <> 'CANCELADA' " +
           "AND r.fechaEntrada < :salida " +
           "AND r.fechaSalida > :entrada")
    long countReservasActivasEnRango(@Param("tipoId") Long tipoId,
                                     @Param("entrada") LocalDate entrada,
                                     @Param("salida") LocalDate salida);

    List<Reserva> findByEstadoAndFechaEntradaLessThanEqualOrderByCreadoEnDesc(
           EstadoReserva estado, LocalDate fecha);
    List<Reserva> findByEstadoAndFechaSalidaLessThanEqualOrderByFechaEntradaAsc(
           EstadoReserva estado, LocalDate fecha);
    Optional<Reserva> findByHabitacionIdAndEstado(Long habitacionId, EstadoReserva estado);
}