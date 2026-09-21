package com.kata.evaluacion.api;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kata.evaluacion.aplicacion.ServicioEvaluaciones;
import com.kata.evaluacion.aplicacion.ServicioPreguntas;
import com.kata.evaluacion.dominio.Evaluacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionController {

    private final ServicioEvaluaciones servicio;
    private final ServicioPreguntas preguntas;

    public EvaluacionController(ServicioEvaluaciones servicio, ServicioPreguntas preguntas) {
        this.servicio = servicio;
        this.preguntas = preguntas;
    }

    @GetMapping
    public List<RespuestaEvaluacion> listar() {
        return servicio.listar().stream().map(this::respuesta).toList();
    }

    @GetMapping("/{id}")
    public RespuestaEvaluacion obtener(@PathVariable Long id) {
        return respuesta(servicio.obtener(id));
    }

    @PostMapping
    public ResponseEntity<RespuestaEvaluacion> crear(@Valid @RequestBody PeticionEvaluacion peticion) {
        Evaluacion creada = servicio.crear(
                peticion.nombre(),
                peticion.descripcion() == null ? "" : peticion.descripcion(),
                peticion.tiempoLimiteMinutos(),
                peticion.cantidadPreguntas());
        return ResponseEntity.created(URI.create("/api/evaluaciones/" + creada.getId()))
                .body(respuesta(creada));
    }

    @PutMapping("/{id}")
    public RespuestaEvaluacion actualizar(@PathVariable Long id, @Valid @RequestBody PeticionEvaluacion peticion) {
        Evaluacion actualizada = servicio.actualizar(
                id,
                peticion.nombre(),
                peticion.descripcion() == null ? "" : peticion.descripcion(),
                peticion.tiempoLimiteMinutos(),
                peticion.cantidadPreguntas(),
                preguntas.contar(id));
        return respuesta(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private RespuestaEvaluacion respuesta(Evaluacion evaluacion) {
        return RespuestaEvaluacion.desde(evaluacion, (int) preguntas.contar(evaluacion.getId()));
    }

    public record PeticionEvaluacion(
            @NotBlank @Size(max = 200) String nombre,
            @Size(max = 2000) String descripcion,
            @Min(1) @Max(480) int tiempoLimiteMinutos,
            @Min(1) @Max(50) int cantidadPreguntas) {
    }

    public record RespuestaEvaluacion(
            Long id,
            String nombre,
            String descripcion,
            int tiempoLimiteMinutos,
            int cantidadPreguntas,
            int preguntasRegistradas,
            Instant creadoEn) {

        static RespuestaEvaluacion desde(Evaluacion evaluacion, int preguntasRegistradas) {
            return new RespuestaEvaluacion(
                    evaluacion.getId(),
                    evaluacion.getNombre(),
                    evaluacion.getDescripcion(),
                    evaluacion.getTiempoLimiteMinutos(),
                    evaluacion.getCantidadPreguntas(),
                    preguntasRegistradas,
                    evaluacion.getCreadoEn());
        }
    }
}
