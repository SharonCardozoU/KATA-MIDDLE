package com.kata.evaluacion.motor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.kata.evaluacion.motor.modelo.CasoPrueba;
import com.kata.evaluacion.motor.modelo.EstadoCaso;
import com.kata.evaluacion.motor.modelo.ResultadoCaso;
import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;
import com.kata.evaluacion.motor.sandbox.ResultadoComando;
import com.kata.evaluacion.motor.sandbox.Sandbox;

class EjecutorBaseTest {

    @Test
    void casoCorrectoSeMarcaOk() {
        EjecutorBase ejecutor = new Interpretado(sandboxQueEscribe(0, false, "",
                List.of(new CasoSimulado("2", "0", "", "10"))));

        ResultadoEjecucion resultado = ejecutor.ejecutar(solicitud(new CasoPrueba("1", "2")));

        assertTrue(resultado.compilacionExitosa());
        ResultadoCaso caso = resultado.casos().get(0);
        assertEquals(EstadoCaso.OK, caso.estado());
        assertEquals("2", caso.salidaObtenida());
        assertEquals(10L, caso.milisegundos());
    }

    @Test
    void salidaDistintaSeReporta() {
        EjecutorBase ejecutor = new Interpretado(sandboxQueEscribe(0, false, "",
                List.of(new CasoSimulado("9", "0", "", "4"))));

        ResultadoCaso caso = ejecutor.ejecutar(solicitud(new CasoPrueba("1\n", "2"))).casos().get(0);

        assertEquals(EstadoCaso.SALIDA_DISTINTA, caso.estado());
        assertEquals("9", caso.salidaObtenida());
    }

    @Test
    void codigo124EsTiempoAgotado() {
        EjecutorBase ejecutor = new Interpretado(sandboxQueEscribe(0, false, "",
                List.of(new CasoSimulado("", "124", "", "5000"))));

        ResultadoCaso caso = ejecutor.ejecutar(solicitud(new CasoPrueba("1", "2"))).casos().get(0);

        assertEquals(EstadoCaso.TIEMPO_AGOTADO, caso.estado());
        assertTrue(caso.mensajeError().contains("excedio"));
    }

    @Test
    void codigo143TambienEsTiempoAgotado() {
        EjecutorBase ejecutor = new Interpretado(sandboxQueEscribe(0, false, "",
                List.of(new CasoSimulado("", "143", "", "1"))));

        assertEquals(EstadoCaso.TIEMPO_AGOTADO,
                ejecutor.ejecutar(solicitud(new CasoPrueba(null, "2"))).casos().get(0).estado());
    }

    @Test
    void codigo137EsErrorDeMemoria() {
        EjecutorBase ejecutor = new Interpretado(sandboxQueEscribe(0, false, "",
                List.of(new CasoSimulado("", "137", "", "1"))));

        ResultadoCaso caso = ejecutor.ejecutar(solicitud(new CasoPrueba("1", "2"))).casos().get(0);

        assertEquals(EstadoCaso.ERROR_EJECUCION, caso.estado());
        assertTrue(caso.mensajeError().contains("memoria"));
    }

    @Test
    void errorDeEjecucionUsaStderr() {
        EjecutorBase ejecutor = new Interpretado(sandboxQueEscribe(0, false, "",
                List.of(new CasoSimulado("", "1", "boom", "8"))));

        assertEquals("boom", ejecutor.ejecutar(solicitud(new CasoPrueba("1", "2"))).casos().get(0).mensajeError());
    }

    @Test
    void errorSinStderrUsaElCodigo() {
        EjecutorBase ejecutor = new Interpretado(sandboxQueEscribe(0, false, "",
                List.of(new CasoSimulado("", "7", "", "8"))));

        assertTrue(ejecutor.ejecutar(solicitud(new CasoPrueba("1", "2"))).casos().get(0)
                .mensajeError().contains("7"));
    }

    @Test
    void archivosRotosSeTratanComoErrorDeEjecucion() {
        EjecutorBase ejecutor = new Interpretado(spec -> new ResultadoComando(0, "", "", false, 3));

        ResultadoCaso caso = ejecutor.ejecutar(solicitud(new CasoPrueba("1", "2"))).casos().get(0);

        assertEquals(EstadoCaso.ERROR_EJECUCION, caso.estado());
        assertTrue(caso.mensajeError().contains("-1"));
    }

    @Test
    void fallaDeCompilacionLeeElLog() {
        EjecutorBase ejecutor = new Compilado(spec -> {
            escribir(spec.directorioTrabajo().resolve("compilacion.log"), "cannot find symbol");
            return new ResultadoComando(20, "", "", false, 12);
        });

        ResultadoEjecucion resultado = ejecutor.ejecutar(solicitud(new CasoPrueba("1", "2")));

        assertFalse(resultado.compilacionExitosa());
        assertEquals("cannot find symbol", resultado.mensajeCompilacion());
    }

