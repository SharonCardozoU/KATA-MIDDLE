package com.kata.evaluacion.dominio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluacionRepositorio extends JpaRepository<Evaluacion, Long> {

    List<Evaluacion> findAllByOrderByCreadoEnDesc();
}
