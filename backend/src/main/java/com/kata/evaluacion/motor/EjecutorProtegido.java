package com.kata.evaluacion.motor;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kata.evaluacion.motor.modelo.CasoPrueba;
import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;

/**
 * Proxy de proteccion. Comparte la interfaz Ejecutor, asi que ni el orquestador
 * ni los ejecutores por lenguaje saben que existe: todo el control de admision
 * queda en un solo punto.
 *
 * Realiza dos tacticas: validar la entrada (EC01) y limitar la respuesta a un
 * evento acotando la concurrencia, para que un pico de envios no tumbe el
 * servicio (EC03).
 */
public class EjecutorProtegido implements Ejecutor {

    private static final Logger log = LoggerFactory.getLogger(EjecutorProtegido.class);

    private final Ejecutor delegado;
    private final LimitesEjecucion limites;
    private final Semaphore cupos;

    public EjecutorProtegido(Ejecutor delegado, LimitesEjecucion limites, Semaphore cupos) {
        this.delegado = delegado;
        this.limites = limites;
        this.cupos = cupos;
    }

    @Override
    public String lenguaje() {
        return delegado.lenguaje();
    }

    @Override
    public ResultadoEjecucion ejecutar(SolicitudEjecucion solicitud) {
        validar(solicitud);

        if (!cupos.tryAcquire()) {
            throw new CapacidadExcedidaException(
                    "El motor ya atiende " + limites.getMaxEjecucionesConcurrentes()
                            + " ejecuciones. Reintente en unos segundos.");
        }
        try {
            log.info("Ejecutando envio en {} con {} casos", solicitud.lenguaje(), solicitud.casos().size());
            return delegado.ejecutar(solicitud);
        } finally {
            cupos.release();
        }
    }

    private void validar(SolicitudEjecucion solicitud) {
        if (solicitud.codigoFuente() == null || solicitud.codigoFuente().isBlank()) {
            throw new SolicitudInvalidaException("El codigo fuente no puede estar vacio");
        }
        if (solicitud.codigoFuente().getBytes().length > limites.getMaxTamanoCodigoBytes()) {
            throw new SolicitudInvalidaException(
                    "El codigo fuente supera " + limites.getMaxTamanoCodigoBytes() + " bytes");
        }
        if (solicitud.casos() == null || solicitud.casos().isEmpty()) {
            throw new SolicitudInvalidaException("Se requiere al menos un caso de prueba");
        }
        if (solicitud.casos().size() > limites.getMaxCasosPorEnvio()) {
            throw new SolicitudInvalidaException(
                    "Maximo " + limites.getMaxCasosPorEnvio() + " casos por envio");
        }
        for (CasoPrueba caso : solicitud.casos()) {
            if (caso.entrada() != null && caso.entrada().getBytes().length > limites.getMaxTamanoEntradaBytes()) {
                throw new SolicitudInvalidaException(
                        "La entrada de un caso supera " + limites.getMaxTamanoEntradaBytes() + " bytes");
            }
        }
    }
}
