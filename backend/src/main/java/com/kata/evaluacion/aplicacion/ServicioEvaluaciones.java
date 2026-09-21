package com.kata.evaluacion.aplicacion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kata.evaluacion.dominio.Evaluacion;
import com.kata.evaluacion.dominio.EvaluacionRepositorio;

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
}
