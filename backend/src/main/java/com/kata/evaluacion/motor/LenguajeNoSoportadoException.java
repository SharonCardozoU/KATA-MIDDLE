package com.kata.evaluacion.motor;

import java.util.Set;

public class LenguajeNoSoportadoException extends RuntimeException {

    public LenguajeNoSoportadoException(String lenguaje, Set<String> soportados) {
        super("El lenguaje '" + lenguaje + "' no esta soportado. Disponibles: " + soportados);
    }
}
