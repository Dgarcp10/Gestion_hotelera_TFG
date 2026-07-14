package com.dgarcp10.backend.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dgarcp10.backend.model.Habitacion;
import com.dgarcp10.backend.service.HabitacionService;
@RestController
@RequestMapping("/api/habitaciones")
public class HabitacionController {
    private final HabitacionService service;
    public HabitacionController(HabitacionService service) {
        this.service = service;
    }
    @GetMapping
    public List<Habitacion> listar(@RequestParam(required = false) Long tipoId) {
        if (tipoId != null) return service.listarPorTipo(tipoId);
        return service.listarTodos();
    }
    @GetMapping("/{id}")
    public Habitacion obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Habitacion crear(@RequestBody Habitacion habitacion) {
        return service.crear(habitacion);
    }
    @PutMapping("/{id}")
    public Habitacion actualizar(@PathVariable Long id, @RequestBody Habitacion habitacion) {
        return service.actualizar(id, habitacion);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}