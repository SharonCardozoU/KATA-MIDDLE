import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Assessment, PeticionAssessment } from '../modelos/assessment';
import { EvaluacionEnvio } from '../modelos/evaluacion';
import { InicioIntento, ResumenIntento } from '../modelos/intento';
import { PeticionPregunta, Pregunta } from '../modelos/pregunta';

@Injectable({ providedIn: 'root' })
export class AssessmentApi {
  private readonly http = inject(HttpClient);

  listar(): Observable<Assessment[]> {
    return this.http.get<Assessment[]>('/api/evaluaciones');
  }

  obtener(id: number): Observable<Assessment> {
    return this.http.get<Assessment>(`/api/evaluaciones/${id}`);
  }

  crear(peticion: PeticionAssessment): Observable<Assessment> {
    return this.http.post<Assessment>('/api/evaluaciones', peticion);
  }

  actualizar(id: number, peticion: PeticionAssessment): Observable<Assessment> {
    return this.http.put<Assessment>(`/api/evaluaciones/${id}`, peticion);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`/api/evaluaciones/${id}`);
  }

  listarPreguntas(evaluacionId: number): Observable<Pregunta[]> {
    return this.http.get<Pregunta[]>(`/api/evaluaciones/${evaluacionId}/preguntas`);
  }

  crearPregunta(evaluacionId: number, peticion: PeticionPregunta): Observable<Pregunta> {
    return this.http.post<Pregunta>(`/api/evaluaciones/${evaluacionId}/preguntas`, peticion);
  }

  actualizarPregunta(evaluacionId: number, preguntaId: number, peticion: PeticionPregunta): Observable<Pregunta> {
    return this.http.put<Pregunta>(`/api/evaluaciones/${evaluacionId}/preguntas/${preguntaId}`, peticion);
  }

  obtenerPregunta(evaluacionId: number, preguntaId: number): Observable<Pregunta> {
    return this.http.get<Pregunta>(`/api/evaluaciones/${evaluacionId}/preguntas/${preguntaId}`);
  }

  resolverPregunta(
    evaluacionId: number,
    preguntaId: number,
    peticion: { lenguaje: string; codigoFuente: string },
  ): Observable<EvaluacionEnvio> {
    return this.http.post<EvaluacionEnvio>(
      `/api/evaluaciones/${evaluacionId}/preguntas/${preguntaId}/ejecuciones`,
      peticion,
    );
  }

  crearIntento(evaluacionId: number): Observable<InicioIntento> {
    return this.http.post<InicioIntento>(`/api/evaluaciones/${evaluacionId}/intentos`, {});
  }

  obtenerIntento(evaluacionId: number, intentoId: number): Observable<ResumenIntento> {
    return this.http.get<ResumenIntento>(`/api/evaluaciones/${evaluacionId}/intentos/${intentoId}`);
  }

  resolverEnIntento(
    evaluacionId: number,
    intentoId: number,
    preguntaId: number,
    peticion: { lenguaje: string; codigoFuente: string },
  ): Observable<EvaluacionEnvio> {
    return this.http.post<EvaluacionEnvio>(
      `/api/evaluaciones/${evaluacionId}/intentos/${intentoId}/preguntas/${preguntaId}/ejecuciones`,
      peticion,
    );
  }
}
