package com.dgarcp10.backend.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dgarcp10.backend.model.Averia;
import com.dgarcp10.backend.model.EstadoAveria;
public interface AveriaRepository extends JpaRepository<Averia, Long> {
    Optional<Averia> findByHabitacionIdAndEstado(Long habitacionId, EstadoAveria estado);
    List<Averia> findByEstado(EstadoAveria estado);
}