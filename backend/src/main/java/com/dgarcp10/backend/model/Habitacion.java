package com.dgarcp10.backend.model;

import java.time.LocalDate;

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
@Table(name = "habitacion")
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private Integer numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_habitacion_id", nullable = false)
    private TipoHabitacion tipoHabitacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoHabitacion estado; // = EstadoHabitacion.LIBRE;

    @Column(name = "pendiente_limpieza", nullable = false)
    private Boolean pendienteLimpieza = false;

    @Column(nullable = false)
    private Boolean averiada = false;

    @Column(name = "proxima_limpieza")
    private LocalDate proximaLimpieza;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public TipoHabitacion getTipoHabitacion() { return tipoHabitacion; }
    public void setTipoHabitacion(TipoHabitacion tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }
    public EstadoHabitacion getEstado() { return estado; }
    public void setEstado(EstadoHabitacion estado) { this.estado = estado; }
    public Boolean getPendienteLimpieza() { return pendienteLimpieza; }
    public void setPendienteLimpieza(Boolean pendienteLimpieza) { this.pendienteLimpieza = pendienteLimpieza; }
    public Boolean getAveriada() { return averiada; }
    public void setAveriada(Boolean averiada) { this.averiada = averiada; }
    public LocalDate getProximaLimpieza() { return proximaLimpieza; }
    public void setProximaLimpieza(LocalDate proximaLimpieza) { this.proximaLimpieza = proximaLimpieza; }
}
