package com.dgarcp10.backend.controller;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.service.UsuarioService;
@RestController
@RequestMapping("/api/usuarios")
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
    public Usuario crear(@RequestBody Usuario usuario) { return service.crear(usuario); }
    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        return service.actualizar(id, usuario);
    }
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}