package com.kata.evaluacion.motor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.kata.evaluacion.motor.lenguajes.EjecutorCobol;
import com.kata.evaluacion.motor.lenguajes.EjecutorJava;
import com.kata.evaluacion.motor.lenguajes.EjecutorJavaScript;
import com.kata.evaluacion.motor.lenguajes.EjecutorPython;
import com.kata.evaluacion.motor.lenguajes.EjecutorTypeScript;
import com.kata.evaluacion.motor.modelo.CasoPrueba;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;
import com.kata.evaluacion.motor.sandbox.ResultadoComando;
import com.kata.evaluacion.motor.sandbox.Sandbox;

class PerfilLenguajesTest {

    private final Sandbox sandbox = spec -> new ResultadoComando(1, "", "skip", false, 0);
    private final LimitesEjecucion limites = new LimitesEjecucion();

    @Test
    void cadaEstrategiaDeclaraSuLenguaje() {
        assertEquals("python", new EjecutorPython(sandbox, limites).lenguaje());
        assertEquals("java", new EjecutorJava(sandbox, limites).lenguaje());
        assertEquals("javascript", new EjecutorJavaScript(sandbox, limites).lenguaje());
        assertEquals("typescript", new EjecutorTypeScript(sandbox, limites).lenguaje());
        assertEquals("cobol", new EjecutorCobol(sandbox, limites).lenguaje());
    }

    @Test
    void lasEstrategiasUsanElTemplate() {
        SolicitudEjecucion solicitud = new SolicitudEjecucion("python", "print(1)", List.of(new CasoPrueba("1", "2")));

        assertFalse(new EjecutorPython(sandbox, limites).ejecutar(solicitud).compilacionExitosa());
        assertFalse(new EjecutorJava(sandbox, limites).ejecutar(solicitud).compilacionExitosa());
        assertFalse(new EjecutorJavaScript(sandbox, limites).ejecutar(solicitud).compilacionExitosa());
        assertFalse(new EjecutorTypeScript(sandbox, limites).ejecutar(solicitud).compilacionExitosa());
        assertFalse(new EjecutorCobol(sandbox, limites).ejecutar(solicitud).compilacionExitosa());
    }
}
