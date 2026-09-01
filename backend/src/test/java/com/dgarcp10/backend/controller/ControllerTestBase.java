package com.dgarcp10.backend.controller;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.dgarcp10.backend.config.JwtUtil;
import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.UsuarioRepository;

import tools.jackson.databind.ObjectMapper;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class ControllerTestBase {
    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UsuarioRepository usuarioRepo;
    @Autowired protected JwtUtil jwtUtil;
    @Autowired protected BCryptPasswordEncoder encoder;
    protected Usuario nuevoUsuario(RolUsuario rol) {
        Usuario u = new Usuario();
        u.setUsername("u" + System.nanoTime());
        u.setEmail(u.getUsername() + "@test.es");
        u.setPasswordHash("x1234567");
        u.setNombre("Test");
        u.setApellido("Test");
        u.setRol(rol);
        u.setCreadoEn(Instant.now());
        return usuarioRepo.save(u);
    }
    protected Usuario nuevoUsuarioConPassword(RolUsuario rol, String passwordPlano) {
        Usuario u = nuevoUsuario(rol);
        u.setPasswordHash(encoder.encode(passwordPlano));
        return usuarioRepo.save(u);
    }
    protected String tokenDe(Usuario usuario) {
        return jwtUtil.generateToken(usuario.getId(), usuario.getUsername(), usuario.getRol().name());
    }
}