    @Test
    void contenedorExpiradoSeReportaComoFalla() {
        EjecutorBase ejecutor = new Interpretado(spec -> new ResultadoComando(124, "", "", true, 30_000));

        ResultadoEjecucion resultado = ejecutor.ejecutar(solicitud(new CasoPrueba("1", "2")));

        assertFalse(resultado.compilacionExitosa());
        assertTrue(resultado.mensajeCompilacion().contains("tiempo maximo"));
    }

    @Test
    void contenedorConErrorUsaStderr() {
        EjecutorBase ejecutor = new Interpretado(spec -> new ResultadoComando(1, "", "docker: denied", false, 4));

        assertEquals("docker: denied", ejecutor.ejecutar(solicitud(new CasoPrueba("1", "2"))).mensajeCompilacion());
    }

    @Test
    void contenedorConErrorSinStderrUsaMensajeGenerico() {
        EjecutorBase ejecutor = new Interpretado(spec -> new ResultadoComando(1, "", "  ", false, 4));

        assertEquals("El contenedor termino con error",
                ejecutor.ejecutar(solicitud(new CasoPrueba("1", "2"))).mensajeCompilacion());
    }

    @Test
    void elGuionDeInterpretadoNoCompila() {
        EjecutorBase ejecutor = new Interpretado(spec -> {
            String guion = leer(spec.directorioTrabajo().resolve("ejecutar.sh"));
            assertFalse(guion.contains("javac"));
            assertTrue(guion.contains("python3 solucion.py"));
            return new ResultadoComando(1, "", "ok", false, 1);
        });

        ejecutor.ejecutar(solicitud(new CasoPrueba("1", "2")));
    }

    @Test
    void elGuionDeCompiladoInvocaAlCompilador() {
        EjecutorBase ejecutor = new Compilado(spec -> {
            String guion = leer(spec.directorioTrabajo().resolve("ejecutar.sh"));
            assertTrue(guion.contains("javac Solucion.java"));
            return new ResultadoComando(20, "", "", false, 1);
        });

        ejecutor.ejecutar(solicitud(new CasoPrueba("1", "2")));
    }

    private static SolicitudEjecucion solicitud(CasoPrueba caso) {
        return new SolicitudEjecucion("python", "print(1)", List.of(caso));
    }

    private static Sandbox sandboxQueEscribe(int codigo, boolean expiro, String stderr, List<CasoSimulado> casos) {
        return spec -> {
            for (int i = 0; i < casos.size(); i++) {
                CasoSimulado caso = casos.get(i);
                escribir(spec.directorioTrabajo().resolve("caso_" + i + ".out"), caso.salida());
                escribir(spec.directorioTrabajo().resolve("caso_" + i + ".code"), caso.codigo());
                escribir(spec.directorioTrabajo().resolve("caso_" + i + ".err"), caso.error());
                escribir(spec.directorioTrabajo().resolve("caso_" + i + ".ms"), caso.ms());
            }
            return new ResultadoComando(codigo, "", stderr, expiro, 9);
        };
    }

    private static void escribir(Path destino, String contenido) {
        try {
            Files.writeString(destino, contenido, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String leer(Path origen) {
        try {
            return Files.readString(origen, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private record CasoSimulado(String salida, String codigo, String error, String ms) {
    }

    private static final class Interpretado extends EjecutorBase {
        private Interpretado(Sandbox sandbox) {
            super(sandbox, limites());
        }

        @Override
        public String lenguaje() {
            return "python";
        }

        @Override
        protected String imagenDocker() {
            return "python:3.12-alpine";
        }

        @Override
        protected String nombreArchivoFuente() {
            return "solucion.py";
        }

        @Override
        protected String comandoCompilacion() {
            return null;
        }

        @Override
        protected String comandoEjecucion() {
            return "python3 solucion.py";
        }
    }

    private static final class Compilado extends EjecutorBase {
        private Compilado(Sandbox sandbox) {
            super(sandbox, limites());
        }

        @Override
        public String lenguaje() {
            return "java";
        }

        @Override
        protected String imagenDocker() {
            return "eclipse-temurin:21-jdk";
        }

        @Override
        protected String nombreArchivoFuente() {
            return "Solucion.java";
        }

        @Override
        protected String comandoCompilacion() {
            return "javac Solucion.java";
        }

        @Override
        protected String comandoEjecucion() {
            return "java Solucion";
        }
    }

    private static LimitesEjecucion limites() {
        return new LimitesEjecucion();
    }
}
