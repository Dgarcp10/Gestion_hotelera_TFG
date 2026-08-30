package com.dgarcp10.backend.service;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Service;
@Service
public class PasarelaService {
    private static final DateTimeFormatter FORMATO_CADUCIDAD = DateTimeFormatter.ofPattern("MM/yy");
    public void validarFormato(String numero, String caducidad, String cvv) {
        if (numero == null || !numero.matches("\\d{16}")) {
            throw new IllegalArgumentException("Número de tarjeta inválido");
        }
        YearMonth ym;
        try {
            ym = YearMonth.parse(caducidad, FORMATO_CADUCIDAD);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("Fecha de caducidad inválida");
        }
        if (YearMonth.now().isAfter(ym)) {
            throw new IllegalArgumentException("Tarjeta caducada");
        }
        if (cvv == null || !cvv.matches("\\d{3}")) {
            throw new IllegalArgumentException("CVV inválido");
        }
    }
    public boolean procesar(String numero) {
        return !numero.endsWith("0000");
    }
}