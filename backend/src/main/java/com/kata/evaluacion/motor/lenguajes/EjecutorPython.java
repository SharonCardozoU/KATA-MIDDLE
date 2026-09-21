package com.kata.evaluacion.motor.lenguajes;

import org.springframework.stereotype.Component;

import com.kata.evaluacion.motor.EjecutorBase;
import com.kata.evaluacion.motor.LimitesEjecucion;
import com.kata.evaluacion.motor.sandbox.Sandbox;

@Component
public class EjecutorPython extends EjecutorBase {

    public EjecutorPython(Sandbox sandbox, LimitesEjecucion limites) {
        super(sandbox, limites);
    }

    @Override
    public String lenguaje() {
        return "python";
    }

    @Override
    protected String imagenDocker() {
        return "python:3.12-alpine";
    }

    @Override
    protected String nombreArchivoFuente() {
        return "solucion.py";
    }

    @Override
    protected String comandoCompilacion() {
        return null;
    }

    @Override
    protected String comandoEjecucion() {
        return "python3 solucion.py";
    }
}
