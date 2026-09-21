package com.kata.evaluacion.motor.sandbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

class SandboxDockerTest {

    @Test
    void rechazaImagenFueraDeLaLista() throws Exception {
        Path dir = Files.createTempDirectory("kata-");
        try {
            ResultadoComando resultado = new SandboxDocker().ejecutar(spec(dir, "maliciosa:latest", List.of("sh", "ejecutar.sh")));

            assertEquals(-1, resultado.codigoSalida());
            assertTrue(resultado.salidaError().contains("Imagen"));
        } finally {
            Files.deleteIfExists(dir);
        }
    }

    @Test
    void rechazaImagenNula() throws Exception {
        Path dir = Files.createTempDirectory("kata-");
        try {
            ResultadoComando resultado = new SandboxDocker().ejecutar(spec(dir, null, List.of("sh", "ejecutar.sh")));
            assertTrue(resultado.salidaError().contains("Imagen"));
        } finally {
            Files.deleteIfExists(dir);
        }
    }

    @Test
    void rechazaDirectorioNuloOFueraDelTmp() {
        SandboxDocker sandbox = new SandboxDocker();
        assertTrue(sandbox.ejecutar(spec(null, "python:3.12-alpine", List.of("sh", "ejecutar.sh")))
                .salidaError().contains("Directorio"));
        assertTrue(sandbox.ejecutar(spec(Path.of("/tmp/../etc"), "python:3.12-alpine", List.of("sh", "ejecutar.sh")))
                .codigoSalida() == -1);
    }

    @Test
    void rechazaComandoDistintoAlGuion() throws Exception {
        Path dir = Files.createTempDirectory("kata-");
        try {
            SandboxDocker sandbox = new SandboxDocker();
            assertTrue(sandbox.ejecutar(spec(dir, "python:3.12-alpine", List.of("rm", "-rf", "/")))
                    .salidaError().contains("Comando"));
            assertTrue(sandbox.ejecutar(spec(dir, "python:3.12-alpine", List.of("bash", "ejecutar.sh")))
                    .salidaError().contains("Comando"));
            assertTrue(sandbox.ejecutar(spec(dir, "python:3.12-alpine", List.of("sh", "otro.sh")))
                    .salidaError().contains("Comando"));
            assertTrue(sandbox.ejecutar(spec(dir, "python:3.12-alpine", null))
                    .salidaError().contains("Comando"));
        } finally {
            Files.deleteIfExists(dir);
        }
    }

    @Test
    void lanzaElProcesoCuandoLaEspecificacionEsValida() throws Exception {
        Path dir = Files.createTempDirectory("kata-");
        AtomicReference<List<String>> capturado = new AtomicReference<>();
        SandboxDocker sandbox = new SandboxDocker(argumentos -> {
            capturado.set(List.copyOf(argumentos));
            return new ProcesoSimulado(0, "ok", "", false);
        });
        try {
            ResultadoComando resultado = sandbox.ejecutar(spec(dir, "python:3.12-alpine", List.of("sh", "ejecutar.sh")));

            assertEquals(0, resultado.codigoSalida());
            assertEquals("ok", resultado.salidaEstandar());
            assertFalse(resultado.expiro());
            assertEquals("/usr/bin/docker", capturado.get().get(0));
            assertTrue(capturado.get().contains("python:3.12-alpine"));
            assertTrue(capturado.get().containsAll(List.of("sh", "ejecutar.sh")));
        } finally {
            Files.deleteIfExists(dir);
        }
    }

    @Test
    void marcaExpiradoSiElProcesoNoTermina() throws Exception {
        Path dir = Files.createTempDirectory("kata-");
        SandboxDocker sandbox = new SandboxDocker(argumentos -> new ProcesoSimulado(0, "", "", true));
        try {
            ResultadoComando resultado = sandbox.ejecutar(new EspecificacionSandbox(
                    dir, "node:20-alpine", List.of("sh", "ejecutar.sh"), Duration.ofMillis(20), 64, 1, 16));

            assertTrue(resultado.expiro());
            assertEquals(124, resultado.codigoSalida());
        } finally {
            Files.deleteIfExists(dir);
        }
    }

