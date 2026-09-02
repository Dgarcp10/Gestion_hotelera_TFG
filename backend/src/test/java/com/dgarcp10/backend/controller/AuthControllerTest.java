package com.dgarcp10.backend.controller;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.Usuario;
class AuthControllerTest extends ControllerTestBase {
    @Test
    void register_ok_devuelveTokenYrolUsuario() throws Exception {
        long sufijo = System.nanoTime();
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(Map.of(
                "username", "cliente" + sufijo,
                "email", "cliente" + sufijo + "@test.es",
                "password", "clave123",
                "nombre", "Ana",
                "apellido", "Perez"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.rol").value("USUARIO"));
    }
    @Test
    void register_usernameDuplicado_conflicto() throws Exception {
        Usuario u = nuevoUsuario(RolUsuario.USUARIO);
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(Map.of(
                "username", u.getUsername(),
                "email", "otro" + System.nanoTime() + "@test.es",
                "password", "clave123",
                "nombre", "Ana",
                "apellido", "Perez"))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("El usuario ya existe"));
    }
    @Test
    void login_ok_devuelveToken() throws Exception {
        Usuario u = nuevoUsuarioConPassword(RolUsuario.JEFE, "clave123");
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(Map.of(
                "username", u.getUsername(),
                "password", "clave123"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.rol").value("JEFE"));
    }
    @Test
    void login_passwordErroneo_noAutorizado() throws Exception {
        Usuario u = nuevoUsuarioConPassword(RolUsuario.USUARIO, "clave123");
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(Map.of(
                "username", u.getUsername(),
                "password", "claveMal"))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }
}