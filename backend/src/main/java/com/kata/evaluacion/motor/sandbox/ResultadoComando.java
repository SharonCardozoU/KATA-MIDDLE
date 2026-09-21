package com.kata.evaluacion.motor.sandbox;

public record ResultadoComando(
        int codigoSalida,
        String salidaEstandar,
        String salidaError,
        boolean expiro,
        long milisegundos) {
}
