package com.dgarcp10.backend.service;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
class PasarelaServiceTest {
    private final PasarelaService pasarela = new PasarelaService();
    @Test
    void tarjetaValida_noLanza() {
        pasarela.validarFormato("1234567890123456", "12/30", "123");
    }
    @Test
    void numeroDemasiadoCorto_lanza() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> pasarela.validarFormato("1234", "12/30", "123"));
        assertTrue(ex.getMessage().contains("Número de tarjeta inválido"));
    }
    @Test
    void numeroConLetras_lanza() {
        assertThrows(IllegalArgumentException.class,
            () -> pasarela.validarFormato("12345678abcdef12", "12/30", "123"));
    }
    @Test
    void caducidadFormatoInvalido_lanza() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> pasarela.validarFormato("1234567890123456", "30/12", "123"));
        assertTrue(ex.getMessage().contains("Fecha de caducidad inválida"));
    }
    @Test
    void caducidadPasada_lanza() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> pasarela.validarFormato("1234567890123456", "01/20", "123"));
        assertTrue(ex.getMessage().contains("Tarjeta caducada"));
    }
    @Test
    void cvvInvalido_lanza() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> pasarela.validarFormato("1234567890123456", "12/30", "12"));
        assertTrue(ex.getMessage().contains("CVV inválido"));
    }
    @Test
    void tarjeta0000_procesarDevuelveFalso() {
        assertFalse(pasarela.procesar("1234567890120000"));
        assertTrue(pasarela.procesar("1234567890123456"));
    }
}