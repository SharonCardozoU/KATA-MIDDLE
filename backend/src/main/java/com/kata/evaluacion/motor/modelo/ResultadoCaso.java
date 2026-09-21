package com.kata.evaluacion.motor.modelo;

public record ResultadoCaso(
        int indice,
        String entrada,
        String salidaEsperada,
        String salidaObtenida,
        EstadoCaso estado,
        String mensajeError,
        long milisegundos) {

    public boolean exitoso() {
        return estado == EstadoCaso.OK;
    }
}
