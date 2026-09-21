package com.kata.evaluacion.dominio;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Assessment de la plataforma. cantidadPreguntas es el cupo planeado, no el
 * conteo de filas de preguntas: esas llegan en el siguiente incremento.
 */
@Entity
@Table(name = "evaluacion")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(name = "tiempo_limite_minutos", nullable = false)
    private int tiempoLimiteMinutos;

    @Column(name = "cantidad_preguntas", nullable = false)
    private int cantidadPreguntas;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected Evaluacion() {
    }

    public Evaluacion(String nombre, String descripcion, int tiempoLimiteMinutos, int cantidadPreguntas) {
        this.nombre = nombre;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.tiempoLimiteMinutos = tiempoLimiteMinutos;
        this.cantidadPreguntas = cantidadPreguntas;
        this.creadoEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getTiempoLimiteMinutos() {
        return tiempoLimiteMinutos;
    }

    public int getCantidadPreguntas() {
        return cantidadPreguntas;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void actualizar(String nombre, String descripcion, int tiempoLimiteMinutos, int cantidadPreguntas) {
        this.nombre = nombre;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.tiempoLimiteMinutos = tiempoLimiteMinutos;
        this.cantidadPreguntas = cantidadPreguntas;
    }
}
