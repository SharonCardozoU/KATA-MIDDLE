package com.kata.evaluacion.motor.sandbox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SandboxDocker implements Sandbox {

    private static final Logger log = LoggerFactory.getLogger(SandboxDocker.class);

    /**
     * El contenedor debe correr con el mismo uid que creo el directorio temporal,
     * o el compilador no podra escribir en el volumen montado.
     */
    private final String usuario = resolverUsuario();

    @Override
    public ResultadoComando ejecutar(EspecificacionSandbox especificacion) {
        List<String> argumentos = construirArgumentos(especificacion);
        log.debug("Lanzando contenedor: {}", String.join(" ", argumentos));

        long inicio = System.currentTimeMillis();
        Process proceso = null;
        try {
            proceso = new ProcessBuilder(argumentos).start();

            CompletableFuture<String> salida = leerAsincrono(proceso.getInputStream());
            CompletableFuture<String> error = leerAsincrono(proceso.getErrorStream());

            boolean termino = proceso.waitFor(especificacion.tiempoMaximo().toMillis(), TimeUnit.MILLISECONDS);
            if (!termino) {
                proceso.destroyForcibly();
                proceso.waitFor(5, TimeUnit.SECONDS);
                return new ResultadoComando(124, salida.getNow(""), "El contenedor excedio el tiempo maximo total",
                        true, System.currentTimeMillis() - inicio);
            }

            return new ResultadoComando(proceso.exitValue(), salida.get(5, TimeUnit.SECONDS),
                    error.get(5, TimeUnit.SECONDS), false, System.currentTimeMillis() - inicio);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (proceso != null) {
                proceso.destroyForcibly();
            }
            return new ResultadoComando(-1, "", "Ejecucion interrumpida", false, System.currentTimeMillis() - inicio);
        } catch (Exception e) {
            if (proceso != null) {
                proceso.destroyForcibly();
            }
            return new ResultadoComando(-1, "", "No se pudo ejecutar el contenedor: " + e.getMessage(),
                    false, System.currentTimeMillis() - inicio);
        }
    }

    private List<String> construirArgumentos(EspecificacionSandbox e) {
        List<String> argumentos = new ArrayList<>(List.of(
                "docker", "run", "--rm",
                "--network", "none",
                "--read-only",
                "--user", usuario,
                "--cap-drop", "ALL",
                "--security-opt", "no-new-privileges",
                "--tmpfs", "/tmp",
                "--memory", e.memoriaMb() + "m",
                "--memory-swap", e.memoriaMb() + "m",
                "--cpus", String.valueOf(e.cpus()),
                "--pids-limit", String.valueOf(e.limitePids()),
                "-v", e.directorioTrabajo().toAbsolutePath() + ":/work",
                "-w", "/work",
                e.imagen()));
        argumentos.addAll(e.comando());
        return argumentos;
    }

    private String resolverUsuario() {
        try {
            Process proceso = new ProcessBuilder("sh", "-c", "echo $(id -u):$(id -g)").start();
            String salida = new String(proceso.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            proceso.waitFor(5, TimeUnit.SECONDS);
            if (salida.matches("\\d+:\\d+")) {
                return salida;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.warn("No se pudo resolver el uid del proceso, se usara 1000:1000", e);
        }
        return "1000:1000";
    }

    private CompletableFuture<String> leerAsincrono(InputStream flujo) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream entrada = flujo) {
                return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                return "";
            }
        });
    }
}
