package com.dgarcp10.backend.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.model.TareaLimpieza;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.UsuarioRepository;
import com.dgarcp10.backend.service.LimpiezaService;
@RestController
@RequestMapping("/api/tareas-limpieza")
@PreAuthorize("hasAnyRole('LIMPIEZA', 'JEFE')")
public class LimpiezaController {
    private final LimpiezaService limpiezaService;
    private final UsuarioRepository usuarioRepo;
    public LimpiezaController(LimpiezaService limpiezaService,
                              UsuarioRepository usuarioRepo) {
        this.limpiezaService = limpiezaService;
        this.usuarioRepo = usuarioRepo;
    }
    @GetMapping("/pendientes")
    public List<TareaLimpieza> pendientes() {
        return limpiezaService.tareasPendientes();
    }
    @GetMapping("/previstas")
    public List<Habitacion> previstas() {
        return limpiezaService.previstasManana();
    }
    @PostMapping("/{id}/completar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completar(@PathVariable Long id, Authentication auth) {
        String username = (String) auth.getPrincipal();
        Usuario usuario = usuarioRepo.findByUsername(username).orElseThrow();
        limpiezaService.completarTarea(id, usuario.getId());
    }
    @PostMapping("/programar")
    @ResponseStatus(HttpStatus.CREATED)
    public void programar(@RequestParam Integer numero) {
        limpiezaService.programarLimpieza(numero);
    }
}