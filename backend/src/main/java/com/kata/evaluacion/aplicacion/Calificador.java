package com.kata.evaluacion.aplicacion;

import org.springframework.stereotype.Component;

import com.kata.evaluacion.motor.modelo.ResultadoCaso;
import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;

/**
 * Pure Fabrication de GRASP: la regla de puntaje no pertenece ni al ejecutor ni
 * al controlador, y aislarla permite cambiarla sin tocar el motor.
 */
@Component
public class Calificador {

    public Calificacion calificar(ResultadoEjecucion resultado, double puntajeMaximo) {
        if (!resultado.compilacionExitosa()) {
            return new Calificacion(0, 0, 0d, 0d);
        }

        int total = resultado.casos().size();
        int exitosos = (int) resultado.casos().stream().filter(ResultadoCaso::exitoso).count();
        double porcentaje = total == 0 ? 0d : (double) exitosos / total * 100d;

        return new Calificacion(exitosos, total, redondear(porcentaje), redondear(porcentaje / 100d * puntajeMaximo));
    }

    private double redondear(double valor) {
        return Math.round(valor * 100d) / 100d;
    }
}
