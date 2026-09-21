package com.kata.evaluacion.motor.lenguajes;

import org.springframework.stereotype.Component;

import com.kata.evaluacion.motor.EjecutorBase;
import com.kata.evaluacion.motor.LimitesEjecucion;
import com.kata.evaluacion.motor.sandbox.Sandbox;

/**
 * Segunda estrategia de EC02. La imagen lleva tsc instalado de antemano porque
 * el contenedor corre sin red y no puede bajar paquetes en el momento del envio.
 */
@Component
public class EjecutorTypeScript extends EjecutorBase {

    public EjecutorTypeScript(Sandbox sandbox, LimitesEjecucion limites) {
        super(sandbox, limites);
    }

    @Override
    public String lenguaje() {
        return "typescript";
    }

    @Override
    protected String imagenDocker() {
        return "kata-typescript:20";
    }

    @Override
    protected String nombreArchivoFuente() {
        return "solucion.ts";
    }

    @Override
    protected String comandoCompilacion() {
        return "tsc solucion.ts --outDir . --esModuleInterop --module commonjs --target es2020 --strict false --skipLibCheck --typeRoots /usr/local/lib/node_modules/@types --types node";
    }

    @Override
    protected String comandoEjecucion() {
        return "node solucion.js";
    }
}
