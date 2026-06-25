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
@Table(name = "tarea_limpieza")
public class TareaLimpieza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    // La linea comentada es para usar un tipo de columna personalizado, pero no es necesario si usamos EnumType.STRING
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // @Column(columnDefinition = "tipo_tarea_limpieza", nullable = false)
    private TipoTareaLimpieza tipo;

    @Column(nullable = false)
    private Boolean accionable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completada_por")
    private Usuario completadoPor;

    @Column(name = "completada_en")
    private Instant completadaEn;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Habitacion getHabitacion() { return habitacion; }
    public void setHabitacion(Habitacion habitacion) { this.habitacion = habitacion; }
    public TipoTareaLimpieza getTipo() { return tipo; }
    public void setTipo(TipoTareaLimpieza tipo) { this.tipo = tipo; }
    public Boolean getAccionable() { return accionable; }
    public void setAccionable(Boolean accionable) { this.accionable = accionable; }
    public Usuario getCompletadoPor() { return completadoPor; }
    public void setCompletadoPor(Usuario completadoPor) { this.completadoPor = completadoPor; }
    public Instant getCompletadaEn() { return completadaEn; }
    public void setCompletadaEn(Instant completadaEn) { this.completadaEn = completadaEn; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
