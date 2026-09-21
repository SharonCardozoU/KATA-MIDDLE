import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';

import { CasoPrueba, EstadoCaso, EvaluacionEnvio } from '../modelos/evaluacion';
import { PLANTILLAS } from '../modelos/plantillas';
import { EvaluacionApi } from '../servicios/evaluacion-api';

/**
 * Capa VistaModelo del MVVM. Guarda el estado de la pantalla en señales y traduce
 * la respuesta del backend a algo que la vista pueda pintar. No sabe nada de HTTP:
 * eso es responsabilidad de EvaluacionApi.
 */
@Component({
  selector: 'app-evaluacion',
  templateUrl: './evaluacion.html',
  styleUrl: './evaluacion.css',
})
export class Evaluacion implements OnInit {
  private readonly api = inject(EvaluacionApi);

  readonly lenguajes = signal<string[]>([]);
  readonly lenguaje = signal('python');
  readonly codigo = signal(PLANTILLAS['python']);
  readonly casos = signal<CasoPrueba[]>([
    { entrada: '42', salidaEsperada: '84' },
    { entrada: '7', salidaEsperada: '14' },
  ]);

  readonly resultado = signal<EvaluacionEnvio | null>(null);
  readonly ejecutando = signal(false);
  readonly error = signal<string | null>(null);

  readonly puedeEjecutar = computed(
    () => !this.ejecutando() && this.codigo().trim().length > 0 && this.casos().length > 0,
  );

  ngOnInit(): void {
    this.api.lenguajes().subscribe({
      next: (lista) => this.lenguajes.set(lista),
      error: () =>
        this.error.set('No se pudo consultar los lenguajes. Verifica que el backend este arriba en el puerto 8080.'),
    });
  }

  cambiarLenguaje(nuevo: string): void {
    const clave = (nuevo ?? '').toLowerCase().trim();
    this.lenguaje.set(clave);
    this.codigo.set(PLANTILLAS[clave] ?? '');
    this.resultado.set(null);
  }

  agregarCaso(): void {
    this.casos.update((lista) => [...lista, { entrada: '', salidaEsperada: '' }]);
  }

  eliminarCaso(indice: number): void {
    this.casos.update((lista) => lista.filter((_, i) => i !== indice));
  }

  actualizarEntrada(indice: number, valor: string): void {
    this.casos.update((lista) =>
      lista.map((caso, i) => (i === indice ? { ...caso, entrada: valor } : caso)),
    );
  }

  actualizarEsperada(indice: number, valor: string): void {
    this.casos.update((lista) =>
      lista.map((caso, i) => (i === indice ? { ...caso, salidaEsperada: valor } : caso)),
    );
  }

  ejecutar(): void {
    this.ejecutando.set(true);
    this.error.set(null);
    this.resultado.set(null);

    this.api
      .ejecutar({
        lenguaje: this.lenguaje(),
        codigoFuente: this.codigo(),
        casos: this.casos(),
        puntajeMaximo: 10,
      })
      .subscribe({
        next: (respuesta) => {
          this.resultado.set(respuesta);
          this.ejecutando.set(false);
        },
        error: (fallo: HttpErrorResponse) => {
          this.error.set(this.mensajeDeError(fallo));
          this.ejecutando.set(false);
        },
      });
  }

  etiquetaDeEstado(estado: EstadoCaso): string {
    switch (estado) {
      case 'OK':
        return 'Correcto';
      case 'SALIDA_DISTINTA':
        return 'Salida distinta';
      case 'TIEMPO_AGOTADO':
        return 'Tiempo agotado';
      case 'ERROR_EJECUCION':
        return 'Error de ejecucion';
      default:
        return estado;
    }
  }

  /**
   * El backend distingue 400 por entrada invalida, 422 por lenguaje no soportado
   * y 429 por capacidad copada, y en los tres casos envia un campo 'mensaje'.
   * Vale la pena mostrarlo tal cual en vez de un error genarico.
   */
  private mensajeDeError(fallo: HttpErrorResponse): string {
    if (fallo.status === 0) {
      return 'Sin conexion con el backend.';
    }
    if (fallo.error?.mensaje) {
      return fallo.error.mensaje;
    }
    return `El servidor respondio ${fallo.status}.`;
  }
}
