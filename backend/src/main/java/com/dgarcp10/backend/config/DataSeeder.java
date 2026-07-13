package com.dgarcp10.backend.config;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.UsuarioRepository;
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {
    private final UsuarioRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final String jefeUsername;
    private final String jefeEmail;
    private final String jefePassword;
    public DataSeeder(
            UsuarioRepository repo,
            BCryptPasswordEncoder encoder,
            @Value("${app.jefe.username}") String jefeUsername,
            @Value("${app.jefe.email}") String jefeEmail,
            @Value("${app.jefe.password}") String jefePassword) {
        this.repo = repo;
        this.encoder = encoder;
        this.jefeUsername = jefeUsername;
        this.jefeEmail = jefeEmail;
        this.jefePassword = jefePassword;
    }
    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        Usuario jefe = new Usuario();
        jefe.setUsername(jefeUsername);
        jefe.setEmail(jefeEmail);
        jefe.setPasswordHash(encoder.encode(jefePassword));
        jefe.setNombre("Jefe");
        jefe.setApellido("Del Hotel");
        jefe.setRol(RolUsuario.JEFE);
        jefe.setPasswordTemporal(true);
        jefe.setCreadoEn(Instant.now());
        repo.save(jefe);
    }
}