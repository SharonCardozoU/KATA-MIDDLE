export interface CasoPruebaPregunta {
  orden: number;
  entrada: string;
  salidaEsperada: string;
}

export interface Pregunta {
  id: number;
  nombre: string;
  descripcion: string;
  lenguaje: string;
  puntaje: number;
  creadoEn: string;
  casos: CasoPruebaPregunta[];
}

export interface PeticionPregunta {
  nombre: string;
  descripcion: string;
  lenguaje: string;
  puntaje: number;
  casos: { entrada: string; salidaEsperada: string }[];
}
