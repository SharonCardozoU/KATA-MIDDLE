package com.kata.evaluacion.aplicacion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kata.evaluacion.dominio.Evaluacion;
import com.kata.evaluacion.dominio.Pregunta;
import com.kata.evaluacion.dominio.PreguntaRepositorio;
import com.kata.evaluacion.motor.LenguajeNoSoportadoException;
import com.kata.evaluacion.motor.RegistroEjecutores;
import com.kata.evaluacion.motor.SolicitudInvalidaException;

@Service
public class ServicioPreguntas {

    private final PreguntaRepositorio repositorio;
    private final ServicioEvaluaciones evaluaciones;
    private final RegistroEjecutores ejecutores;

    public ServicioPreguntas(
            PreguntaRepositorio repositorio,
            ServicioEvaluaciones evaluaciones,
            RegistroEjecutores ejecutores) {
        this.repositorio = repositorio;
        this.evaluaciones = evaluaciones;
        this.ejecutores = ejecutores;
    }

    @Transactional(readOnly = true)
    public List<Pregunta> listar(Long evaluacionId) {
        evaluaciones.obtener(evaluacionId);
        return repositorio.findByEvaluacionIdOrderByCreadoEnAsc(evaluacionId);
    }

    @Transactional(readOnly = true)
    public long contar(Long evaluacionId) {
        return repositorio.countByEvaluacionId(evaluacionId);
    }

    @Transactional(readOnly = true)
    public Pregunta obtener(Long evaluacionId, Long preguntaId) {
        evaluaciones.obtener(evaluacionId);
        return repositorio.findByIdAndEvaluacionId(preguntaId, evaluacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la pregunta " + preguntaId + " en la evaluacion " + evaluacionId));
    }

    @Transactional
    public Pregunta crear(Long evaluacionId, String nombre, String descripcion, String lenguaje,
            double puntaje, List<CasoEntrada> casos) {
        Evaluacion evaluacion = evaluaciones.obtener(evaluacionId);
        long registradas = repositorio.countByEvaluacionId(evaluacionId);
        if (registradas >= evaluacion.getCantidadPreguntas()) {
            throw new SolicitudInvalidaException(
                    "Esta evaluacion ya tiene las " + evaluacion.getCantidadPreguntas() + " preguntas planeadas");
        }
        if (casos == null || casos.isEmpty()) {
            throw new SolicitudInvalidaException("Se requiere al menos un caso de prueba");
        }

        String clave = lenguaje == null ? "" : lenguaje.toLowerCase(Locale.ROOT).strip();
        if (!ejecutores.lenguajesSoportados().contains(clave)) {
            throw new LenguajeNoSoportadoException(lenguaje, ejecutores.lenguajesSoportados());
        }

        if (puntaje <= 0) {
            throw new SolicitudInvalidaException("El puntaje debe ser mayor que cero");
        }

        Pregunta pregunta = new Pregunta(
                evaluacion, nombre.strip(), descripcion.strip(), clave, BigDecimal.valueOf(puntaje));
        for (CasoEntrada caso : casos) {
            pregunta.agregarCaso(caso.entrada(), caso.salidaEsperada());
        }
        return repositorio.save(pregunta);
    }

    @Transactional
    public Pregunta actualizar(Long evaluacionId, Long preguntaId, String nombre, String descripcion,
            String lenguaje, double puntaje, List<CasoEntrada> casos) {
        Pregunta pregunta = obtener(evaluacionId, preguntaId);
        if (casos == null || casos.isEmpty()) {
            throw new SolicitudInvalidaException("Se requiere al menos un caso de prueba");
        }
        String clave = lenguaje == null ? "" : lenguaje.toLowerCase(Locale.ROOT).strip();
        if (!ejecutores.lenguajesSoportados().contains(clave)) {
            throw new LenguajeNoSoportadoException(lenguaje, ejecutores.lenguajesSoportados());
        }
        if (puntaje <= 0) {
            throw new SolicitudInvalidaException("El puntaje debe ser mayor que cero");
        }
        pregunta.actualizar(nombre.strip(), descripcion.strip(), clave, BigDecimal.valueOf(puntaje));
        pregunta.getCasos().clear();
        for (CasoEntrada caso : casos) {
            pregunta.agregarCaso(caso.entrada(), caso.salidaEsperada());
        }
        return repositorio.save(pregunta);
    }

    public record CasoEntrada(String entrada, String salidaEsperada) {
    }
}
