package com.kata.evaluacion.api;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kata.evaluacion.aplicacion.RecursoNoEncontradoException;
import com.kata.evaluacion.motor.CapacidadExcedidaException;
import com.kata.evaluacion.motor.LenguajeNoSoportadoException;
import com.kata.evaluacion.motor.SolicitudInvalidaException;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<Map<String, Object>> solicitudInvalida(SolicitudInvalidaException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(LenguajeNoSoportadoException.class)
    public ResponseEntity<Map<String, Object>> lenguajeNoSoportado(LenguajeNoSoportadoException e) {
        return respuesta(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(CapacidadExcedidaException.class)
    public ResponseEntity<Map<String, Object>> capacidadExcedida(CapacidadExcedidaException e) {
        return respuesta(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus estado, String mensaje) {
        String texto = (mensaje == null || mensaje.isBlank()) ? estado.getReasonPhrase() : mensaje;
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("instante", Instant.now().toString());
        cuerpo.put("estado", estado.value());
        cuerpo.put("mensaje", texto);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
