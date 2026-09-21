package com.kata.evaluacion.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kata.evaluacion.aplicacion.EvaluacionEnvio;
import com.kata.evaluacion.aplicacion.ServicioIntentos;
import com.kata.evaluacion.aplicacion.ServicioIntentos.ResumenIntento;
import com.kata.evaluacion.dominio.Intento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/evaluaciones/{evaluacionId}/intentos")
public class IntentoController {

    private final ServicioIntentos servicio;

    public IntentoController(ServicioIntentos servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<InicioIntento> crear(@PathVariable Long evaluacionId) {
        Intento intento = servicio.crear(evaluacionId);
        return ResponseEntity
                .created(URI.create("/api/evaluaciones/" + evaluacionId + "/intentos/" + intento.getId()))
                .body(new InicioIntento(intento.getId(), evaluacionId));
    }

    @GetMapping("/{intentoId}")
    public ResumenIntento obtener(@PathVariable Long evaluacionId, @PathVariable Long intentoId) {
        return servicio.resumen(evaluacionId, intentoId);
    }

    @PostMapping("/{intentoId}/preguntas/{preguntaId}/ejecuciones")
    public EvaluacionEnvio resolver(
            @PathVariable Long evaluacionId,
            @PathVariable Long intentoId,
            @PathVariable Long preguntaId,
            @Valid @RequestBody PeticionResolucion peticion) {
        return servicio.resolver(evaluacionId, intentoId, preguntaId, peticion.lenguaje(), peticion.codigoFuente());
    }

    public record PeticionResolucion(@NotBlank String lenguaje, @NotBlank String codigoFuente) {
    }

    public record InicioIntento(Long id, Long evaluacionId) {
    }
}
