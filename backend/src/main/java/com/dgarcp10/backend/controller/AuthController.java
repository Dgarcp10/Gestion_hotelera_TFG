package com.dgarcp10.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dgarcp10.backend.config.JwtUtil;
import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.UsuarioRepository;
import com.dgarcp10.backend.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;

    public AuthController(UsuarioRepository repo,
                          BCryptPasswordEncoder encoder,
                          JwtUtil jwtUtil,
                          UsuarioService usuarioService) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Usuario usuario = repo.findByUsername(username)
            .orElse(null);

        if (usuario == null || !encoder.matches(password, usuario.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Credenciales inválidas"));
        }

        String token = jwtUtil.generateToken(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getRol().name()
        );

        return ResponseEntity.ok(Map.of(
            "token", token,
            "usuarioId", usuario.getId(),
            "username", usuario.getUsername(),
            "rol", usuario.getRol().name()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        if (repo.findByUsername(body.get("username")).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "El usuario ya existe"));
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(body.get("username"));
        usuario.setEmail(body.get("email"));
        usuario.setPasswordHash(body.get("password"));
        usuario.setNombre(body.get("nombre"));
        usuario.setApellido(body.get("apellido"));
        usuario.setRol(RolUsuario.USUARIO);
        usuario.setPasswordTemporal(false);
        Usuario creado = usuarioService.crear(usuario);
        String token = jwtUtil.generateToken(
            creado.getId(), creado.getUsername(), creado.getRol().name()
        );
        return ResponseEntity.ok(Map.of(
            "token", token,
            "usuarioId", creado.getId(),
            "username", creado.getUsername(),
            "rol", creado.getRol().name()
        ));
    }
}