package com.dgarcp10.backend.controller;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgarcp10.backend.model.EstadoHabitacion;
import com.dgarcp10.backend.model.EstadoReserva;
import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.Reserva;
import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.HabitacionRepository;
import com.dgarcp10.backend.repository.ReservaRepository;
import com.dgarcp10.backend.repository.TipoHabitacionRepository;
class DashboardControllerTest extends ControllerTestBase {
    @Autowired HabitacionRepository habitacionRepo;
    @Autowired TipoHabitacionRepository tipoRepo;
    @Autowired ReservaRepository reservaRepo;
    private TipoHabitacion nuevoTipo(String nombre) {
        TipoHabitacion t = new TipoHabitacion();
        t.setNombre(nombre);
        t.setCapacidad(2);
        t.setPrecioBase(new BigDecimal("100.00"));
        return tipoRepo.save(t);
    }
    private Habitacion nuevaHabitacion(int numero, TipoHabitacion tipo, EstadoHabitacion estado) {
        Habitacion h = new Habitacion();
        h.setNumero(numero);
        h.setTipoHabitacion(tipo);
        h.setEstado(estado);
        h.setPendienteLimpieza(false);
        h.setAveriada(false);
        return habitacionRepo.save(h);
    }
    private Reserva nuevaEstancia(Usuario huesped, TipoHabitacion tipo, Habitacion hab,
                                 LocalDate entrada, LocalDate salida) {
        Reserva r = new Reserva();
        r.setUsuario(huesped);
        r.setTipoHabitacion(tipo);
        r.setFechaEntrada(entrada);
        r.setFechaSalida(salida);
        r.setPrecioTotal(new BigDecimal("200.00"));
        r.setImporteCobrado(new BigDecimal("200.00"));
        r.setEstado(EstadoReserva.EN_CURSO);
        r.setCreadoEn(Instant.now());
        r.setHabitacion(hab);
        return reservaRepo.save(r);
    }
    @Test
    void graficos_jefe_200_conAnioYLists() throws Exception {
        Usuario jefe = nuevoUsuario(RolUsuario.JEFE);
        Usuario huesped = nuevoUsuario(RolUsuario.USUARIO);
        TipoHabitacion tipo = nuevoTipo("Doble");
        Habitacion hab = nuevaHabitacion(501, tipo, EstadoHabitacion.OCUPADA);
        nuevaEstancia(huesped, tipo, hab, LocalDate.now().minusDays(1), LocalDate.now().plusDays(3));
        mockMvc.perform(get("/api/dashboard/graficos")
            .header("Authorization", "Bearer " + tokenDe(jefe)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.anio").value(YearMonth.now().getYear()))
            .andExpect(jsonPath("$.ocupacionMensual").isArray())
            .andExpect(jsonPath("$.gananciasMensuales").isArray());
    }
    @Test
    void graficos_sinToken_noAutorizado() throws Exception {
        mockMvc.perform(get("/api/dashboard/graficos"))
            .andExpect(status().isUnauthorized());
    }
    @Test
    void graficos_usuarioNormal_forbidden() throws Exception {
        Usuario cliente = nuevoUsuario(RolUsuario.USUARIO);
        mockMvc.perform(get("/api/dashboard/graficos")
            .header("Authorization", "Bearer " + tokenDe(cliente)))
            .andExpect(status().isForbidden());
    }
    @Test
    void graficos_anioInvalido_badRequest() throws Exception {
        Usuario jefe = nuevoUsuario(RolUsuario.JEFE);
        mockMvc.perform(get("/api/dashboard/graficos")
            .header("Authorization", "Bearer " + tokenDe(jefe))
            .param("anio", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Año inválido: 0"));
    }
    @Test
    void seguimiento_hastaAnteriorA_badRequest() throws Exception {
        Usuario jefe = nuevoUsuario(RolUsuario.JEFE);
        mockMvc.perform(get("/api/dashboard/seguimiento")
            .header("Authorization", "Bearer " + tokenDe(jefe))
            .param("desde", LocalDate.now().toString())
            .param("hasta", LocalDate.now().minusDays(2).toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("La fecha 'hasta' debe ser posterior o igual a 'desde'"));
    }
}