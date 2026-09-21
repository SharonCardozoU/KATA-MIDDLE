import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { EvaluacionEnvio, PeticionEjecucion } from '../modelos/evaluacion';

/**
 * Capa Modelo del MVVM. Es lo unico del frontend que conoce la forma de la API,
 * asi que un cambio de contrato se absorbe aqui sin tocar la vista.
 *
 * Las rutas son relativas a proposito: el servidor de desarrollo de Angular las
 * reenvia al backend segun proxy.conf.json, con lo cual el navegador nunca hace
 * una peticion entre origenes distintos y no hace falta habilitar CORS.
 */
@Injectable({ providedIn: 'root' })
export class EvaluacionApi {
  private readonly http = inject(HttpClient);

  lenguajes(): Observable<string[]> {
    return this.http.get<string[]>('/api/lenguajes');
  }

  ejecutar(peticion: PeticionEjecucion): Observable<EvaluacionEnvio> {
    return this.http.post<EvaluacionEnvio>('/api/ejecuciones', peticion);
  }
}
