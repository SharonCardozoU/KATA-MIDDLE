package com.kata.evaluacion.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.kata.evaluacion.aplicacion.RecursoNoEncontradoException;
import com.kata.evaluacion.motor.CapacidadExcedidaException;
import com.kata.evaluacion.motor.LenguajeNoSoportadoException;
import com.kata.evaluacion.motor.SolicitudInvalidaException;

class ManejadorErroresTest {

    private final ManejadorErrores manejador = new ManejadorErrores();

    @Test
    void solicitudInvalidaDevuelve400() {
        ResponseEntity<Map<String, Object>> respuesta = manejador.solicitudInvalida(
                new SolicitudInvalidaException("codigo vacio"));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(400, respuesta.getBody().get("estado"));
        assertEquals("codigo vacio", respuesta.getBody().get("mensaje"));
        assertNotNull(respuesta.getBody().get("instante"));
    }

    @Test
    void mensajeNuloNoRevientaElCuerpo() {
        ResponseEntity<Map<String, Object>> respuesta = manejador.noEncontrado(
                new RecursoNoEncontradoException(null));

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Not Found", respuesta.getBody().get("mensaje"));
    }

    @Test
    void lenguajeNoSoportadoDevuelve422() {
        ResponseEntity<Map<String, Object>> respuesta = manejador.lenguajeNoSoportado(
                new LenguajeNoSoportadoException("ruby", Set.of("python")));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, respuesta.getStatusCode());
        assertEquals(422, respuesta.getBody().get("estado"));
    }

    @Test
    void capacidadExcedidaDevuelve429() {
        ResponseEntity<Map<String, Object>> respuesta = manejador.capacidadExcedida(
                new CapacidadExcedidaException("sin cupo"));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, respuesta.getStatusCode());
        assertEquals("sin cupo", respuesta.getBody().get("mensaje"));
    }

    @Test
    void validacionConcatenaCampos() {
        BeanPropertyBindingResult errores = new BeanPropertyBindingResult(new Object(), "peticion");
        errores.addError(new FieldError("peticion", "nombre", "no debe estar vacio"));
        MethodArgumentNotValidException excepcion = new MethodArgumentNotValidException(null, errores);

        ResponseEntity<Map<String, Object>> respuesta = manejador.validacion(excepcion);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("nombre: no debe estar vacio", respuesta.getBody().get("mensaje"));
    }

    @Test
    void validacionSinErroresUsaLaFraseDelEstado() {
        BeanPropertyBindingResult errores = new BeanPropertyBindingResult(new Object(), "peticion");
        MethodArgumentNotValidException excepcion = new MethodArgumentNotValidException(null, errores);

        ResponseEntity<Map<String, Object>> respuesta = manejador.validacion(excepcion);

        assertEquals("Bad Request", respuesta.getBody().get("mensaje"));
        assertEquals(List.of(), errores.getFieldErrors());
    }
}
