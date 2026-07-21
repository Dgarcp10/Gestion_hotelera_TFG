package com.dgarcp10.backend.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dgarcp10.backend.model.TareaLimpieza;
public interface TareaLimpiezaRepository extends JpaRepository<TareaLimpieza, Long> {
    List<TareaLimpieza> findByCompletadaEnIsNullOrderByCreadoEnDesc();
    Optional<TareaLimpieza> findByHabitacionIdAndCompletadaEnIsNull(Long habitacionId);
}