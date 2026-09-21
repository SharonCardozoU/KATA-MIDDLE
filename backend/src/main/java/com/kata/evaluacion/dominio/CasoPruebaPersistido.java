package com.kata.evaluacion.dominio;

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
@Table(name = "caso_prueba")
public class CasoPruebaPersistido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false, length = 8000)
    private String entrada;

    @Column(name = "salida_esperada", nullable = false, length = 8000)
    private String salidaEsperada;

    protected CasoPruebaPersistido() {
    }

    public CasoPruebaPersistido(Pregunta pregunta, int orden, String entrada, String salidaEsperada) {
        this.pregunta = pregunta;
        this.orden = orden;
        this.entrada = entrada == null ? "" : entrada;
        this.salidaEsperada = salidaEsperada == null ? "" : salidaEsperada;
    }

    public Long getId() {
        return id;
    }

    public int getOrden() {
        return orden;
    }

    public String getEntrada() {
        return entrada;
    }

    public String getSalidaEsperada() {
        return salidaEsperada;
    }
}
