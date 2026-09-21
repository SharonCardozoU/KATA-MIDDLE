import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { EstadoCaso, EvaluacionEnvio } from '../modelos/evaluacion';
import { Pregunta } from '../modelos/pregunta';
import { PLANTILLAS_VACIAS } from '../modelos/plantillas';
import { AssessmentApi } from '../servicios/assessment-api';
import { EvaluacionApi } from '../servicios/evaluacion-api';
import { formatearSegundos, segundosRestantes } from './reloj';

@Component({
  selector: 'app-resolver-pregunta',
  imports: [RouterLink],
  templateUrl: './resolver-pregunta.html',
  styleUrl: './resolver-pregunta.css',
})
export class ResolverPregunta implements OnInit, OnDestroy {
  private readonly api = inject(AssessmentApi);
  private readonly motor = inject(EvaluacionApi);
  private readonly ruta = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly evaluacionId = signal(0);
  readonly intentoId = signal(0);
  readonly preguntaId = signal(0);
  readonly preguntas = signal<Pregunta[]>([]);
  readonly pregunta = signal<Pregunta | null>(null);
  readonly lenguajes = signal<string[]>([]);
  readonly lenguaje = signal('');
  readonly codigo = signal('');
  readonly resultado = signal<EvaluacionEnvio | null>(null);
  readonly ejecutando = signal(false);
  readonly error = signal<string | null>(null);
  readonly restante = signal(0);
  readonly relojListo = signal(false);
  readonly tiempoAgotado = computed(() => this.relojListo() && this.restante() <= 0);
  readonly formatearSegundos = formatearSegundos;
  private temporizador: ReturnType<typeof setInterval> | undefined;

  readonly indice = computed(() => this.preguntas().findIndex((p) => p.id === this.preguntaId()));
  readonly siguienteId = computed(() => {
    const i = this.indice();
    return i >= 0 && i < this.preguntas().length - 1 ? this.preguntas()[i + 1].id : null;
  });
  readonly esUltima = computed(() => {
    const i = this.indice();
    return i >= 0 && i === this.preguntas().length - 1;
  });
  readonly progreso = computed(() => {
    const i = this.indice();
    return i < 0 ? '' : `Pregunta ${i + 1} de ${this.preguntas().length}`;
  });
  readonly puedeEjecutar = computed(
    () => !this.ejecutando() && !this.tiempoAgotado() && this.codigo().trim().length > 0,
  );

  ngOnInit(): void {
    this.motor.lenguajes().subscribe({
      next: (lista) => this.lenguajes.set(lista),
    });
    this.ruta.paramMap.subscribe((params) => {
      const evaluacionId = Number(params.get('id'));
      const preguntaId = Number(params.get('preguntaId'));
      const intentoParam = params.get('intentoId');
      this.evaluacionId.set(evaluacionId);
      this.preguntaId.set(preguntaId);

      if (!intentoParam) {
        this.api.crearIntento(evaluacionId).subscribe({
          next: (intento) =>
            this.router.navigate(
              ['/resolver', evaluacionId, 'intento', intento.id, 'preguntas', preguntaId],
              { replaceUrl: true },
            ),
          error: (fallo: HttpErrorResponse) =>
            this.error.set(
              'No se pudo iniciar la evaluación. ' +
                (fallo.error?.mensaje || `El servidor respondio ${fallo.status || 0}.`),
            ),
        });
        return;
      }

      this.intentoId.set(Number(intentoParam));
      this.resultado.set(null);
      this.cargarPreguntas(evaluacionId);
      this.cargarPregunta(evaluacionId, preguntaId);
      this.iniciarReloj(evaluacionId, Number(intentoParam));
    });
  }

  ngOnDestroy(): void {
    clearInterval(this.temporizador);
  }

  cambiarLenguaje(nuevo: string): void {
    const clave = (nuevo ?? '').toLowerCase().trim();
    this.lenguaje.set(clave);
    this.codigo.set(PLANTILLAS_VACIAS[clave] ?? '');
    this.resultado.set(null);
  }

  ejecutar(): void {
    this.ejecutando.set(true);
    this.error.set(null);
    this.resultado.set(null);
    this.api
      .resolverEnIntento(this.evaluacionId(), this.intentoId(), this.preguntaId(), {
        lenguaje: this.lenguaje(),
        codigoFuente: this.codigo(),
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

  irSiguiente(): void {
    const siguiente = this.siguienteId();
    if (siguiente) {
      this.router.navigate([
        '/resolver',
        this.evaluacionId(),
        'intento',
        this.intentoId(),
        'preguntas',
        siguiente,
      ]);
    }
  }

  irResultado(): void {
    this.router.navigate(['/resolver', this.evaluacionId(), 'intento', this.intentoId(), 'resultado']);
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
    }
  }

  private iniciarReloj(evaluacionId: number, intentoId: number): void {
    clearInterval(this.temporizador);
    this.relojListo.set(false);
    this.api.obtenerIntento(evaluacionId, intentoId).subscribe({
      next: (item) => {
        const irAResultados = () => {
          clearInterval(this.temporizador);
          this.router.navigate(['/resolver', item.evaluacionId, 'intento', item.id, 'resultado']);
        };
        this.restante.set(segundosRestantes(item.iniciadoEn, item.tiempoLimiteMinutos));
        this.relojListo.set(true);
        if (this.restante() <= 0) {
          irAResultados();
          return;
        }
        this.temporizador = setInterval(() => {
          const segundos = segundosRestantes(item.iniciadoEn, item.tiempoLimiteMinutos);
          this.restante.set(segundos);
          if (segundos <= 0) {
            irAResultados();
          }
        }, 1000);
      },
    });
  }

  private cargarPreguntas(evaluacionId: number): void {
    this.api.listarPreguntas(evaluacionId).subscribe({
      next: (lista) => this.preguntas.set(lista),
    });
  }

  private cargarPregunta(evaluacionId: number, preguntaId: number): void {
    this.api.obtenerPregunta(evaluacionId, preguntaId).subscribe({
      next: (pregunta) => {
        this.pregunta.set(pregunta);
        this.cambiarLenguaje(pregunta.lenguaje);
      },
      error: () => this.error.set('No se pudo cargar la pregunta.'),
    });
  }

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
