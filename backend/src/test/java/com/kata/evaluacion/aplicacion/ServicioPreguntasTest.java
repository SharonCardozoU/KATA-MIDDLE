package com.kata.evaluacion.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kata.evaluacion.aplicacion.ServicioPreguntas.CasoEntrada;
import com.kata.evaluacion.dominio.Evaluacion;
import com.kata.evaluacion.dominio.Pregunta;
import com.kata.evaluacion.dominio.PreguntaRepositorio;
import com.kata.evaluacion.motor.LenguajeNoSoportadoException;
import com.kata.evaluacion.motor.RegistroEjecutores;
import com.kata.evaluacion.motor.SolicitudInvalidaException;

@ExtendWith(MockitoExtension.class)
class ServicioPreguntasTest {

    @Mock
    private PreguntaRepositorio repositorio;
    @Mock
    private ServicioEvaluaciones evaluaciones;
    @Mock
    private RegistroEjecutores ejecutores;

    @InjectMocks
    private ServicioPreguntas servicio;

    @Test
    void crearRechazaSiElCupoEstaLleno() {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 1);
        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(repositorio.countByEvaluacionId(1L)).thenReturn(1L);

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.crear(1L, "p", "enunciado", "python", 10, List.of(new CasoEntrada("1", "2"))));
    }

    @Test
    void crearRechazaSinCasos() {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 2);
        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(repositorio.countByEvaluacionId(1L)).thenReturn(0L);

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.crear(1L, "p", "enunciado", "python", 10, List.of()));
        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.crear(1L, "p", "enunciado", "python", 10, null));
    }

    @Test
    void crearRechazaLenguajeDesconocido() {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 2);
        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(repositorio.countByEvaluacionId(1L)).thenReturn(0L);
        when(ejecutores.lenguajesSoportados()).thenReturn(Set.of("python"));

        assertThrows(LenguajeNoSoportadoException.class,
                () -> servicio.crear(1L, "p", "enunciado", "ruby", 10, List.of(new CasoEntrada("1", "2"))));
    }

    @Test
    void crearRechazaPuntajeNoPositivo() {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 2);
        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(repositorio.countByEvaluacionId(1L)).thenReturn(0L);
        when(ejecutores.lenguajesSoportados()).thenReturn(Set.of("python"));

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.crear(1L, "p", "enunciado", "python", 0, List.of(new CasoEntrada("1", "2"))));
    }

    @Test
    void crearPersisteLaPregunta() {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 2);
        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(repositorio.countByEvaluacionId(1L)).thenReturn(0L);
        when(ejecutores.lenguajesSoportados()).thenReturn(Set.of("python"));
        when(repositorio.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pregunta creada = servicio.crear(1L, "  Suma  ", " enuncia ", "Python", 10,
                List.of(new CasoEntrada("1 2", "3")));

        assertEquals("Suma", creada.getNombre());
        assertEquals("python", creada.getLenguaje());
        assertEquals(0, BigDecimal.valueOf(10).compareTo(creada.getPuntaje()));
        assertEquals(1, creada.getCasos().size());
        assertEquals("1 2", creada.getCasos().get(0).getEntrada());
    }

    @Test
    void obtenerLanzaSiNoEstaEnLaEvaluacion() {
        when(evaluaciones.obtener(1L)).thenReturn(new Evaluacion("Kata", "d", 60, 2));
        when(repositorio.findByIdAndEvaluacionId(8L, 1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(1L, 8L));
    }

    @Test
    void actualizarReemplazaCasos() {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 2);
        Pregunta pregunta = new Pregunta(evaluacion, "Suma", "old", "python", BigDecimal.TEN);
        pregunta.agregarCaso("1", "1");
        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(repositorio.findByIdAndEvaluacionId(3L, 1L)).thenReturn(Optional.of(pregunta));
        when(ejecutores.lenguajesSoportados()).thenReturn(Set.of("java"));
        when(repositorio.save(pregunta)).thenReturn(pregunta);

        Pregunta actualizada = servicio.actualizar(1L, 3L, " Resta ", " nuevo ", "JAVA", 5,
                List.of(new CasoEntrada("4 1", "3")));

        assertEquals("Resta", actualizada.getNombre());
        assertEquals("java", actualizada.getLenguaje());
        assertEquals(1, actualizada.getCasos().size());
        assertEquals("4 1", actualizada.getCasos().get(0).getEntrada());
    }

    @Test
    void actualizarRechazaCasosVacios() {
        Evaluacion evaluacion = new Evaluacion("Kata", "d", 60, 2);
        Pregunta pregunta = new Pregunta(evaluacion, "Suma", "old", "python", BigDecimal.TEN);
        when(evaluaciones.obtener(1L)).thenReturn(evaluacion);
        when(repositorio.findByIdAndEvaluacionId(3L, 1L)).thenReturn(Optional.of(pregunta));

        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.actualizar(1L, 3L, "Suma", "d", "python", 5, List.of()));
    }

    @Test
    void listarYContarPasanPorLaEvaluacion() {
        when(evaluaciones.obtener(1L)).thenReturn(new Evaluacion("Kata", "d", 60, 2));
        when(repositorio.findByEvaluacionIdOrderByCreadoEnAsc(1L)).thenReturn(List.of());
        when(repositorio.countByEvaluacionId(1L)).thenReturn(0L);

        assertEquals(List.of(), servicio.listar(1L));
        assertEquals(0L, servicio.contar(1L));
    }
}
