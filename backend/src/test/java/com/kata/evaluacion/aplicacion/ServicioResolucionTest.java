package com.kata.evaluacion.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kata.evaluacion.dominio.Evaluacion;
import com.kata.evaluacion.dominio.Pregunta;
import com.kata.evaluacion.motor.modelo.CasoPrueba;
import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;

@ExtendWith(MockitoExtension.class)
class ServicioResolucionTest {

    @Mock
    private ServicioPreguntas preguntas;
    @Mock
    private OrquestadorEjecucion orquestador;

    @InjectMocks
    private ServicioResolucion servicio;

    @Test
    void usaLosCasosPersistidosYNoLosDelCandidato() {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 1);
        Pregunta pregunta = new Pregunta(evaluacion, "Suma", "enunciado", "python", BigDecimal.TEN);
        pregunta.agregarCaso("1 2", "3");
        when(preguntas.obtener(1L, 2L)).thenReturn(pregunta);
        when(orquestador.evaluar(any(SolicitudEjecucion.class), eq(10d)))
                .thenReturn(new EvaluacionEnvio(ResultadoEjecucion.compilado(List.of()), new Calificacion(1, 1, 100, 10), 5));

        EvaluacionEnvio envio = servicio.resolver(1L, 2L, "python", "print(3)");

        assertEquals(10d, envio.calificacion().puntaje());
        org.mockito.ArgumentCaptor<SolicitudEjecucion> captor = org.mockito.ArgumentCaptor.forClass(SolicitudEjecucion.class);
        org.mockito.Mockito.verify(orquestador).evaluar(captor.capture(), eq(10d));
        List<CasoPrueba> casos = captor.getValue().casos();
        assertEquals(1, casos.size());
        assertEquals("1 2", casos.get(0).entrada());
        assertEquals("3", casos.get(0).salidaEsperada());
        assertEquals("print(3)", captor.getValue().codigoFuente());
    }
}
