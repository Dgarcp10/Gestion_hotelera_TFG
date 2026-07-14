package com.dgarcp10.backend.config;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.dgarcp10.backend.model.RolUsuario;
import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.TipoHabitacionRepository;
import com.dgarcp10.backend.repository.UsuarioRepository;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {
    
    private final UsuarioRepository usuarioRepo;
    private final TipoHabitacionRepository tipoHabitacionRepo;
    private final BCryptPasswordEncoder encoder;
    private final String jefeUsername;
    private final String jefeEmail;
    private final String jefePassword;
    public DataSeeder(
            UsuarioRepository usuarioRepo,
            TipoHabitacionRepository tipoHabitacionRepo,
            BCryptPasswordEncoder encoder,
            @Value("${app.jefe.username}") String jefeUsername,
            @Value("${app.jefe.email}") String jefeEmail,
            @Value("${app.jefe.password}") String jefePassword) {
        this.usuarioRepo = usuarioRepo;
        this.tipoHabitacionRepo = tipoHabitacionRepo;
        this.encoder = encoder;
        this.jefeUsername = jefeUsername;
        this.jefeEmail = jefeEmail;
        this.jefePassword = jefePassword;
    }
    @Override
    public void run(String... args) {
        if (usuarioRepo.count() == 0) {
            Usuario jefe = new Usuario();
            jefe.setUsername(jefeUsername);
            jefe.setEmail(jefeEmail);
            jefe.setPasswordHash(encoder.encode(jefePassword));
            jefe.setNombre("Jefe");
            jefe.setApellido("Del Hotel");
            jefe.setRol(RolUsuario.JEFE);
            jefe.setPasswordTemporal(true);
            jefe.setCreadoEn(Instant.now());
            usuarioRepo.save(jefe);
        }
        
        if (tipoHabitacionRepo.count() == 0) {
            TipoHabitacion ind = new TipoHabitacion();
            ind.setNombre("Individual");
            ind.setCapacidad(1);
            ind.setPrecioBase(new BigDecimal("50.00"));
            TipoHabitacion dob = new TipoHabitacion();
            dob.setNombre("Doble");
            dob.setCapacidad(2);
            dob.setPrecioBase(new BigDecimal("80.00"));
            TipoHabitacion tri = new TipoHabitacion();
            tri.setNombre("Triple");
            tri.setCapacidad(3);
            tri.setPrecioBase(new BigDecimal("110.00"));
            tipoHabitacionRepo.saveAll(List.of(ind, dob, tri));
        }
    }
}