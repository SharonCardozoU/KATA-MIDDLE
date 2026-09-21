export interface CasoPrueba {
  entrada: string;
  salidaEsperada: string;
}

export interface PeticionEjecucion {
  lenguaje: string;
  codigoFuente: string;
  casos: CasoPrueba[];
  puntajeMaximo: number;
}

export type EstadoCaso = 'OK' | 'SALIDA_DISTINTA' | 'TIEMPO_AGOTADO' | 'ERROR_EJECUCION';

export interface ResultadoCaso {
  indice: number;
  entrada: string;
  salidaEsperada: string;
  salidaObtenida: string;
  estado: EstadoCaso;
  mensajeError: string | null;
  milisegundos: number;
}

export interface ResultadoEjecucion {
  compilacionExitosa: boolean;
  mensajeCompilacion: string;
  casos: ResultadoCaso[];
}

export interface Calificacion {
  casosExitosos: number;
  totalCasos: number;
  porcentaje: number;
  puntaje: number;
}

export interface EvaluacionEnvio {
  ejecucion: ResultadoEjecucion;
  calificacion: Calificacion;
  milisegundosTotales: number;
}
