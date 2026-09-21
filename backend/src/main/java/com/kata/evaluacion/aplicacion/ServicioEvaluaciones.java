package com.kata.evaluacion.aplicacion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kata.evaluacion.dominio.Evaluacion;
import com.kata.evaluacion.dominio.EvaluacionRepositorio;
import com.kata.evaluacion.motor.SolicitudInvalidaException;

@Service
public class ServicioEvaluaciones {

    private final EvaluacionRepositorio repositorio;

    public ServicioEvaluaciones(EvaluacionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Evaluacion> listar() {
        return repositorio.findAllByOrderByCreadoEnDesc();
    }

    @Transactional(readOnly = true)
    public Evaluacion obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la evaluacion " + id));
    }

    @Transactional
    public Evaluacion crear(String nombre, String descripcion, int tiempoLimiteMinutos, int cantidadPreguntas) {
        return repositorio.save(new Evaluacion(nombre.strip(), descripcion, tiempoLimiteMinutos, cantidadPreguntas));
    }

    @Transactional
    public Evaluacion actualizar(
            Long id, String nombre, String descripcion, int tiempoLimiteMinutos, int cantidadPreguntas,
            long preguntasRegistradas) {
        if (cantidadPreguntas < preguntasRegistradas) {
            throw new SolicitudInvalidaException(
                    "Ya hay " + preguntasRegistradas
                            + " preguntas registradas. El cupo no puede ser menor que esa cantidad.");
        }
        Evaluacion evaluacion = obtener(id);
        evaluacion.actualizar(
                nombre.strip(), descripcion == null ? "" : descripcion, tiempoLimiteMinutos, cantidadPreguntas);
        return repositorio.save(evaluacion);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe la evaluacion " + id);
        }
        repositorio.deleteById(id);
    }
}
