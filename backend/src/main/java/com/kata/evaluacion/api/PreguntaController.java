package com.kata.evaluacion.api;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kata.evaluacion.aplicacion.EvaluacionEnvio;
import com.kata.evaluacion.aplicacion.ServicioPreguntas;
import com.kata.evaluacion.aplicacion.ServicioPreguntas.CasoEntrada;
import com.kata.evaluacion.aplicacion.ServicioResolucion;
import com.kata.evaluacion.dominio.Pregunta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/evaluaciones/{evaluacionId}/preguntas")
public class PreguntaController {

    private final ServicioPreguntas servicio;
    private final ServicioResolucion resolucion;

    public PreguntaController(ServicioPreguntas servicio, ServicioResolucion resolucion) {
        this.servicio = servicio;
        this.resolucion = resolucion;
    }

    @GetMapping
    public List<RespuestaPregunta> listar(@PathVariable Long evaluacionId) {
        return servicio.listar(evaluacionId).stream().map(RespuestaPregunta::desde).toList();
    }

    @GetMapping("/{preguntaId}")
    public RespuestaPregunta obtener(@PathVariable Long evaluacionId, @PathVariable Long preguntaId) {
        return RespuestaPregunta.desde(servicio.obtener(evaluacionId, preguntaId));
    }

    @PostMapping("/{preguntaId}/ejecuciones")
    public EvaluacionEnvio resolver(
            @PathVariable Long evaluacionId,
            @PathVariable Long preguntaId,
            @Valid @RequestBody PeticionResolucion peticion) {
        return resolucion.resolver(evaluacionId, preguntaId, peticion.lenguaje(), peticion.codigoFuente());
    }

    @PostMapping
    public ResponseEntity<RespuestaPregunta> crear(
            @PathVariable Long evaluacionId,
            @Valid @RequestBody PeticionPregunta peticion) {
        Pregunta creada = servicio.crear(
                evaluacionId,
                peticion.nombre(),
                peticion.descripcion(),
                peticion.lenguaje(),
                peticion.puntaje(),
                peticion.casos().stream()
                        .map(caso -> new CasoEntrada(caso.entrada(), caso.salidaEsperada()))
                        .toList());
        return ResponseEntity
                .created(URI.create("/api/evaluaciones/" + evaluacionId + "/preguntas/" + creada.getId()))
                .body(RespuestaPregunta.desde(creada));
    }

    @PutMapping("/{preguntaId}")
    public RespuestaPregunta actualizar(
            @PathVariable Long evaluacionId,
            @PathVariable Long preguntaId,
            @Valid @RequestBody PeticionPregunta peticion) {
        return RespuestaPregunta.desde(servicio.actualizar(
                evaluacionId,
                preguntaId,
                peticion.nombre(),
                peticion.descripcion(),
                peticion.lenguaje(),
                peticion.puntaje(),
                peticion.casos().stream()
                        .map(caso -> new CasoEntrada(caso.entrada(), caso.salidaEsperada()))
                        .toList()));
    }

    public record PeticionPregunta(
            @NotBlank @Size(max = 200) String nombre,
            @NotBlank @Size(max = 4000) String descripcion,
            @NotBlank String lenguaje,
            @DecimalMin("0.01") @DecimalMax("1000") double puntaje,
            @NotEmpty List<PeticionCaso> casos) {
    }

    public record PeticionResolucion(@NotBlank String lenguaje, @NotBlank String codigoFuente) {
    }

    public record PeticionCaso(String entrada, @NotBlank String salidaEsperada) {
    }

    public record RespuestaCaso(int orden, String entrada, String salidaEsperada) {
    }

    public record RespuestaPregunta(
            Long id,
            String nombre,
            String descripcion,
            String lenguaje,
            double puntaje,
            Instant creadoEn,
            List<RespuestaCaso> casos) {

        static RespuestaPregunta desde(Pregunta pregunta) {
            return new RespuestaPregunta(
                    pregunta.getId(),
                    pregunta.getNombre(),
                    pregunta.getDescripcion(),
                    pregunta.getLenguaje(),
                    pregunta.getPuntaje().doubleValue(),
                    pregunta.getCreadoEn(),
                    pregunta.getCasos().stream()
                            .map(caso -> new RespuestaCaso(caso.getOrden(), caso.getEntrada(), caso.getSalidaEsperada()))
                            .toList());
        }
    }
}
