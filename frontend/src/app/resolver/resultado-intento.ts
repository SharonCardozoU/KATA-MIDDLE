import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ResumenIntento } from '../modelos/intento';
import { AssessmentApi } from '../servicios/assessment-api';
import { etiquetaEstado, formatearSegundos } from './reloj';

@Component({
  selector: 'app-resultado-intento',
  imports: [RouterLink],
  templateUrl: './resultado-intento.html',
  styleUrl: './resultado-intento.css',
})
export class ResultadoIntento implements OnInit {
  private readonly api = inject(AssessmentApi);
  private readonly ruta = inject(ActivatedRoute);

  readonly resumen = signal<ResumenIntento | null>(null);
  readonly error = signal<string | null>(null);
  readonly formatearSegundos = formatearSegundos;
  readonly etiquetaEstado = etiquetaEstado;

  ngOnInit(): void {
    const evaluacionId = Number(this.ruta.snapshot.paramMap.get('id'));
    const intentoId = Number(this.ruta.snapshot.paramMap.get('intentoId'));
    this.api.obtenerIntento(evaluacionId, intentoId).subscribe({
      next: (item) => this.resumen.set(item),
      error: () => this.error.set('No se pudo cargar el resultado de la evaluación.'),
    });
  }
}
