package com.kata.evaluacion.motor.sandbox;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public record EspecificacionSandbox(
        Path directorioTrabajo,
        String imagen,
        List<String> comando,
        Duration tiempoMaximo,
        int memoriaMb,
        double cpus,
        int limitePids) {
}
