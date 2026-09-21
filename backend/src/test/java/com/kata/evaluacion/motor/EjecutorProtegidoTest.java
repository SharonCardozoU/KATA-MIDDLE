package com.kata.evaluacion.motor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.kata.evaluacion.motor.modelo.CasoPrueba;
import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;

class EjecutorProtegidoTest {

    @Test
    void rechazaCodigoVacio() {
        EjecutorProtegido proxy = nuevoProxy(limites(), solicitud -> ResultadoEjecucion.fallaCompilacion("no"));

        assertThrows(SolicitudInvalidaException.class, () -> proxy.ejecutar(solicitud("python", "", caso())));
    }

    @Test
    void rechazaCodigoDemasiadoGrande() {
        LimitesEjecucion limites = limites();
        limites.setMaxTamanoCodigoBytes(8);
        EjecutorProtegido proxy = nuevoProxy(limites, solicitud -> ResultadoEjecucion.fallaCompilacion("no"));

        assertThrows(SolicitudInvalidaException.class,
                () -> proxy.ejecutar(solicitud("python", "print(1)\nprint(2)\n", caso())));
    }

    @Test
    void rechazaCuandoNoHayCupo() throws Exception {
        LimitesEjecucion limites = limites();
        limites.setMaxEjecucionesConcurrentes(1);
        CountDownLatch ocupado = new CountDownLatch(1);
        CountDownLatch soltar = new CountDownLatch(1);
        AtomicInteger ejecuciones = new AtomicInteger();

        EjecutorProtegido proxy = nuevoProxy(limites, solicitud -> {
            ejecuciones.incrementAndGet();
            ocupado.countDown();
            await(soltar);
            return ResultadoEjecucion.compilado(List.of());
        });

        Thread ocupante = new Thread(() -> proxy.ejecutar(solicitud("python", "print(1)", caso())));
        ocupante.start();
        if (!ocupado.await(3, TimeUnit.SECONDS)) {
            soltar.countDown();
            throw new AssertionError("El primer envio no tomo el cupo");
        }

        assertThrows(CapacidadExcedidaException.class, () -> proxy.ejecutar(solicitud("python", "print(2)", caso())));
        soltar.countDown();
        ocupante.join(3_000);
        assertEquals(1, ejecuciones.get());
    }

    @Test
    void delegaSiLaSolicitudEsValida() {
        AtomicInteger llamadas = new AtomicInteger();
        EjecutorProtegido proxy = nuevoProxy(limites(), solicitud -> {
            llamadas.incrementAndGet();
            return ResultadoEjecucion.compilado(List.of());
        });

        ResultadoEjecucion resultado = proxy.ejecutar(solicitud("python", "print(1)", caso()));

        assertEquals(1, llamadas.get());
        assertEquals(true, resultado.compilacionExitosa());
    }

    private static EjecutorProtegido nuevoProxy(LimitesEjecucion limites, Ejecutor delegado) {
        return new EjecutorProtegido(delegado, limites, new java.util.concurrent.Semaphore(limites.getMaxEjecucionesConcurrentes()));
    }

    private static LimitesEjecucion limites() {
        LimitesEjecucion limites = new LimitesEjecucion();
        limites.setMaxTamanoCodigoBytes(1024);
        limites.setMaxCasosPorEnvio(5);
        limites.setMaxTamanoEntradaBytes(256);
        limites.setMaxEjecucionesConcurrentes(2);
        return limites;
    }

    private static SolicitudEjecucion solicitud(String lenguaje, String codigo, CasoPrueba caso) {
        return new SolicitudEjecucion(lenguaje, codigo, List.of(caso));
    }

    private static CasoPrueba caso() {
        return new CasoPrueba("1", "2");
    }

    private static void await(CountDownLatch pestillo) {
        try {
            pestillo.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private interface Ejecutor extends com.kata.evaluacion.motor.Ejecutor {
        @Override
        default String lenguaje() {
            return "python";
        }
    }
}
