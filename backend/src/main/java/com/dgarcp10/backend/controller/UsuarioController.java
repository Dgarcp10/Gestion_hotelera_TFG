package com.dgarcp10.backend.controller;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.service.UsuarioService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }
    @GetMapping
    public List<Usuario> listar() { return service.listarTodos(); }
    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable Long id) { return service.obtenerPorId(id); }
    @PostMapping
    public Usuario crear(@RequestBody Usuario usuario) { return service.crear(usuario); }
    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        return service.actualizar(id, usuario);
    }
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}