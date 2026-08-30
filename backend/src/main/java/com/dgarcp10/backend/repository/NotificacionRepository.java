package com.dgarcp10.backend.repository;
import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dgarcp10.backend.model.Notificacion;
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);
    List<Notificacion> findByCreadoEnBetweenOrderByCreadoEnDesc(Instant desde, Instant hasta);
}