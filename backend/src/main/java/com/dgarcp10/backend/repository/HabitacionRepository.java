package com.dgarcp10.backend.repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dgarcp10.backend.model.Habitacion;
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {
    Optional<Habitacion> findByNumero(Integer numero);
    Long countByTipoHabitacionId(Long tipoHabitacionId);
    List<Habitacion> findByTipoHabitacionId(Long tipoHabitacionId);
    List<Habitacion> findByPendienteLimpiezaTrueOrProximaLimpiezaLessThanEqual(LocalDate fecha);
List<Habitacion> findByProximaLimpieza(LocalDate fecha);
}