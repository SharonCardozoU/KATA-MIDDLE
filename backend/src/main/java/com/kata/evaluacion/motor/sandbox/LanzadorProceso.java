package com.kata.evaluacion.motor.sandbox;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
interface LanzadorProceso {
    Process iniciar(List<String> argumentos) throws IOException;
}
