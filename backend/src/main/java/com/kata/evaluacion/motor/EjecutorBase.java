package com.kata.evaluacion.motor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kata.evaluacion.motor.modelo.CasoPrueba;
import com.kata.evaluacion.motor.modelo.EstadoCaso;
import com.kata.evaluacion.motor.modelo.ResultadoCaso;
import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;
import com.kata.evaluacion.motor.sandbox.EspecificacionSandbox;
import com.kata.evaluacion.motor.sandbox.ResultadoComando;
import com.kata.evaluacion.motor.sandbox.Sandbox;

/**
 * Template Method. Fija el esqueleto identico para todos los lenguajes
 * (preparar, compilar, ejecutar los N casos, recoger, limpiar) y deja como
 * puntos de extension unicamente la imagen y los comandos.
 *
 * Decision de desempeno de EC03: se lanza un solo contenedor por envio. Adentro
 * se compila una vez y se recorren los casos, en lugar de un contenedor por caso.
 */
public abstract class EjecutorBase implements Ejecutor {

    private static final Logger log = LoggerFactory.getLogger(EjecutorBase.class);
    private static final int CODIGO_FALLA_COMPILACION = 20;

    /**
     * GNU coreutils devuelve 124 al agotar el tiempo; el timeout de BusyBox que
     * traen las imagenes alpine devuelve 143 (128 + SIGTERM). Hay que aceptar
     * ambos o los casos que se cuelgan en Python y Node se reportan como error
     * de ejecucion en vez de tiempo agotado.
     */
    private static final Set<Integer> CODIGOS_TIEMPO_AGOTADO = Set.of(124, 143);

    /** 128 + SIGKILL: lo produce el limite de memoria del contenedor. */
    private static final int CODIGO_TERMINADO_POR_SISTEMA = 137;
    private static final String PREFIJO_CASO = "caso_";

    protected final Sandbox sandbox;
    protected final LimitesEjecucion limites;

    protected EjecutorBase(Sandbox sandbox, LimitesEjecucion limites) {
        this.sandbox = sandbox;
        this.limites = limites;
    }

    @Override
    public final ResultadoEjecucion ejecutar(SolicitudEjecucion solicitud) {
        Path espacio = null;
        try {
            espacio = crearEspacioPrivado();
            prepararEspacio(espacio, solicitud);

            ResultadoComando comando = sandbox.ejecutar(new EspecificacionSandbox(
                    espacio,
                    imagenDocker(),
                    List.of("sh", "ejecutar.sh"),
                    Duration.ofSeconds(limites.getTiempoMaximoTotalSegundos()),
                    limites.getMemoriaMb(),
                    limites.getCpus(),
                    limites.getLimitePids()));

            return recogerResultados(espacio, solicitud, comando);

        } catch (IOException e) {
            log.error("Error preparando el espacio de ejecucion", e);
            return ResultadoEjecucion.fallaCompilacion("Error interno preparando la ejecucion");
        } finally {
            borrarRecursivo(espacio);
        }
    }

    protected abstract String imagenDocker();

    protected abstract String nombreArchivoFuente();

    /** Devuelve null cuando el lenguaje es interpretado y no requiere compilacion. */
    protected abstract String comandoCompilacion();

    protected abstract String comandoEjecucion();

    private static Path crearEspacioPrivado() throws IOException {
        Path raiz = Path.of(System.getProperty("user.home"), ".kata-sandbox");
        Files.createDirectories(raiz);
        restringirADuenio(raiz);
        Path espacio = raiz.resolve("kata-" + UUID.randomUUID());
        Files.createDirectories(espacio);
        restringirADuenio(espacio);
        return espacio;
    }

    private static void restringirADuenio(Path ruta) throws IOException {
        Files.setPosixFilePermissions(ruta, PosixFilePermissions.fromString("rwx------"));
    }

    private void prepararEspacio(Path espacio, SolicitudEjecucion solicitud) throws IOException {
        escribir(espacio.resolve(nombreArchivoFuente()), solicitud.codigoFuente());

        List<CasoPrueba> casos = solicitud.casos();
        for (int i = 0; i < casos.size(); i++) {
            String entrada = casos.get(i).entrada() == null ? "" : casos.get(i).entrada();
            escribir(archivoCaso(espacio, i, ".in"), entrada.endsWith("\n") ? entrada : entrada + "\n");
        }

        escribir(espacio.resolve("ejecutar.sh"), guion(casos.size()));
    }

