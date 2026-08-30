package com.dgarcp10.backend.service;
import java.math.BigDecimal;
public record InfoCancelacion(int penalizacionPorcentaje, BigDecimal importeCobrado,
                              BigDecimal importeReembolsar) {}