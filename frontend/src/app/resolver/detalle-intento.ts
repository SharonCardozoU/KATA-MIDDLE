import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ResumenIntento } from '../modelos/intento';
import { AssessmentApi } from '../servicios/assessment-api';
import { etiquetaEstado, formatearSegundos, segundosRestantes } from './reloj';

@Component({
  selector: 'app-detalle-intento',
  imports: [RouterLink],
  templateUrl: './detalle-intento.html',
  styleUrl: './detalle-intento.css',
})
export class DetalleIntento implements OnInit, OnDestroy {
  private readonly api = inject(AssessmentApi);
  private readonly ruta = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private temporizador: ReturnType<typeof setInterval> | undefined;

  readonly resumen = signal<ResumenIntento | null>(null);
  readonly restante = signal(0);
  readonly error = signal<string | null>(null);

  readonly formatearSegundos = formatearSegundos;
  readonly etiquetaEstado = etiquetaEstado;

  ngOnInit(): void {
    const evaluacionId = Number(this.ruta.snapshot.paramMap.get('id'));
    const intentoId = Number(this.ruta.snapshot.paramMap.get('intentoId'));
    this.api.obtenerIntento(evaluacionId, intentoId).subscribe({
      next: (item) => {
        this.resumen.set(item);
        this.arrancarReloj(item);
      },
      error: () => this.error.set('No se pudo cargar el assessment.'),
    });
  }

  ngOnDestroy(): void {
    clearInterval(this.temporizador);
  }

  private arrancarReloj(item: ResumenIntento): void {
    const irAResultados = () => {
      clearInterval(this.temporizador);
      this.router.navigate(['/resolver', item.evaluacionId, 'intento', item.id, 'resultado']);
    };
    const segundos = segundosRestantes(item.iniciadoEn, item.tiempoLimiteMinutos);
    this.restante.set(segundos);
    if (segundos <= 0) {
      irAResultados();
      return;
    }
    this.temporizador = setInterval(() => {
      const actual = segundosRestantes(item.iniciadoEn, item.tiempoLimiteMinutos);
      this.restante.set(actual);
      if (actual <= 0) {
        irAResultados();
      }
    }, 1000);
  }
}
