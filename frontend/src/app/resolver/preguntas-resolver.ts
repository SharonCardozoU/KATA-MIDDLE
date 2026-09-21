import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AssessmentApi } from '../servicios/assessment-api';

@Component({
  selector: 'app-preguntas-resolver',
  template: `
    @if (error()) {
      <p class="aviso aviso-error">{{ error() }}</p>
    } @else {
      <p>Iniciando assessment…</p>
    }
  `,
})
export class PreguntasResolver implements OnInit {
  private readonly api = inject(AssessmentApi);
  private readonly ruta = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.api.crearIntento(id).subscribe({
      next: (intento) => this.router.navigate(['/resolver', id, 'intento', intento.id], { replaceUrl: true }),
      error: (fallo: HttpErrorResponse) => {
        this.error.set('No se pudo iniciar la evaluación. ' + (fallo.error?.mensaje || `HTTP ${fallo.status || 0}`));
      },
    });
  }
}
