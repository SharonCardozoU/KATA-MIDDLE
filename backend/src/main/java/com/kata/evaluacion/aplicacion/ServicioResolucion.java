package com.kata.evaluacion.aplicacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kata.evaluacion.dominio.Pregunta;
import com.kata.evaluacion.motor.modelo.CasoPrueba;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;

/**
 * El candidato no manda los casos ni el puntaje: salen de la pregunta persistida.
 * Asi no se puede inflar la nota enviando salidas esperadas inventadas.
 */
@Service
public class ServicioResolucion {

    private final ServicioPreguntas preguntas;
    private final OrquestadorEjecucion orquestador;

    public ServicioResolucion(ServicioPreguntas preguntas, OrquestadorEjecucion orquestador) {
        this.preguntas = preguntas;
        this.orquestador = orquestador;
    }

    @Transactional(readOnly = true)
    public EvaluacionEnvio resolver(Long evaluacionId, Long preguntaId, String lenguaje, String codigoFuente) {
        Pregunta pregunta = preguntas.obtener(evaluacionId, preguntaId);
        SolicitudEjecucion solicitud = new SolicitudEjecucion(
                lenguaje,
                codigoFuente,
                pregunta.getCasos().stream()
                        .map(caso -> new CasoPrueba(caso.getEntrada(), caso.getSalidaEsperada()))
                        .toList());
        return orquestador.evaluar(solicitud, pregunta.getPuntaje().doubleValue());
    }
}
