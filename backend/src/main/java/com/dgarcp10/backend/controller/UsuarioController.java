package com.dgarcp10.backend.controller;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.service.UsuarioService;
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('JEFE')")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }
    @GetMapping
    public List<Usuario> listar() { return service.listarTodos(); }
    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable Long id) { return service.obtenerPorId(id); }
    @GetMapping("/username/{username}")
    public Usuario buscarPorUsername(@PathVariable String username) {
        return service.obtenerPorUsername(username);
    }
    @GetMapping("/email/{email}")
    public Usuario buscarPorEmail(@PathVariable String email) {
        return service.obtenerPorEmail(email);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> crear(@RequestBody Usuario usuario) {
        String passwordGenerada = generarPasswordTemporal();
        usuario.setPasswordHash(passwordGenerada);
        usuario.setPasswordTemporal(true);
        Usuario creado = service.crear(usuario);
        return Map.of(
            "id", creado.getId(),
            "username", creado.getUsername(),
            "passwordGenerado", passwordGenerada
        );
    }
    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        return service.actualizar(id, usuario);
    }
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }

    private String generarPasswordTemporal() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&";
        SecureRandom random = new SecureRandom();
        return random.ints(8, 0, chars.length())
            .mapToObj(chars::charAt)
            .map(String::valueOf)
            .collect(Collectors.joining());
    }
}