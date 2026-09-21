package com.kata.evaluacion.aplicacion;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.kata.evaluacion.motor.RegistroEjecutores;
import com.kata.evaluacion.motor.modelo.ResultadoEjecucion;
import com.kata.evaluacion.motor.modelo.SolicitudEjecucion;

/**
 * Facade sobre el subsistema de ejecucion. La capa de presentacion depende solo
 * de esta clase, no del registro, ni del sandbox, ni de los ejecutores.
 */
@Service
public class OrquestadorEjecucion {

    private final RegistroEjecutores registro;
    private final Calificador calificador;

    public OrquestadorEjecucion(RegistroEjecutores registro, Calificador calificador) {
        this.registro = registro;
        this.calificador = calificador;
    }

    public EvaluacionEnvio evaluar(SolicitudEjecucion solicitud, double puntajeMaximo) {
        long inicio = System.currentTimeMillis();
        ResultadoEjecucion resultado = registro.para(solicitud.lenguaje()).ejecutar(solicitud);
        Calificacion calificacion = calificador.calificar(resultado, puntajeMaximo);
        return new EvaluacionEnvio(resultado, calificacion, System.currentTimeMillis() - inicio);
    }

    public Set<String> lenguajesSoportados() {
        return registro.lenguajesSoportados();
    }
}
