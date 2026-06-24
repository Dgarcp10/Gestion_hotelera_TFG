package com.dgarcp10.backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bloqueo_habitacion")
public class BloqueoHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "estado_bloqueo", nullable = false)
    private EstadoBloqueo estado = EstadoBloqueo.ACTIVO;

    @Column(name = "confirmado_sin_reubicacion", nullable = false)
    private Boolean confirmadoSinReubicacion = false;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Habitacion getHabitacion() { return habitacion; }
    public void setHabitacion(Habitacion habitacion) { this.habitacion = habitacion; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Usuario getCreadoPor() { return creadoPor; }
    public void setCreadoPor(Usuario creadoPor) { this.creadoPor = creadoPor; }
    public EstadoBloqueo getEstado() { return estado; }
    public void setEstado(EstadoBloqueo estado) { this.estado = estado; }
    public Boolean getConfirmadoSinReubicacion() { return confirmadoSinReubicacion; }
    public void setConfirmadoSinReubicacion(Boolean confirmadoSinReubicacion) { this.confirmadoSinReubicacion = confirmadoSinReubicacion; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
