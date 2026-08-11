package com.dgarcp10.backend.repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dgarcp10.backend.model.Habitacion;
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {
    Optional<Habitacion> findByNumero(Integer numero);
    Long countByTipoHabitacionId(Long tipoHabitacionId);
    List<Habitacion> findByTipoHabitacionId(Long tipoHabitacionId);
    List<Habitacion> findByPendienteLimpiezaTrueOrProximaLimpiezaLessThanEqual(LocalDate fecha);
    
    @Query("SELECT h FROM Habitacion h WHERE h.proximaLimpieza <= :fecha " +
       "OR EXISTS (SELECT 1 FROM TareaLimpieza t WHERE t.habitacion = h AND t.completadaEn IS NULL) " +
       "ORDER BY h.proximaLimpieza ASC NULLS LAST")
    List<Habitacion> findHabitacionesParaLimpiar(@Param("fecha") LocalDate fecha);  
}