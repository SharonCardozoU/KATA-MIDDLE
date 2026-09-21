package com.kata.evaluacion.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.kata.evaluacion.aplicacion.Calificacion;
import com.kata.evaluacion.aplicacion.EvaluacionEnvio;
import com.kata.evaluacion.aplicacion.OrquestadorEjecucion;
import com.kata.evaluacion.aplicacion.ServicioEvaluaciones;
import com.kata.evaluacion.aplicacion.ServicioPreguntas;
import com.kata.evaluacion.dominio.Evaluacion;
import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;

@ExtendWith(MockitoExtension.class)
class ControladoresTest {

    @Mock
    private ServicioEvaluaciones evaluaciones;
    @Mock
    private ServicioPreguntas preguntas;
    @Mock
    private OrquestadorEjecucion orquestador;

    @Test
    void evaluacionesListaCreaYElimina() {
        EvaluacionController controlador = new EvaluacionController(evaluaciones, preguntas);
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 2);
        when(evaluaciones.listar()).thenReturn(List.of(evaluacion));
        when(preguntas.contar(null)).thenReturn(0L);
        when(evaluaciones.crear("Kata", "d", 60, 2)).thenReturn(evaluacion);
        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(evaluaciones.actualizar(eq(1L), eq("Kata"), eq("d"), eq(60), eq(2), eq(0L))).thenReturn(evaluacion);
        when(preguntas.contar(1L)).thenReturn(0L);

        assertEquals(1, controlador.listar().size());
        assertEquals("Kata", controlador.obtener(1L).nombre());
        assertEquals(HttpStatus.CREATED, controlador.crear(
                new EvaluacionController.PeticionEvaluacion("Kata", "d", 60, 2)).getStatusCode());
        assertEquals("Kata", controlador.actualizar(1L,
                new EvaluacionController.PeticionEvaluacion("Kata", "d", 60, 2)).nombre());
        assertEquals(HttpStatus.NO_CONTENT, controlador.eliminar(8L).getStatusCode());
        verify(evaluaciones).eliminar(8L);
    }

    @Test
    void ejecucionExponeLenguajesSaludYEnvio() {
        EjecucionController controlador = new EjecucionController(orquestador);
        when(orquestador.lenguajesSoportados()).thenReturn(Set.of("python"));
        when(orquestador.evaluar(any(SolicitudEjecucion.class), eq(10d)))
                .thenReturn(new EvaluacionEnvio(
                        ResultadoEjecucion.compilado(List.of()), new Calificacion(1, 1, 100, 10), 4));

        assertEquals(Set.of("python"), controlador.lenguajes());
        assertEquals("arriba", controlador.salud().get("estado"));
        assertEquals(HttpStatus.OK, controlador.ejecutar(new EjecucionController.PeticionEjecucion(
                "python", "print(1)",
                List.of(new EjecucionController.PeticionCaso("1", "2")), 10)).getStatusCode());
    }
}
