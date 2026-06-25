package com.dgarcp10.backend.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "averia")
public class Averia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportado_por", nullable = false)
    private Usuario reportadoPor;

    @Column(columnDefinition = "TEXT")
    private String notas;

    // La linea comentada es para usar un tipo de columna personalizado, pero no es necesario si usamos EnumType.STRING
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // @Column(columnDefinition = "gravedad_averia", nullable = false)
    private GravedadAveria gravedad;

    // La linea comentada es para usar un tipo de columna personalizado, pero no es necesario si usamos EnumType.STRING
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // @Column(columnDefinition = "estado_averia", nullable = false)
    private EstadoAveria estado = EstadoAveria.ABIERTA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bloqueo_id")
    private BloqueoHabitacion bloqueo;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Habitacion getHabitacion() { return habitacion; }
    public void setHabitacion(Habitacion habitacion) { this.habitacion = habitacion; }
    public Usuario getReportadoPor() { return reportadoPor; }
    public void setReportadoPor(Usuario reportadoPor) { this.reportadoPor = reportadoPor; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public GravedadAveria getGravedad() { return gravedad; }
    public void setGravedad(GravedadAveria gravedad) { this.gravedad = gravedad; }
    public EstadoAveria getEstado() { return estado; }
    public void setEstado(EstadoAveria estado) { this.estado = estado; }
    public BloqueoHabitacion getBloqueo() { return bloqueo; }
    public void setBloqueo(BloqueoHabitacion bloqueo) { this.bloqueo = bloqueo; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
