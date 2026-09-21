package com.kata.evaluacion.motor.sandbox;

/**
 * Frontera de aislamiento de la arquitectura. Es la costura que permite cambiar
 * el mecanismo de aislamiento sin tocar los ejecutores por lenguaje ni el
 * orquestador: hoy contenedores Docker, manana gVisor o un servicio aparte.
 */
public interface Sandbox {

    ResultadoComando ejecutar(EspecificacionSandbox especificacion);
}
