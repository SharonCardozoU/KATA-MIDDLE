export interface InicioIntento {
  id: number;
  evaluacionId: number;
}

export type EstadoPregunta = 'PENDIENTE' | 'CORRECTA' | 'INCORRECTA';

export interface ResumenPregunta {
  preguntaId: number;
  nombre: string;
  puntajeMaximo: number;
  estado: EstadoPregunta;
  respondida: boolean;
  compilacionExitosa: boolean;
  casosExitosos: number;
  totalCasos: number;
  porcentaje: number;
  puntaje: number;
}

export interface ResumenIntento {
  id: number;
  evaluacionId: number;
  nombreEvaluacion: string;
  iniciadoEn: string;
  tiempoLimiteMinutos: number;
  tiempoConsumidoSegundos: number;
  tiempoRestanteSegundos: number;
  preguntasRespondidas: number;
  totalPreguntas: number;
  preguntasCorrectas: number;
  preguntasIncorrectas: number;
  puntajeObtenido: number;
  puntajeMaximo: number;
  porcentaje: number;
  preguntas: ResumenPregunta[];
}
