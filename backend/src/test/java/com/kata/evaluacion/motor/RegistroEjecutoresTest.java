package com.kata.evaluacion.motor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;

class RegistroEjecutoresTest {

    @Test
    void descubreEstrategiasSinListaFija() {
        RegistroEjecutores registro = new RegistroEjecutores(
                List.of(new Fijo("python"), new Fijo("cobol")),
                new LimitesEjecucion());

        assertEquals(Set.of("python", "cobol"), registro.lenguajesSoportados());
        assertEquals("cobol", registro.para("COBOL").lenguaje());
    }

    @Test
    void lenguajeDesconocidoSeRechaza() {
        RegistroEjecutores registro = new RegistroEjecutores(List.of(new Fijo("python")), new LimitesEjecucion());

        assertThrows(LenguajeNoSoportadoException.class, () -> registro.para("ruby"));
    }

    @Test
    void envuelveCadaEstrategiaConElProxy() {
        RegistroEjecutores registro = new RegistroEjecutores(List.of(new Fijo("python")), new LimitesEjecucion());

        assertTrue(registro.para("python") instanceof EjecutorProtegido);
    }

    private static final class Fijo implements Ejecutor {
        private final String lenguaje;

        private Fijo(String lenguaje) {
            this.lenguaje = lenguaje;
        }

        @Override
        public String lenguaje() {
            return lenguaje;
        }

        @Override
        public ResultadoEjecucion ejecutar(SolicitudEjecucion solicitud) {
            return ResultadoEjecucion.compilado(List.of());
        }
    }
}
