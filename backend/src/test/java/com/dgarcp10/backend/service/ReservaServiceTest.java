package com.dgarcp10.backend.service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.dgarcp10.backend.model.EstadoHabitacion;
import com.dgarcp10.backend.model.EstadoPago;
import com.dgarcp10.backend.model.EstadoReserva;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.Pago;
import com.dgarcp10.backend.model.Reserva;
import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.PagoRepository;
import com.dgarcp10.backend.repository.ReservaRepository;
import com.dgarcp10.backend.repository.TipoHabitacionRepository;
import com.dgarcp10.backend.repository.UsuarioRepository;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservaServiceTest {
    @Autowired ReservaService reservaService;
    @Autowired UsuarioRepository usuarioRepo;
    @Autowired TipoHabitacionRepository tipoRepo;
    @Autowired HabitacionRepository habitacionRepo;
    @Autowired ReservaRepository reservaRepo;
    @Autowired PagoRepository pagoRepo;
    @Autowired PasarelaService pasarelaService;
    private static final LocalDate HOY = LocalDate.now();
    private Usuario nuevoUsuario(RolUsuario rol) {
        Usuario u = new Usuario();
        u.setUsername("u" + System.nanoTime());
        u.setEmail(u.getUsername() + "@test.es");
        u.setPasswordHash("x");
        u.setNombre("Test");
        u.setApellido("Test");
        u.setRol(rol);
        u.setCreadoEn(Instant.now());
        return usuarioRepo.save(u);
    }
    private TipoHabitacion nuevoTipo(String nombre, int capacidad) {
        TipoHabitacion t = new TipoHabitacion();
        t.setNombre(nombre);
        t.setCapacidad(capacidad);
        t.setPrecioBase(new BigDecimal("100.00"));
        return tipoRepo.save(t);
    }
    private Habitacion nuevaHabitacion(int numero, TipoHabitacion tipo) {
        Habitacion h = new Habitacion();
        h.setNumero(numero);
        h.setTipoHabitacion(tipo);
        h.setEstado(EstadoHabitacion.LIBRE);
        h.setPendienteLimpieza(false);
        h.setAveriada(false);
        return habitacionRepo.save(h);
    }
    private Reserva nuevaReservaPendienteFutura(Usuario huesped, TipoHabitacion tipo,
                                                 LocalDate entrada, BigDecimal total) {
        Reserva r = new Reserva();
        r.setUsuario(huesped);
        r.setTipoHabitacion(tipo);
        r.setFechaEntrada(entrada);
        r.setFechaSalida(entrada.plusDays(3));
        r.setPrecioTotal(total);
        r.setImporteCobrado(total);
        r.setEstado(EstadoReserva.PENDIENTE);
        r.setCreadoEn(Instant.now());
        r = reservaRepo.save(r);
        Pago p = new Pago();
        p.setReserva(r);
        p.setImporte(total);
        p.setEstado(EstadoPago.COMPLETADO);
        p.setReferencia("PAY-TEST");
        p.setCreadoEn(Instant.now());
        r.setPago(pagoRepo.save(p));
        return reservaRepo.save(r);
    }
    @Test
    void infoCancelacion_calcula10Y90() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        Reserva r = nuevaReservaPendienteFutura(u, nuevoTipo("Doble", 2), HOY.plusDays(10),
            new BigDecimal("400.00"));
        InfoCancelacion info = reservaService.infoCancelacion(r.getId(), u.getId());
        assertEquals(10, info.penalizacionPorcentaje());
        assertEquals(new BigDecimal("40.00"), info.importeCobrado());
        assertEquals(new BigDecimal("360.00"), info.importeReembolsar());
    }
    @Test
    void cancelar_aplicaCanceladaYReembolso() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        Reserva r = nuevaReservaPendienteFutura(u, nuevoTipo("Doble", 2), HOY.plusDays(10),
            new BigDecimal("400.00"));
        Reserva cancelada = reservaService.cancelar(r.getId(), u.getId());
        assertEquals(EstadoReserva.CANCELADA, cancelada.getEstado());
        assertEquals(new BigDecimal("40.00"), cancelada.getImporteCobrado());
        assertEquals(EstadoPago.REEMBOLSADO, cancelada.getPago().getEstado());
    }
    @Test
    void cancelar_noPropietario_lanzaForbidden() {
        Usuario dueno = nuevoUsuario(RolUsuario.USUARIO);
        Usuario otro = nuevoUsuario(RolUsuario.USUARIO);
        Reserva r = nuevaReservaPendienteFutura(dueno, nuevoTipo("Doble", 2), HOY.plusDays(10),
            new BigDecimal("400.00"));
        assertThrows(ResponseStatusException.class, () -> reservaService.cancelar(r.getId(), otro.getId()));
    }
    @Test
    void cancelar_entradaPasada_lanza() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        Reserva r = new Reserva();
        r.setUsuario(u);
        r.setTipoHabitacion(tipo);
        r.setFechaEntrada(HOY.minusDays(1));
        r.setFechaSalida(HOY.plusDays(2));
        r.setPrecioTotal(new BigDecimal("300.00"));
        r.setImporteCobrado(new BigDecimal("300.00"));
        r.setEstado(EstadoReserva.PENDIENTE);
        r.setCreadoEn(Instant.now());
        r = reservaRepo.save(r);
        Long reservaId = r.getId();
        assertThrows(IllegalStateException.class, () -> reservaService.cancelar(reservaId, u.getId()));
    }
    @Test
    void cancelar_reservaEnCurso_lanza() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        Reserva r = new Reserva();
        r.setUsuario(u);
        r.setTipoHabitacion(tipo);
        r.setFechaEntrada(HOY.minusDays(1));
        r.setFechaSalida(HOY.plusDays(2));
        r.setPrecioTotal(new BigDecimal("300.00"));
        r.setImporteCobrado(new BigDecimal("300.00"));
        r.setEstado(EstadoReserva.EN_CURSO);
        r.setCreadoEn(Instant.now());
        r = reservaRepo.save(r);
        Long reservaId = r.getId();
        assertThrows(IllegalStateException.class, () -> reservaService.cancelar(reservaId, u.getId()));
    }
    @Test
    void crearReserva_validaYPersistePago() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Individual", 1);
        nuevaHabitacion(301, tipo);
        Reserva r = reservaService.crearReserva(u.getId(), tipo.getId(),
            HOY.plusDays(2), HOY.plusDays(4), "1234567890123456", "12/30", "123");
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertEquals(new BigDecimal("200.00"), r.getPrecioTotal());
        assertTrue(r.getPago() != null);
        assertEquals(EstadoPago.COMPLETADO, r.getPago().getEstado());
        assertTrue(r.getPago().getReferencia().startsWith("PAY-"));
    }
    @Test
    void crearReserva_tarjetaRechazada_noPersisteNada() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Individual", 1);
        nuevaHabitacion(302, tipo);
        assertThrows(IllegalStateException.class, () -> reservaService.crearReserva(u.getId(),
            tipo.getId(), HOY.plusDays(2), HOY.plusDays(4), "1234567890120000", "12/30", "123"));
        assertTrue(reservaRepo.findAll().stream().noneMatch(r -> r.getTipoHabitacion().getId().equals(tipo.getId())));
    }
    @Test
    void modificar_subePrecio_cobraLActualiza() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        nuevaHabitacion(310, tipo);
        Reserva r = nuevaReservaPendienteFutura(u, tipo, HOY.plusDays(10), new BigDecimal("100.00"));
        Reserva modificada = reservaService.modificar(r.getId(), u.getId(), tipo.getId(),
            HOY.plusDays(10), HOY.plusDays(16), "1234567890123456", "12/30", "123");
        assertEquals(new BigDecimal("600.00"), modificada.getPrecioTotal());
        assertEquals(new BigDecimal("600.00"), modificada.getImporteCobrado());
        assertEquals(EstadoPago.COMPLETADO, modificada.getPago().getEstado());
        assertEquals(new BigDecimal("600.00"), modificada.getPago().getImporte());
    }
    @Test
    void modificar_bajaPrecio_reembolsa() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        nuevaHabitacion(311, tipo);
        Reserva r = nuevaReservaPendienteFutura(u, tipo, HOY.plusDays(10), new BigDecimal("400.00"));
        Reserva modificada = reservaService.modificar(r.getId(), u.getId(), tipo.getId(),
            HOY.plusDays(10), HOY.plusDays(11), null, null, null);
        assertEquals(new BigDecimal("100.00"), modificada.getPrecioTotal());
        assertEquals(new BigDecimal("100.00"), modificada.getImporteCobrado());
        assertEquals(EstadoPago.REEMBOLSADO, modificada.getPago().getEstado());
        assertEquals(new BigDecimal("100.00"), modificada.getPago().getImporte());
    }
    @Test
    void modificar_igualPrecio_keepsCompletado() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        nuevaHabitacion(312, tipo);
        Reserva r = nuevaReservaPendienteFutura(u, tipo, HOY.plusDays(10), new BigDecimal("100.00"));
        Reserva modificada = reservaService.modificar(r.getId(), u.getId(), tipo.getId(),
            HOY.plusDays(10), HOY.plusDays(11), null, null, null);
        assertEquals(new BigDecimal("100.00"), modificada.getPrecioTotal());
        assertEquals(EstadoPago.COMPLETADO, modificada.getPago().getEstado());
    }
    @Test
    void modificar_pagoRechazado_noCambiaNada() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        nuevaHabitacion(313, tipo);
        Reserva r = nuevaReservaPendienteFutura(u, tipo, HOY.plusDays(10), new BigDecimal("100.00"));
        assertThrows(IllegalStateException.class, () -> reservaService.modificar(r.getId(), u.getId(),
            tipo.getId(), HOY.plusDays(10), HOY.plusDays(16), "1234567890120000", "12/30", "123"));
        Reserva sinCambios = reservaService.obtenerReserva(r.getId());
        assertEquals(new BigDecimal("100.00"), sinCambios.getPrecioTotal());
        assertEquals(EstadoPago.COMPLETADO, sinCambios.getPago().getEstado());
        assertEquals(new BigDecimal("100.00"), sinCambios.getPago().getImporte());
    }
    @Test
    void modificar_otroUsuario_lanzaForbidden() {
        Usuario dueno = nuevoUsuario(RolUsuario.USUARIO);
        Usuario otro = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        nuevaHabitacion(314, tipo);
        Reserva r = nuevaReservaPendienteFutura(dueno, tipo, HOY.plusDays(10), new BigDecimal("100.00"));
        assertThrows(ResponseStatusException.class, () -> reservaService.modificar(r.getId(), otro.getId(),
            tipo.getId(), HOY.plusDays(10), HOY.plusDays(11), null, null, null));
    }
    @Test
    void modificar_sinDisponibilidad_lanza() {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Doble", 2);
        nuevaHabitacion(315, tipo);
        Reserva rA = nuevaReservaPendienteFutura(u, tipo, HOY.plusDays(10), new BigDecimal("100.00"));
        nuevaReservaPendienteFutura(u, tipo, HOY.plusDays(12), new BigDecimal("100.00"));
        assertThrows(IllegalStateException.class, () -> reservaService.modificar(rA.getId(), u.getId(),
            tipo.getId(), HOY.plusDays(12), HOY.plusDays(14), null, null, null));
    }
}