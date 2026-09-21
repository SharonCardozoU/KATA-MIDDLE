import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Assessment } from '../modelos/assessment';
import { AssessmentApi } from '../servicios/assessment-api';

@Component({
  selector: 'app-lista-resolver',
  templateUrl: './lista-resolver.html',
  styleUrl: './lista-resolver.css',
})
export class ListaResolver implements OnInit {
  private readonly api = inject(AssessmentApi);
  private readonly router = inject(Router);

  readonly assessments = signal<Assessment[]>([]);
  readonly error = signal<string | null>(null);
  readonly iniciandoId = signal<number | null>(null);

  ngOnInit(): void {
    this.api.listar().subscribe({
      next: (lista) => this.assessments.set(lista.filter((item) => item.preguntasRegistradas > 0)),
      error: (fallo: HttpErrorResponse) => {
        this.error.set(fallo.status === 0 ? 'Sin conexion con el backend.' : 'No se pudieron cargar las evaluaciones.');
      },
    });
  }

  entrar(item: Assessment): void {
    this.iniciandoId.set(item.id);
    this.api.crearIntento(item.id).subscribe({
      next: (intento) => this.router.navigate(['/resolver', item.id, 'intento', intento.id]),
      error: (fallo: HttpErrorResponse) => {
        this.error.set('No se pudo iniciar. ' + (fallo.error?.mensaje || `HTTP ${fallo.status || 0}`));
        this.iniciandoId.set(null);
      },
    });
  }
}
