package com.kata.evaluacion.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "intento")
public class Intento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluacion_id", nullable = false)
    private Evaluacion evaluacion;

    @Column(name = "iniciado_en", nullable = false)
    private Instant iniciadoEn;

    @OneToMany(mappedBy = "intento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespuestaIntento> respuestas = new ArrayList<>();

    protected Intento() {
    }

    public Intento(Evaluacion evaluacion) {
        this.evaluacion = evaluacion;
        this.iniciadoEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Evaluacion getEvaluacion() {
        return evaluacion;
    }

    public Instant getIniciadoEn() {
        return iniciadoEn;
    }

    public List<RespuestaIntento> getRespuestas() {
        return respuestas;
    }
}
