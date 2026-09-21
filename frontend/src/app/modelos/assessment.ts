export interface Assessment {
  id: number;
  nombre: string;
  descripcion: string;
  tiempoLimiteMinutos: number;
  cantidadPreguntas: number;
  preguntasRegistradas: number;
  creadoEn: string;
}

export interface PeticionAssessment {
  nombre: string;
  descripcion: string;
  tiempoLimiteMinutos: number;
  cantidadPreguntas: number;
}
