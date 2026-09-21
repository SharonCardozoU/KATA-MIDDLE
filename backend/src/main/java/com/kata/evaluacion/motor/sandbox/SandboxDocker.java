package com.kata.evaluacion.motor.sandbox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SandboxDocker implements Sandbox {

    private static final Logger log = LoggerFactory.getLogger(SandboxDocker.class);
    private static final String BINARIO_DOCKER = "/usr/bin/docker";
    private static final Pattern SEPARADOR = Pattern.compile("\\s+");

    /**
     * El contenedor debe correr con el mismo uid que creo el directorio temporal,
     * o el compilador no podra escribir en el volumen montado.
     */
    private final String usuario = resolverUsuario();
    private final LanzadorProceso lanzador;

    public SandboxDocker() {
        this(SandboxDocker::lanzar);
    }

    SandboxDocker(LanzadorProceso lanzador) {
        this.lanzador = lanzador;
    }

    private static Process lanzar(List<String> argumentos) throws IOException {
        return new ProcessBuilder(argumentos).start(); // NOSONAR imagen, ruta tmp y comando van allowlisteados
    }

    @Override
    public ResultadoComando ejecutar(EspecificacionSandbox especificacion) {
        long inicio = System.currentTimeMillis();
        String imagen = validarImagen(especificacion.imagen());
        if (imagen == null) {
            return new ResultadoComando(-1, "", "Imagen de sandbox no permitida", false,
                    System.currentTimeMillis() - inicio);
        }
        String trabajo = validarDirectorio(especificacion.directorioTrabajo());
        if (trabajo == null) {
            return new ResultadoComando(-1, "", "Directorio de trabajo fuera del area temporal", false,
                    System.currentTimeMillis() - inicio);
        }
        List<String> comando = validarComando(especificacion.comando());
        if (comando.isEmpty()) {
            return new ResultadoComando(-1, "", "Comando de sandbox no permitido", false,
                    System.currentTimeMillis() - inicio);
        }

        Duration limite = especificacion.tiempoMaximo() == null ? Duration.ofSeconds(30) : especificacion.tiempoMaximo();
        List<String> argumentos = construirArgumentos(especificacion, imagen, trabajo, comando);
        log.debug("Lanzando contenedor con imagen {}", imagen);

        try (ProcesoControlado controlado = new ProcesoControlado(lanzador.iniciar(argumentos))) {
            Process proceso = controlado.proceso();
            CompletableFuture<String> salida = leerAsincrono(proceso.getInputStream());
            CompletableFuture<String> error = leerAsincrono(proceso.getErrorStream());

            boolean termino = proceso.waitFor(limite.toMillis(), TimeUnit.MILLISECONDS);
            if (!termino) {
                proceso.destroyForcibly();
                boolean cerrado = proceso.waitFor(5, TimeUnit.SECONDS);
                if (!cerrado) {
                    log.warn("El contenedor no termino tras destroyForcibly");
                }
                return new ResultadoComando(124, salida.getNow(""), "El contenedor excedio el tiempo maximo total",
                        true, System.currentTimeMillis() - inicio);
            }

            return new ResultadoComando(proceso.exitValue(), salida.get(5, TimeUnit.SECONDS),
                    error.get(5, TimeUnit.SECONDS), false, System.currentTimeMillis() - inicio);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ResultadoComando(-1, "", "Ejecucion interrumpida", false, System.currentTimeMillis() - inicio);
        } catch (IOException | ExecutionException | TimeoutException e) {
            String detalle = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return new ResultadoComando(-1, "", "No se pudo ejecutar el contenedor: " + detalle,
                    false, System.currentTimeMillis() - inicio);
        }
    }

    private List<String> construirArgumentos(
            EspecificacionSandbox e, String imagen, String trabajo, List<String> comando) {
        List<String> argumentos = new ArrayList<>(List.of(
                BINARIO_DOCKER, "run", "--rm",
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
                "-v", trabajo + ":/work",
                "-w", "/work",
                imagen));
        argumentos.addAll(comando);
        return argumentos;
    }

    private static String validarImagen(String imagen) {
        if (imagen == null) {
            return null;
        }
        return switch (imagen) {
            case "python:3.12-alpine" -> "python:3.12-alpine";
            case "eclipse-temurin:21-jdk" -> "eclipse-temurin:21-jdk";
            case "node:20-alpine" -> "node:20-alpine";
            case "kata-typescript:20" -> "kata-typescript:20";
            case "kata-cobol:3" -> "kata-cobol:3";
            default -> null;
        };
    }

    private static String validarDirectorio(Path directorio) {
        if (directorio == null) {
            return null;
        }
        Path absoluto = directorio.toAbsolutePath().normalize();
        Path nombre = absoluto.getFileName();
        if (nombre == null) {
            return null;
        }
        String tmp = System.getProperty("java.io.tmpdir", "/tmp");
        Path temporal = Path.of(tmp).toAbsolutePath().normalize();
        Path privado = Path.of(System.getProperty("user.home"), ".kata-sandbox").toAbsolutePath().normalize();
        for (Path raiz : List.of(temporal, privado)) {
            Path reconstruido = raiz.resolve(nombre.toString()).normalize();
            if (absoluto.equals(reconstruido) && Files.isDirectory(reconstruido)) {
                return reconstruido.toString();
            }
        }
        return null;
    }

    private static List<String> validarComando(List<String> comando) {
        if (comando != null && comando.size() == 2 && "sh".equals(comando.get(0)) && "ejecutar.sh".equals(comando.get(1))) {
            return List.of("sh", "ejecutar.sh");
        }
        return List.of();
    }

    private String resolverUsuario() {
        try {
            String uid = campoProc("Uid:");
            String gid = campoProc("Gid:");
            if (uid != null && gid != null) {
                return uid + ":" + gid;
            }
        } catch (IOException e) {
            log.warn("No se pudo resolver el uid del proceso, se usara 1000:1000", e);
        }
        return "1000:1000";
    }

    private static String campoProc(String etiqueta) throws IOException {
        Path status = Path.of("/proc/self/status");
        if (!Files.isRegularFile(status)) {
            return null;
        }
        for (String linea : Files.readAllLines(status, StandardCharsets.UTF_8)) {
            if (linea.startsWith(etiqueta)) {
                String[] partes = SEPARADOR.split(linea);
                if (partes.length > 1 && esEntero(partes[1])) {
                    return partes[1];
                }
            }
        }
        return null;
    }

    private static boolean esEntero(String valor) {
        if (valor == null || valor.isEmpty()) {
            return false;
        }
        for (int i = 0; i < valor.length(); i++) {
            if (!Character.isDigit(valor.charAt(i))) {
                return false;
            }
        }
        return true;
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

    private record ProcesoControlado(Process proceso) implements AutoCloseable {
        @Override
        public void close() {
            if (proceso.isAlive()) {
                proceso.destroyForcibly();
            }
        }
    }
}
