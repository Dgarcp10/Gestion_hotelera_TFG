package com.dgarcp10.backend.repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dgarcp10.backend.model.TareaLimpieza;
public interface TareaLimpiezaRepository extends JpaRepository<TareaLimpieza, Long> {
    List<TareaLimpieza> findByCompletadaEnIsNullOrderByCreadoEnDesc();
    Optional<TareaLimpieza> findByHabitacionIdAndCompletadaEnIsNull(Long habitacionId);
    @Query("SELECT DISTINCT t.habitacion.id FROM TareaLimpieza t WHERE t.completadaEn IS NULL")
    Set<Long> findIdsHabitacionConTareaPendiente();
}