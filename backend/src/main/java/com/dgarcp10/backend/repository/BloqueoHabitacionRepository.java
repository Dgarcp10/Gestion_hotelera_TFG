package com.dgarcp10.backend.repository;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dgarcp10.backend.model.BloqueoHabitacion;
import com.dgarcp10.backend.model.EstadoBloqueo;
public interface BloqueoHabitacionRepository extends JpaRepository<BloqueoHabitacion, Long> {
    List<BloqueoHabitacion> findByHabitacionIdAndEstado(Long habitacionId, EstadoBloqueo estado);
    List<BloqueoHabitacion> findByEstado(EstadoBloqueo estado);
    @Query("SELECT COUNT(b) > 0 FROM BloqueoHabitacion b " +
           "WHERE b.habitacion.id = :habitacionId " +
           "AND b.estado = 'ACTIVO' " +
           "AND b.fechaInicio < :fin " +
           "AND b.fechaFin > :inicio")
    boolean existsActivoSolapado(@Param("habitacionId") Long habitacionId,
                                 @Param("inicio") LocalDate inicio,
                                 @Param("fin") LocalDate fin);
    @Query("SELECT COUNT(b) FROM BloqueoHabitacion b " +
           "WHERE b.habitacion.tipoHabitacion.id = :tipoId " +
           "AND b.estado = 'ACTIVO' " +
           "AND b.fechaInicio < :salida " +
           "AND b.fechaFin > :entrada")
    long countActivosEnRango(@Param("tipoId") Long tipoId,
                             @Param("entrada") LocalDate entrada,
                             @Param("salida") LocalDate salida);
}