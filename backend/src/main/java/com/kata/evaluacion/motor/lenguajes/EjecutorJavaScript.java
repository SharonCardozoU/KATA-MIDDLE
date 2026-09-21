package com.kata.evaluacion.motor.lenguajes;

import org.springframework.stereotype.Component;

import com.kata.evaluacion.motor.EjecutorBase;
import com.kata.evaluacion.motor.LimitesEjecucion;
import com.kata.evaluacion.motor.sandbox.Sandbox;

@Component
public class EjecutorJavaScript extends EjecutorBase {

    public EjecutorJavaScript(Sandbox sandbox, LimitesEjecucion limites) {
        super(sandbox, limites);
    }

    @Override
    public String lenguaje() {
        return "javascript";
    }

    @Override
    protected String imagenDocker() {
        return "node:20-alpine";
    }

    @Override
    protected String nombreArchivoFuente() {
        return "solucion.js";
    }

    @Override
    protected String comandoCompilacion() {
        return null;
    }

    @Override
    protected String comandoEjecucion() {
        return "node solucion.js";
    }
}
