package com.kata.evaluacion.dominio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RespuestaIntentoRepositorio extends JpaRepository<RespuestaIntento, Long> {

    Optional<RespuestaIntento> findByIntentoIdAndPreguntaId(Long intentoId, Long preguntaId);
}
