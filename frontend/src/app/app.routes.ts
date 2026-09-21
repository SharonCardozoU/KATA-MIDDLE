import { Routes } from '@angular/router';

import { ListaAssessments } from './assessments/lista-assessments';
import { GestionPreguntas } from './assessments/gestion-preguntas';
import { Evaluacion } from './evaluacion/evaluacion';
import { Portada } from './portada/portada';
import { ListaResolver } from './resolver/lista-resolver';
import { DetalleIntento } from './resolver/detalle-intento';
import { PreguntasResolver } from './resolver/preguntas-resolver';
import { ResolverPregunta } from './resolver/resolver-pregunta';
import { ResultadoIntento } from './resolver/resultado-intento';

export const routes: Routes = [
  { path: '', component: Portada },
  { path: 'admin', component: ListaAssessments },
  { path: 'admin/evaluaciones/:id/preguntas', component: GestionPreguntas },
  { path: 'admin/laboratorio', component: Evaluacion },
  { path: 'evaluaciones/:id/preguntas', redirectTo: 'admin/evaluaciones/:id/preguntas' },
  { path: 'laboratorio', redirectTo: 'admin/laboratorio' },
  { path: 'resolver', component: ListaResolver },
  { path: 'resolver/:id', component: PreguntasResolver },
  { path: 'resolver/:id/intento/:intentoId', component: DetalleIntento },
  { path: 'resolver/:id/preguntas/:preguntaId', component: ResolverPregunta },
  { path: 'resolver/:id/intento/:intentoId/preguntas/:preguntaId', component: ResolverPregunta },
  { path: 'resolver/:id/intento/:intentoId/resultado', component: ResultadoIntento },
];