    private String guion(int numeroCasos) {
        StringBuilder guion = new StringBuilder("#!/bin/sh\ncd /work\n");

        // El 'date' de BusyBox que traen las imagenes alpine ignora %N, asi que no
        // sirve para medir milisegundos. /proc/uptime existe en cualquier contenedor
        // Linux y trae centisegundos, que dan resolucion de 10 ms.
        guion.append("reloj() { read u _ < /proc/uptime; echo \"${u%.*}${u#*.}\"; }\n");

        String compilacion = comandoCompilacion();
        if (compilacion != null) {
            guion.append(compilacion).append(" > compilacion.log 2>&1\n")
                 .append("if [ $? -ne 0 ]; then exit ").append(CODIGO_FALLA_COMPILACION).append("; fi\n");
        }

        guion.append("i=0\n")
             .append("while [ $i -lt ").append(numeroCasos).append(" ]; do\n")
             .append("  t0=$(reloj)\n")
             .append("  timeout ").append(limites.getSegundosPorCaso()).append(' ').append(comandoEjecucion())
             .append(" < caso_$i.in > caso_$i.out 2> caso_$i.err\n")
             .append("  echo $? > caso_$i.code\n")
             .append("  t1=$(reloj)\n")
             .append("  echo $(( (t1 - t0) * 10 )) > caso_$i.ms\n")
             .append("  i=$((i+1))\n")
             .append("done\n")
             .append("exit 0\n");

        return guion.toString();
    }

    private ResultadoEjecucion recogerResultados(Path espacio, SolicitudEjecucion solicitud, ResultadoComando comando) {
        if (comando.codigoSalida() == CODIGO_FALLA_COMPILACION) {
            return ResultadoEjecucion.fallaCompilacion(leer(espacio.resolve("compilacion.log")));
        }
        if (comando.expiro()) {
            return ResultadoEjecucion.fallaCompilacion(
                    "La ejecucion completa excedio el tiempo maximo de " + limites.getTiempoMaximoTotalSegundos() + " s");
        }
        if (comando.codigoSalida() != 0) {
            return ResultadoEjecucion.fallaCompilacion(
                    comando.salidaError().isBlank() ? "El contenedor termino con error" : comando.salidaError());
        }

        List<CasoPrueba> casos = solicitud.casos();
        List<ResultadoCaso> resultados = new ArrayList<>(casos.size());
        for (int i = 0; i < casos.size(); i++) {
            resultados.add(evaluarCaso(espacio, i, casos.get(i)));
        }
        return ResultadoEjecucion.compilado(resultados);
    }

    private ResultadoCaso evaluarCaso(Path espacio, int indice, CasoPrueba caso) {
        long milisegundos = numeroDeArchivo(archivoCaso(espacio, indice, ".ms"));
        int codigo = codigoDeSalida(archivoCaso(espacio, indice, ".code"));
        String obtenida = normalizar(leer(archivoCaso(espacio, indice, ".out")));
        String error = leer(archivoCaso(espacio, indice, ".err"));
        String esperada = normalizar(caso.salidaEsperada());

        if (CODIGOS_TIEMPO_AGOTADO.contains(codigo)) {
            return new ResultadoCaso(indice, caso.entrada(), esperada, obtenida, EstadoCaso.TIEMPO_AGOTADO,
                    "El caso excedio " + limites.getSegundosPorCaso() + " s y fue terminado", milisegundos);
        }
        if (codigo == CODIGO_TERMINADO_POR_SISTEMA) {
            return new ResultadoCaso(indice, caso.entrada(), esperada, obtenida, EstadoCaso.ERROR_EJECUCION,
                    "El proceso fue terminado por el sistema, probablemente al superar "
                            + limites.getMemoriaMb() + " MB de memoria", milisegundos);
        }
        if (codigo != 0) {
            return new ResultadoCaso(indice, caso.entrada(), esperada, obtenida, EstadoCaso.ERROR_EJECUCION,
                    error.isBlank() ? "Termino con codigo " + codigo : error, milisegundos);
        }
        if (!obtenida.equals(esperada)) {
            return new ResultadoCaso(indice, caso.entrada(), esperada, obtenida, EstadoCaso.SALIDA_DISTINTA,
                    null, milisegundos);
        }
        return new ResultadoCaso(indice, caso.entrada(), esperada, obtenida, EstadoCaso.OK, null, milisegundos);
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.replace("\r\n", "\n").strip();
    }

    private void escribir(Path destino, String contenido) throws IOException {
        Files.writeString(destino, contenido, StandardCharsets.UTF_8);
    }

    private static Path archivoCaso(Path espacio, int indice, String extension) {
        return espacio.resolve(PREFIJO_CASO + indice + extension);
    }

    private String leer(Path origen) {
        try {
            return Files.exists(origen) ? Files.readString(origen, StandardCharsets.UTF_8) : "";
        } catch (IOException e) {
            return "";
        }
    }

    private int codigoDeSalida(Path origen) {
        try {
            return Integer.parseInt(leer(origen).strip());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private long numeroDeArchivo(Path origen) {
        try {
            return Long.parseLong(leer(origen).strip());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private void borrarRecursivo(Path raiz) {
        if (raiz == null || !Files.exists(raiz)) {
            return;
        }
        try (Stream<Path> rutas = Files.walk(raiz)) {
            rutas.sorted(Comparator.reverseOrder()).forEach(ruta -> {
                try {
                    Files.deleteIfExists(ruta);
                } catch (IOException e) {
                    log.warn("No se pudo borrar {}", ruta);
                }
            });
        } catch (IOException e) {
            log.warn("No se pudo limpiar el espacio temporal {}", raiz, e);
        }
    }
}
