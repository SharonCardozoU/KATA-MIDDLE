package com.kata.evaluacion.motor;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Valores que respaldan las medidas declaradas en EC01 y EC03. Viven en
 * configuracion para que los limites se puedan ajustar sin recompilar.
 */
@ConfigurationProperties(prefix = "kata.ejecucion")
public class LimitesEjecucion {

    private int segundosPorCaso = 5;
    private int memoriaMb = 256;
    private double cpus = 1.0;
    private int limitePids = 64;
    private int tiempoMaximoTotalSegundos = 30;
    private int maxEjecucionesConcurrentes = 4;
    private int maxTamanoCodigoBytes = 64 * 1024;
    private int maxCasosPorEnvio = 20;
    private int maxTamanoEntradaBytes = 8 * 1024;

    public int getSegundosPorCaso() {
        return segundosPorCaso;
    }

    public void setSegundosPorCaso(int segundosPorCaso) {
        this.segundosPorCaso = segundosPorCaso;
    }

    public int getMemoriaMb() {
        return memoriaMb;
    }

    public void setMemoriaMb(int memoriaMb) {
        this.memoriaMb = memoriaMb;
    }

    public double getCpus() {
        return cpus;
    }

    public void setCpus(double cpus) {
        this.cpus = cpus;
    }

    public int getLimitePids() {
        return limitePids;
    }

    public void setLimitePids(int limitePids) {
        this.limitePids = limitePids;
    }

    public int getTiempoMaximoTotalSegundos() {
        return tiempoMaximoTotalSegundos;
    }

    public void setTiempoMaximoTotalSegundos(int tiempoMaximoTotalSegundos) {
        this.tiempoMaximoTotalSegundos = tiempoMaximoTotalSegundos;
    }

    public int getMaxEjecucionesConcurrentes() {
        return maxEjecucionesConcurrentes;
    }

    public void setMaxEjecucionesConcurrentes(int maxEjecucionesConcurrentes) {
        this.maxEjecucionesConcurrentes = maxEjecucionesConcurrentes;
    }

    public int getMaxTamanoCodigoBytes() {
        return maxTamanoCodigoBytes;
    }

    public void setMaxTamanoCodigoBytes(int maxTamanoCodigoBytes) {
        this.maxTamanoCodigoBytes = maxTamanoCodigoBytes;
    }

    public int getMaxCasosPorEnvio() {
        return maxCasosPorEnvio;
    }

    public void setMaxCasosPorEnvio(int maxCasosPorEnvio) {
        this.maxCasosPorEnvio = maxCasosPorEnvio;
    }

    public int getMaxTamanoEntradaBytes() {
        return maxTamanoEntradaBytes;
    }

    public void setMaxTamanoEntradaBytes(int maxTamanoEntradaBytes) {
        this.maxTamanoEntradaBytes = maxTamanoEntradaBytes;
    }
}