    @Test
    void traduceFalloAlArrancar() throws Exception {
        Path dir = Files.createTempDirectory("kata-");
        SandboxDocker sandbox = new SandboxDocker(argumentos -> {
            throw new IOException();
        });
        try {
            ResultadoComando resultado = sandbox.ejecutar(spec(dir, "kata-cobol:3", List.of("sh", "ejecutar.sh")));

            assertEquals(-1, resultado.codigoSalida());
            assertTrue(resultado.salidaError().contains("No se pudo ejecutar"));
        } finally {
            Files.deleteIfExists(dir);
        }
    }

    @Test
    void traduceInterrupcion() throws Exception {
        Path dir = Files.createTempDirectory("kata-");
        SandboxDocker sandbox = new SandboxDocker(argumentos -> new ProcesoSimulado(0, "", "", false) {
            @Override
            public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
                throw new InterruptedException();
            }
        });
        try {
            ResultadoComando resultado = sandbox.ejecutar(spec(dir, "eclipse-temurin:21-jdk", List.of("sh", "ejecutar.sh")));

            assertEquals(-1, resultado.codigoSalida());
            assertTrue(resultado.salidaError().contains("interrumpida"));
        } finally {
            Files.deleteIfExists(dir);
            Thread.interrupted();
        }
    }

    @Test
    void usaTreintaSegundosSiNoHayLimite() throws Exception {
        Path dir = Files.createTempDirectory("kata-");
        SandboxDocker sandbox = new SandboxDocker(argumentos -> new ProcesoSimulado(0, "ok", "", false));
        try {
            ResultadoComando resultado = sandbox.ejecutar(new EspecificacionSandbox(
                    dir, "python:3.12-alpine", List.of("sh", "ejecutar.sh"), null, 64, 1, 16));
            assertEquals(0, resultado.codigoSalida());
        } finally {
            Files.deleteIfExists(dir);
        }
    }

    @Test
    void aceptaLasCincoImagenesPermitidas() throws Exception {
        Path dir = Files.createTempDirectory("kata-");
        SandboxDocker sandbox = new SandboxDocker(argumentos -> new ProcesoSimulado(0, "", "", false));
        try {
            for (String imagen : List.of(
                    "python:3.12-alpine",
                    "eclipse-temurin:21-jdk",
                    "node:20-alpine",
                    "kata-typescript:20",
                    "kata-cobol:3")) {
                assertEquals(0, sandbox.ejecutar(spec(dir, imagen, List.of("sh", "ejecutar.sh"))).codigoSalida());
            }
        } finally {
            Files.deleteIfExists(dir);
        }
    }

    private static EspecificacionSandbox spec(Path dir, String imagen, List<String> comando) {
        return new EspecificacionSandbox(dir, imagen, comando, Duration.ofSeconds(1), 64, 1, 16);
    }

    private static class ProcesoSimulado extends Process {
        private final int codigo;
        private final ByteArrayInputStream salida;
        private final ByteArrayInputStream error;
        private final boolean colgado;
        private final CountDownLatch fin = new CountDownLatch(1);
        private volatile boolean vivo = true;

        private ProcesoSimulado(int codigo, String out, String err, boolean colgado) {
            this.codigo = codigo;
            this.salida = new ByteArrayInputStream(out.getBytes(StandardCharsets.UTF_8));
            this.error = new ByteArrayInputStream(err.getBytes(StandardCharsets.UTF_8));
            this.colgado = colgado;
        }

        @Override
        public OutputStream getOutputStream() {
            return OutputStream.nullOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return salida;
        }

        @Override
        public InputStream getErrorStream() {
            return error;
        }

        @Override
        public int waitFor() throws InterruptedException {
            if (colgado && !fin.await(5, TimeUnit.SECONDS)) {
                return codigo;
            }
            vivo = false;
            return codigo;
        }

        @Override
        public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
            if (colgado) {
                return fin.await(timeout, unit);
            }
            vivo = false;
            return true;
        }

        @Override
        public int exitValue() {
            if (vivo) {
                throw new IllegalThreadStateException();
            }
            return codigo;
        }

        @Override
        public void destroy() {
            destroyForcibly();
        }

        @Override
        public Process destroyForcibly() {
            vivo = false;
            fin.countDown();
            return this;
        }

        @Override
        public boolean isAlive() {
            return vivo;
        }
    }
}
