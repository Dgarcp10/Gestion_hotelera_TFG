package com.dgarcp10.backend.repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dgarcp10.backend.model.TareaLimpieza;
public interface TareaLimpiezaRepository extends JpaRepository<TareaLimpieza, Long> {
    List<TareaLimpieza> findByCompletadaEnIsNullOrderByCreadoEnDesc();
    Optional<TareaLimpieza> findByHabitacionIdAndCompletadaEnIsNull(Long habitacionId);
    @Query("SELECT DISTINCT t.habitacion.id FROM TareaLimpieza t WHERE t.completadaEn IS NULL")
    Set<Long> findIdsHabitacionConTareaPendiente();
    @Query("SELECT t FROM TareaLimpieza t " +
           "WHERE (t.creadoEn BETWEEN :desde AND :hasta) " +
           "OR (t.completadaEn BETWEEN :desde AND :hasta) " +
           "ORDER BY t.creadoEn DESC")
    List<TareaLimpieza> findEnRango(@Param("desde") Instant desde, @Param("hasta") Instant hasta);
}