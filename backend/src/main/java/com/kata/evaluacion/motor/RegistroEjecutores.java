package com.kata.evaluacion.motor;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Descubre en arranque todas las estrategias declaradas como bean y envuelve
 * cada una en el proxy de proteccion. Es el punto que hace real la medida de
 * EC02: soportar un lenguaje nuevo es agregar una clase, no registrarla aqui.
 *
 * EjecutorProtegido no es un bean a proposito; si lo fuera, Spring lo inyectaria
 * en esta misma lista y el proxy se envolveria a si mismo.
 */
@Component
public class RegistroEjecutores {

    private static final Logger log = LoggerFactory.getLogger(RegistroEjecutores.class);

    private final Map<String, Ejecutor> porLenguaje;

    public RegistroEjecutores(List<Ejecutor> ejecutores, LimitesEjecucion limites) {
        Semaphore cupos = new Semaphore(limites.getMaxEjecucionesConcurrentes());
        this.porLenguaje = ejecutores.stream().collect(Collectors.toUnmodifiableMap(
                ejecutor -> ejecutor.lenguaje().toLowerCase(Locale.ROOT),
                ejecutor -> new EjecutorProtegido(ejecutor, limites, cupos)));
        log.info("Lenguajes soportados: {}", porLenguaje.keySet());
    }

    public Ejecutor para(String lenguaje) {
        String clave = lenguaje == null ? "" : lenguaje.toLowerCase(Locale.ROOT).strip();
        Ejecutor ejecutor = porLenguaje.get(clave);
        if (ejecutor == null) {
            throw new LenguajeNoSoportadoException(lenguaje, lenguajesSoportados());
        }
        return ejecutor;
    }

    public Set<String> lenguajesSoportados() {
        return porLenguaje.keySet();
    }
}
