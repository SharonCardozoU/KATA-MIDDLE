package com.kata.evaluacion.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kata.evaluacion.dominio.Evaluacion;
import com.kata.evaluacion.dominio.EvaluacionRepositorio;
import com.kata.evaluacion.motor.SolicitudInvalidaException;

@ExtendWith(MockitoExtension.class)
class ServicioEvaluacionesTest {

    @Mock
    private EvaluacionRepositorio repositorio;

    @InjectMocks
    private ServicioEvaluaciones servicio;

    @Test
    void listarDelegaEnElRepositorio() {
        Evaluacion evaluacion = new Evaluacion("Kata", "desc", 60, 2);
        when(repositorio.findAllByOrderByCreadoEnDesc()).thenReturn(List.of(evaluacion));

        assertEquals(List.of(evaluacion), servicio.listar());
    }

    @Test
    void obtenerLanzaSiNoExiste() {
        when(repositorio.findById(9L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(9L));
    }

    @Test
    void crearPersisteElNombreLimpio() {
        when(repositorio.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Evaluacion creada = servicio.crear("  Kata  ", "lab", 45, 3);

        assertEquals("Kata", creada.getNombre());
        assertEquals("lab", creada.getDescripcion());
        assertEquals(45, creada.getTiempoLimiteMinutos());
        assertEquals(3, creada.getCantidadPreguntas());
    }

    @Test
    void actualizarRechazaSiElCupoQuedaPorDebajo() {
        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.actualizar(1L, "Kata", "d", 60, 1, 2));
    }

    @Test
    void actualizarCambiaLosCampos() {
        Evaluacion evaluacion = new Evaluacion("Viejo", "d", 60, 2);
        when(repositorio.findById(1L)).thenReturn(Optional.of(evaluacion));
        when(repositorio.save(evaluacion)).thenReturn(evaluacion);

        Evaluacion actualizada = servicio.actualizar(1L, " Nuevo ", null, 90, 4, 2);

        assertEquals("Nuevo", actualizada.getNombre());
        assertEquals("", actualizada.getDescripcion());
        assertEquals(90, actualizada.getTiempoLimiteMinutos());
        assertEquals(4, actualizada.getCantidadPreguntas());
    }

    @Test
    void eliminarRequiereQueExista() {
        when(repositorio.existsById(4L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.eliminar(4L));
    }

    @Test
    void eliminarBorraSiExiste() {
        when(repositorio.existsById(4L)).thenReturn(true);

        servicio.eliminar(4L);

        verify(repositorio).deleteById(4L);
    }
}
