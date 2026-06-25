package com.dgarcp10.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// Carga el archivo .env desde la raíz del módulo backend
        Dotenv dotenv = Dotenv.configure()
                .directory("./") // Busca en el directorio actual (backend/)
                .ignoreIfMissing() // Evita que falle en producción si las variables ya están en el sistema
                .load();

        // Inyecta las variables en las propiedades del sistema de Java
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );

        // Arranca la aplicación Spring normalmente
		SpringApplication.run(BackendApplication.class, args);
	}

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
