package com.kata.evaluacion.motor.lenguajes;

import org.springframework.stereotype.Component;

import com.kata.evaluacion.motor.EjecutorBase;
import com.kata.evaluacion.motor.LimitesEjecucion;
import com.kata.evaluacion.motor.sandbox.Sandbox;

@Component
public class EjecutorJava extends EjecutorBase {

    public EjecutorJava(Sandbox sandbox, LimitesEjecucion limites) {
        super(sandbox, limites);
    }

    @Override
    public String lenguaje() {
        return "java";
    }

    @Override
    protected String imagenDocker() {
        return "eclipse-temurin:21-jdk";
    }

    @Override
    protected String nombreArchivoFuente() {
        return "Solucion.java";
    }

    @Override
    protected String comandoCompilacion() {
        return "javac Solucion.java";
    }

    @Override
    protected String comandoEjecucion() {
        return "java Solucion";
    }
}
