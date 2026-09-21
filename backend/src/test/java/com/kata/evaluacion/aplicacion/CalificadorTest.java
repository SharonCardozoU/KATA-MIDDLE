package com.kata.evaluacion.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.kata.evaluacion.motor.modelo.EstadoCaso;
import com.kata.evaluacion.motor.modelo.ResultadoCaso;
import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;

class CalificadorTest {

    private final Calificador calificador = new Calificador();

    @Test
    void compilacionFallidaDaCero() {
        Calificacion nota = calificador.calificar(ResultadoEjecucion.fallaCompilacion("error"), 10);

        assertEquals(0, nota.casosExitosos());
        assertEquals(0, nota.totalCasos());
        assertEquals(0d, nota.porcentaje());
        assertEquals(0d, nota.puntaje());
    }

    @Test
    void cuatroDeCincoDanOchentaPorCiento() {
        Calificacion nota = calificador.calificar(ResultadoEjecucion.compilado(List.of(
                ok(0), ok(1), ok(2), ok(3), fallido(4))), 10);

        assertEquals(4, nota.casosExitosos());
        assertEquals(5, nota.totalCasos());
        assertEquals(80d, nota.porcentaje());
        assertEquals(8d, nota.puntaje());
    }

    @Test
    void todosCorrectosDanElPuntajeMaximo() {
        Calificacion nota = calificador.calificar(ResultadoEjecucion.compilado(List.of(ok(0), ok(1))), 7.5);

        assertEquals(100d, nota.porcentaje());
        assertEquals(7.5, nota.puntaje());
    }

    @Test
    void sinCasosElPorcentajeEsCero() {
        Calificacion nota = calificador.calificar(ResultadoEjecucion.compilado(List.of()), 10);

        assertEquals(0, nota.casosExitosos());
        assertEquals(0, nota.totalCasos());
        assertEquals(0d, nota.porcentaje());
        assertEquals(0d, nota.puntaje());
    }

    private static ResultadoCaso ok(int indice) {
        return new ResultadoCaso(indice, "1", "2", "2", EstadoCaso.OK, null, 10);
    }

    private static ResultadoCaso fallido(int indice) {
        return new ResultadoCaso(indice, "1", "2", "9", EstadoCaso.SALIDA_DISTINTA, null, 10);
    }
}
