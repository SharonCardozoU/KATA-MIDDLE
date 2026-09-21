package com.kata.evaluacion.dominio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreguntaRepositorio extends JpaRepository<Pregunta, Long> {

    @EntityGraph(attributePaths = "casos")
    List<Pregunta> findByEvaluacionIdOrderByCreadoEnAsc(Long evaluacionId);

    long countByEvaluacionId(Long evaluacionId);

    @EntityGraph(attributePaths = "casos")
    Optional<Pregunta> findByIdAndEvaluacionId(Long id, Long evaluacionId);
}
