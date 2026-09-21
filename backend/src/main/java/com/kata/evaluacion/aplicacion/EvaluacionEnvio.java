package com.kata.evaluacion.aplicacion;

import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;

public record EvaluacionEnvio(ResultadoEjecucion ejecucion, Calificacion calificacion, long milisegundosTotales) {
}
