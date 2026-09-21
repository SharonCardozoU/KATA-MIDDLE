package com.kata.evaluacion.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kata.evaluacion.aplicacion.ServicioIntentos.ResumenIntento;
import com.kata.evaluacion.dominio.Evaluacion;
import com.kata.evaluacion.dominio.Intento;
import com.kata.evaluacion.dominio.IntentoRepositorio;
import com.kata.evaluacion.dominio.Pregunta;
import com.kata.evaluacion.dominio.RespuestaIntento;
import com.kata.evaluacion.dominio.RespuestaIntentoRepositorio;
import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;

@ExtendWith(MockitoExtension.class)
class ServicioIntentosTest {

    @Mock
    private IntentoRepositorio intentos;
    @Mock
    private RespuestaIntentoRepositorio respuestas;
    @Mock
    private ServicioEvaluaciones evaluaciones;
    @Mock
    private ServicioPreguntas preguntas;
    @Mock
    private ServicioResolucion resolucion;

    @InjectMocks
    private ServicioIntentos servicio;

    @Test
    void crearPersisteElIntento() {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 2);
        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(intentos.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Intento creado = servicio.crear(1L);

        assertEquals(evaluacion, creado.getEvaluacion());
    }

    @Test
    void resumenMezclaPendientesYRespondidas() throws Exception {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 2);
        asignarId(evaluacion, 1L);
        Intento intento = new Intento(evaluacion);
        asignarId(intento, 9L);

        Pregunta pendiente = new Pregunta(evaluacion, "A", "d", "python", BigDecimal.TEN);
        asignarId(pendiente, 1L);
        Pregunta respondida = new Pregunta(evaluacion, "B", "d", "python", BigDecimal.TEN);
        asignarId(respondida, 2L);

        RespuestaIntento respuesta = new RespuestaIntento(intento, respondida);
        respuesta.actualizar("python", 2, 2, BigDecimal.valueOf(100), BigDecimal.TEN, BigDecimal.TEN, true);
        intento.getRespuestas().add(respuesta);

        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(intentos.findById(9L)).thenReturn(Optional.of(intento));
        when(preguntas.listar(1L)).thenReturn(List.of(pendiente, respondida));

        ResumenIntento resumen = servicio.resumen(1L, 9L);

        assertEquals(1, resumen.preguntasRespondidas());
        assertEquals(2, resumen.totalPreguntas());
        assertEquals(1, resumen.preguntasCorrectas());
        assertEquals(0, resumen.preguntasIncorrectas());
        assertEquals(10d, resumen.puntajeObtenido());
        assertEquals(20d, resumen.puntajeMaximo());
        assertEquals(50d, resumen.porcentaje());
        assertEquals("PENDIENTE", resumen.preguntas().get(0).estado());
        assertEquals("CORRECTA", resumen.preguntas().get(1).estado());
        assertTrue(resumen.tiempoRestanteSegundos() > 0);
    }

    @Test
    void resumenMarcaIncorrectaSiFalloUnCaso() throws Exception {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 1);
        asignarId(evaluacion, 1L);
        Intento intento = new Intento(evaluacion);
        asignarId(intento, 9L);
        Pregunta pregunta = new Pregunta(evaluacion, "A", "d", "python", BigDecimal.TEN);
        asignarId(pregunta, 1L);
        RespuestaIntento respuesta = new RespuestaIntento(intento, pregunta);
        respuesta.actualizar("python", 1, 2, BigDecimal.valueOf(50), BigDecimal.valueOf(5), BigDecimal.TEN, true);
        intento.getRespuestas().add(respuesta);

        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(intentos.findById(9L)).thenReturn(Optional.of(intento));
        when(preguntas.listar(1L)).thenReturn(List.of(pregunta));

        ResumenIntento resumen = servicio.resumen(1L, 9L);

        assertEquals("INCORRECTA", resumen.preguntas().get(0).estado());
        assertEquals(1, resumen.preguntasIncorrectas());
    }

    @Test
    void resumenSinBancoDaCero() throws Exception {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 0);
        asignarId(evaluacion, 1L);
        Intento intento = new Intento(evaluacion);
        asignarId(intento, 9L);
        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(intentos.findById(9L)).thenReturn(Optional.of(intento));
        when(preguntas.listar(1L)).thenReturn(List.of());

        assertEquals(0d, servicio.resumen(1L, 9L).porcentaje());
    }

    @Test
    void resumenRechazaIntentoDeOtraEvaluacion() throws Exception {
        Evaluacion una = new Evaluacion("A", "d", 60, 1);
        asignarId(una, 1L);
        Evaluacion otra = new Evaluacion("B", "d", 60, 1);
        asignarId(otra, 2L);
        Intento intento = new Intento(otra);
        asignarId(intento, 9L);
        when(evaluaciones.obtener(1L)).thenReturn(una);
        when(intentos.findById(9L)).thenReturn(Optional.of(intento));

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.resumen(1L, 9L));
    }

    @Test
    void resolverGuardaLaRespuesta() throws Exception {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 1);
        asignarId(evaluacion, 1L);
        Intento intento = new Intento(evaluacion);
        asignarId(intento, 9L);
        Pregunta pregunta = new Pregunta(evaluacion, "A", "d", "python", BigDecimal.TEN);
        asignarId(pregunta, 4L);
        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(intentos.findById(9L)).thenReturn(Optional.of(intento));
        when(preguntas.obtener(1L, 4L)).thenReturn(pregunta);
        when(resolucion.resolver(1L, 4L, "python", "print(1)")).thenReturn(
                new EvaluacionEnvio(ResultadoEjecucion.compilado(List.of()), new Calificacion(1, 1, 100, 10), 8));
        when(respuestas.findByIntentoIdAndPreguntaId(9L, 4L)).thenReturn(Optional.empty());
        when(respuestas.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EvaluacionEnvio envio = servicio.resolver(1L, 9L, 4L, "python", "print(1)");

        assertEquals(10d, envio.calificacion().puntaje());
    }

    private static void asignarId(Object entidad, long id) throws Exception {
        Field campo = entidad.getClass().getDeclaredField("id");
        campo.setAccessible(true);
        campo.set(entidad, id);
    }
}
