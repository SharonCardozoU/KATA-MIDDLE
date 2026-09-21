package com.kata.evaluacion.dominio;

import java.math.BigDecimal;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "pregunta")
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluacion_id", nullable = false)
    private Evaluacion evaluacion;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 4000)
    private String descripcion;

    @Column(nullable = false, length = 40)
    private String lenguaje;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal puntaje;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<CasoPruebaPersistido> casos = new ArrayList<>();

    protected Pregunta() {
    }

    public Pregunta(Evaluacion evaluacion, String nombre, String descripcion, String lenguaje, BigDecimal puntaje) {
        this.evaluacion = evaluacion;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.lenguaje = lenguaje;
        this.puntaje = puntaje;
        this.creadoEn = Instant.now();
    }

    public void agregarCaso(String entrada, String salidaEsperada) {
        casos.add(new CasoPruebaPersistido(this, casos.size(), entrada, salidaEsperada));
    }

    public void actualizar(String nombre, String descripcion, String lenguaje, BigDecimal puntaje) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.lenguaje = lenguaje;
        this.puntaje = puntaje;
    }

    public Long getId() {
        return id;
    }

    public Evaluacion getEvaluacion() {
        return evaluacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getLenguaje() {
        return lenguaje;
    }

    public BigDecimal getPuntaje() {
        return puntaje;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public List<CasoPruebaPersistido> getCasos() {
        return casos;
    }
}
