package com.dgarcp10.backend.repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dgarcp10.backend.model.Averia;
import com.dgarcp10.backend.model.EstadoAveria;
public interface AveriaRepository extends JpaRepository<Averia, Long> {
    Optional<Averia> findByHabitacionIdAndEstado(Long habitacionId, EstadoAveria estado);
    List<Averia> findByEstado(EstadoAveria estado);
    @Query("SELECT a FROM Averia a " +
           "WHERE (a.creadoEn BETWEEN :desde AND :hasta) " +
           "OR (a.resueltaEn IS NOT NULL AND a.resueltaEn BETWEEN :desde AND :hasta) " +
           "ORDER BY a.creadoEn DESC")
    List<Averia> findEnRango(@Param("desde") Instant desde, @Param("hasta") Instant hasta);
}