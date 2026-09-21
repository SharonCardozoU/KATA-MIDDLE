package com.kata.evaluacion.aplicacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kata.evaluacion.dominio.Evaluacion;
import com.kata.evaluacion.dominio.Intento;
import com.kata.evaluacion.dominio.IntentoRepositorio;
import com.kata.evaluacion.dominio.Pregunta;
import com.kata.evaluacion.dominio.RespuestaIntento;
import com.kata.evaluacion.dominio.RespuestaIntentoRepositorio;

@Service
public class ServicioIntentos {

    private final IntentoRepositorio intentos;
    private final RespuestaIntentoRepositorio respuestas;
    private final ServicioEvaluaciones evaluaciones;
    private final ServicioPreguntas preguntas;
    private final ServicioResolucion resolucion;

    public ServicioIntentos(
            IntentoRepositorio intentos,
            RespuestaIntentoRepositorio respuestas,
            ServicioEvaluaciones evaluaciones,
            ServicioPreguntas preguntas,
            ServicioResolucion resolucion) {
        this.intentos = intentos;
        this.respuestas = respuestas;
        this.evaluaciones = evaluaciones;
        this.preguntas = preguntas;
        this.resolucion = resolucion;
    }

    @Transactional
    public Intento crear(Long evaluacionId) {
        Evaluacion evaluacion = evaluaciones.obtener(evaluacionId);
        return intentos.save(new Intento(evaluacion));
    }

    @Transactional(readOnly = true)
    public ResumenIntento resumen(Long evaluacionId, Long intentoId) {
        Intento intento = obtenerDeEvaluacion(evaluacionId, intentoId);
        List<Pregunta> banco = preguntas.listar(evaluacionId);
        Map<Long, RespuestaIntento> porPregunta = intento.getRespuestas().stream()
                .collect(Collectors.toMap(r -> r.getPregunta().getId(), r -> r));

        List<ResumenPregunta> detalle = new ArrayList<>();
        BigDecimal puntajeObtenido = BigDecimal.ZERO;
        BigDecimal puntajeMaximo = BigDecimal.ZERO;
        int respondidas = 0;
        int correctas = 0;
        int incorrectas = 0;

        for (Pregunta pregunta : banco) {
            BigDecimal maximo = pregunta.getPuntaje();
            puntajeMaximo = puntajeMaximo.add(maximo);
            RespuestaIntento respuesta = porPregunta.get(pregunta.getId());
            if (respuesta == null) {
                detalle.add(ResumenPregunta.pendiente(pregunta));
            } else {
                respondidas++;
                puntajeObtenido = puntajeObtenido.add(respuesta.getPuntaje());
                ResumenPregunta fila = ResumenPregunta.desde(pregunta, respuesta);
                if ("CORRECTA".equals(fila.estado())) {
                    correctas++;
                } else {
                    incorrectas++;
                }
                detalle.add(fila);
            }
        }

        double porcentaje = puntajeMaximo.signum() == 0
                ? 0d
                : puntajeObtenido.multiply(BigDecimal.valueOf(100))
                        .divide(puntajeMaximo, 2, RoundingMode.HALF_UP)
                        .doubleValue();

        Evaluacion evaluacion = intento.getEvaluacion();
        long transcurrido = Math.max(0, Duration.between(intento.getIniciadoEn(), Instant.now()).getSeconds());
        long limite = evaluacion.getTiempoLimiteMinutos() * 60L;
        long consumido = Math.min(transcurrido, limite);
        long restante = Math.max(0, limite - transcurrido);

        return new ResumenIntento(
                intento.getId(),
                evaluacionId,
                evaluacion.getNombre(),
                intento.getIniciadoEn(),
                evaluacion.getTiempoLimiteMinutos(),
                consumido,
                restante,
                respondidas,
                banco.size(),
                correctas,
                incorrectas,
                puntajeObtenido.doubleValue(),
                puntajeMaximo.doubleValue(),
                porcentaje,
                detalle);
    }

    @Transactional
    public EvaluacionEnvio resolver(Long evaluacionId, Long intentoId, Long preguntaId, String lenguaje, String codigo) {
        Intento intento = obtenerDeEvaluacion(evaluacionId, intentoId);
        Pregunta pregunta = preguntas.obtener(evaluacionId, preguntaId);
        EvaluacionEnvio envio = resolucion.resolver(evaluacionId, preguntaId, lenguaje, codigo);

        RespuestaIntento respuesta = respuestas
                .findByIntentoIdAndPreguntaId(intento.getId(), pregunta.getId())
                .orElseGet(() -> new RespuestaIntento(intento, pregunta));
        respuesta.actualizar(
                lenguaje,
                envio.calificacion().casosExitosos(),
                envio.calificacion().totalCasos(),
                BigDecimal.valueOf(envio.calificacion().porcentaje()),
                BigDecimal.valueOf(envio.calificacion().puntaje()),
                pregunta.getPuntaje(),
                envio.ejecucion().compilacionExitosa());
        respuestas.save(respuesta);
        return envio;
    }

    private Intento obtenerDeEvaluacion(Long evaluacionId, Long intentoId) {
        evaluaciones.obtener(evaluacionId);
        Intento intento = intentos.findById(intentoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el intento " + intentoId));
        if (!evaluacionId.equals(intento.getEvaluacion().getId())) {
            throw new RecursoNoEncontradoException("El intento no pertenece a esta evaluacion");
        }
        return intento;
    }

    public record ResumenIntento(
            Long id,
            Long evaluacionId,
            String nombreEvaluacion,
            Instant iniciadoEn,
            int tiempoLimiteMinutos,
            long tiempoConsumidoSegundos,
            long tiempoRestanteSegundos,
            int preguntasRespondidas,
            int totalPreguntas,
            int preguntasCorrectas,
            int preguntasIncorrectas,
            double puntajeObtenido,
            double puntajeMaximo,
            double porcentaje,
            List<ResumenPregunta> preguntas) {
    }

    public record ResumenPregunta(
            Long preguntaId,
            String nombre,
            double puntajeMaximo,
            String estado,
            boolean respondida,
            boolean compilacionExitosa,
            int casosExitosos,
            int totalCasos,
            double porcentaje,
            double puntaje) {

        static ResumenPregunta pendiente(Pregunta pregunta) {
            return new ResumenPregunta(
                    pregunta.getId(), pregunta.getNombre(), pregunta.getPuntaje().doubleValue(),
                    "PENDIENTE", false, false, 0, 0, 0d, 0d);
        }

        static ResumenPregunta desde(Pregunta pregunta, RespuestaIntento respuesta) {
            boolean correcta = respuesta.isCompilacionExitosa()
                    && respuesta.getTotalCasos() > 0
                    && respuesta.getCasosExitosos() == respuesta.getTotalCasos();
            return new ResumenPregunta(
                    pregunta.getId(),
                    pregunta.getNombre(),
                    pregunta.getPuntaje().doubleValue(),
                    correcta ? "CORRECTA" : "INCORRECTA",
                    true,
                    respuesta.isCompilacionExitosa(),
                    respuesta.getCasosExitosos(),
                    respuesta.getTotalCasos(),
                    respuesta.getPorcentaje().doubleValue(),
                    respuesta.getPuntaje().doubleValue());
        }
    }
}
