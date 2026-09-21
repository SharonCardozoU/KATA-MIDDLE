package com.kata.evaluacion.motor.lenguajes;

import org.springframework.stereotype.Component;

import com.kata.evaluacion.motor.EjecutorBase;
import com.kata.evaluacion.motor.LimitesEjecucion;
import com.kata.evaluacion.motor.sandbox.Sandbox;

/**
 * El plus de EC02: COBOL entra como una clase nueva. RegistroEjecutores,
 * OrquestadorEjecucion, el calificador y los controladores no se tocan.
 */
@Component
public class EjecutorCobol extends EjecutorBase {

    public EjecutorCobol(Sandbox sandbox, LimitesEjecucion limites) {
        super(sandbox, limites);
    }

    @Override
    public String lenguaje() {
        return "cobol";
    }

    @Override
    protected String imagenDocker() {
        return "kata-cobol:3";
    }

    @Override
    protected String nombreArchivoFuente() {
        return "solucion.cob";
    }

    @Override
    protected String comandoCompilacion() {
        return "cobc -x -free -o solucion solucion.cob";
    }

    @Override
    protected String comandoEjecucion() {
        return "./solucion";
    }
}
