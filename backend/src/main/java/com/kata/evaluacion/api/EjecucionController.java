package com.kata.evaluacion.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kata.evaluacion.aplicacion.EvaluacionEnvio;
import com.kata.evaluacion.aplicacion.OrquestadorEjecucion;
import com.kata.evaluacion.motor.modelo.CasoPrueba;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/api")
public class EjecucionController {

    private final OrquestadorEjecucion orquestador;

    public EjecucionController(OrquestadorEjecucion orquestador) {
        this.orquestador = orquestador;
    }

    @GetMapping("/lenguajes")
    public Set<String> lenguajes() {
        return orquestador.lenguajesSoportados();
    }

    @PostMapping("/ejecuciones")
    public ResponseEntity<EvaluacionEnvio> ejecutar(@Valid @RequestBody PeticionEjecucion peticion) {
        SolicitudEjecucion solicitud = new SolicitudEjecucion(
                peticion.lenguaje(),
                peticion.codigoFuente(),
                peticion.casos().stream().map(c -> new CasoPrueba(c.entrada(), c.salidaEsperada())).toList());

        return ResponseEntity.ok(orquestador.evaluar(solicitud, peticion.puntajeMaximo()));
    }

    @GetMapping("/salud")
    public Map<String, Object> salud() {
        return Map.of("estado", "arriba", "lenguajes", orquestador.lenguajesSoportados());
    }

    public record PeticionEjecucion(
            @NotBlank String lenguaje,
            @NotBlank String codigoFuente,
            @NotEmpty List<PeticionCaso> casos,
            double puntajeMaximo) {
    }

    public record PeticionCaso(String entrada, String salidaEsperada) {
    }
}
