package com.kata.evaluacion.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kata.evaluacion.motor.RegistroEjecutores;
import com.kata.evaluacion.motor.modelo.CasoPrueba;
import com.kata.evaluacion.motor.modelo.EstadoCaso;
import com.kata.evaluacion.motor.modelo.ResultadoCaso;
import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;

@ExtendWith(MockitoExtension.class)
class OrquestadorEjecucionTest {

    @Mock
    private RegistroEjecutores registro;
    @Mock
    private com.kata.evaluacion.motor.Ejecutor ejecutor;

    private final Calificador calificador = new Calificador();

    @Test
    void evaluarComponeMotorYNota() {
        OrquestadorEjecucion orquestador = new OrquestadorEjecucion(registro, calificador);
        SolicitudEjecucion solicitud = new SolicitudEjecucion("python", "print(2)", List.of(new CasoPrueba("1", "2")));
        when(registro.para("python")).thenReturn(ejecutor);
        when(ejecutor.ejecutar(solicitud)).thenReturn(ResultadoEjecucion.compilado(List.of(
                new ResultadoCaso(0, "1", "2", "2", EstadoCaso.OK, null, 3))));

        EvaluacionEnvio envio = orquestador.evaluar(solicitud, 10);

        assertEquals(1, envio.calificacion().casosExitosos());
        assertEquals(10d, envio.calificacion().puntaje());
        verify(ejecutor).ejecutar(solicitud);
    }

    @Test
    void lenguajesSoportadosDelega() {
        when(registro.lenguajesSoportados()).thenReturn(Set.of("python", "java"));
        OrquestadorEjecucion orquestador = new OrquestadorEjecucion(registro, calificador);

        assertEquals(Set.of("python", "java"), orquestador.lenguajesSoportados());
    }
}
