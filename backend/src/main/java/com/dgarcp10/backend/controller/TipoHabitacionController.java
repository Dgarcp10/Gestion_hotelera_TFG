package com.dgarcp10.backend.controller;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dgarcp10.backend.model.TipoHabitacion;
import com.dgarcp10.backend.repository.TipoHabitacionRepository;
@RestController
@RequestMapping("/api/tipos-habitacion")
public class TipoHabitacionController {
    private final TipoHabitacionRepository repo;
    public TipoHabitacionController(TipoHabitacionRepository repo) {
        this.repo = repo;
    }
    @GetMapping
    public List<TipoHabitacion> listar() {
        return repo.findAll();
    }
}