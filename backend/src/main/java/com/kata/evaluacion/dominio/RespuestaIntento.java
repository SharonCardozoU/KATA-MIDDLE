package com.kata.evaluacion.dominio;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "respuesta_intento")
public class RespuestaIntento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intento_id", nullable = false)
    private Intento intento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    @Column(nullable = false, length = 40)
    private String lenguaje;

    @Column(name = "casos_exitosos", nullable = false)
    private int casosExitosos;

    @Column(name = "total_casos", nullable = false)
    private int totalCasos;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal porcentaje;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal puntaje;

    @Column(name = "puntaje_maximo", nullable = false, precision = 8, scale = 2)
    private BigDecimal puntajeMaximo;

    @Column(name = "compilacion_exitosa", nullable = false)
    private boolean compilacionExitosa;

    @Column(name = "respondido_en", nullable = false)
    private Instant respondidoEn;

    protected RespuestaIntento() {
    }

    public RespuestaIntento(Intento intento, Pregunta pregunta) {
        this.intento = intento;
        this.pregunta = pregunta;
        this.respondidoEn = Instant.now();
    }

    public void actualizar(
            String lenguaje,
            int casosExitosos,
            int totalCasos,
            BigDecimal porcentaje,
            BigDecimal puntaje,
            BigDecimal puntajeMaximo,
            boolean compilacionExitosa) {
        this.lenguaje = lenguaje;
        this.casosExitosos = casosExitosos;
        this.totalCasos = totalCasos;
        this.porcentaje = porcentaje;
        this.puntaje = puntaje;
        this.puntajeMaximo = puntajeMaximo;
        this.compilacionExitosa = compilacionExitosa;
        this.respondidoEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public String getLenguaje() {
        return lenguaje;
    }

    public int getCasosExitosos() {
        return casosExitosos;
    }

    public int getTotalCasos() {
        return totalCasos;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public BigDecimal getPuntaje() {
        return puntaje;
    }

    public BigDecimal getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public boolean isCompilacionExitosa() {
        return compilacionExitosa;
    }
}
