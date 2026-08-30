package com.dgarcp10.backend.controller;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dgarcp10.backend.model.Averia;
import com.dgarcp10.backend.model.EstadoAveria;
import com.dgarcp10.backend.model.GravedadAveria;
import com.dgarcp10.backend.repository.UsuarioRepository;
import com.dgarcp10.backend.service.AveriaService;
import com.dgarcp10.backend.service.BloqueoService;
import com.dgarcp10.backend.service.ReubicacionPreview;
@RestController
@RequestMapping("/api/averias")
@PreAuthorize("hasAnyRole('LIMPIEZA', 'MANTENIMIENTO', 'RECEPCION', 'JEFE')")
public class AveriaController {
    private final AveriaService averiaService;
    private final UsuarioRepository usuarioRepo;
    private final BloqueoService bloqueoService;
    public AveriaController(AveriaService averiaService, UsuarioRepository usuarioRepo, BloqueoService bloqueoService) {
        this.averiaService = averiaService;
        this.usuarioRepo = usuarioRepo;
        this.bloqueoService = bloqueoService;
    }
    @GetMapping
    public List<Averia> listar(@RequestParam(required = false) EstadoAveria estado) {
        return averiaService.listar(estado);
    }
    @GetMapping("/{id}")
    public Averia obtener(@PathVariable Long id) {
        return averiaService.obtener(id);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Averia crear(@RequestParam Long habitacionId,
                        @RequestParam GravedadAveria gravedad,
                        @RequestParam(required = false) String notas,
                        @RequestParam(required = false) LocalDate fechaInicio,
                        @RequestParam(required = false) LocalDate fechaFin,
                        @RequestParam(defaultValue = "false") boolean confirmarSinReubicacion,
                        Authentication auth) {
        return averiaService.crear(habitacionId, gravedad, notas, obtenerUsuarioId(auth),
            fechaInicio, fechaFin, confirmarSinReubicacion);
    }
    @GetMapping("/preview")
    public ReubicacionPreview preview(@RequestParam Long habitacionId,
                                    @RequestParam(required = false) LocalDate fechaInicio,
                                    @RequestParam(required = false) LocalDate fechaFin) {
        return bloqueoService.previewReubicacion(habitacionId, fechaInicio, fechaFin);
    }
    @PutMapping("/{id}")
    public Averia actualizar(@PathVariable Long id,
                            @RequestParam(required = false) GravedadAveria gravedad,
                            @RequestParam(required = false) String notas,
                            @RequestParam(required = false) LocalDate fechaInicio,
                            @RequestParam(required = false) LocalDate fechaFin,
                            @RequestParam(defaultValue = "false") boolean confirmarSinReubicacion,
                            Authentication auth) {
        return averiaService.actualizar(id, gravedad, notas, fechaInicio, fechaFin,
            confirmarSinReubicacion, obtenerUsuarioId(auth));
    }
    @PostMapping("/{id}/resolver")
    public Averia resolver(@PathVariable Long id, Authentication auth) {
        return averiaService.resolver(id, obtenerUsuarioId(auth));
    }
    private Long obtenerUsuarioId(Authentication auth) {
        String username = (String) auth.getPrincipal();
        return usuarioRepo.findByUsername(username).orElseThrow().getId();
    }
}