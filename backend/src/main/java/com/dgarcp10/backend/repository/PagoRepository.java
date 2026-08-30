package com.dgarcp10.backend.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dgarcp10.backend.model.Pago;
public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByReservaId(Long reservaId);
}