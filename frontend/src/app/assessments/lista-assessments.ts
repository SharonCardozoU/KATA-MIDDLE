import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Assessment } from '../modelos/assessment';
import { AssessmentApi } from '../servicios/assessment-api';

@Component({
  selector: 'app-lista-assessments',
  imports: [RouterLink],
  templateUrl: './lista-assessments.html',
  styleUrl: './lista-assessments.css',
})
export class ListaAssessments implements OnInit {
  private readonly api = inject(AssessmentApi);

  readonly assessments = signal<Assessment[]>([]);
  readonly nombre = signal('');
  readonly descripcion = signal('');
  readonly tiempoLimiteMinutos = signal(60);
  readonly cantidadPreguntas = signal(5);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);

  readonly puedeCrear = computed(
    () => !this.guardando() && this.nombre().trim().length > 0 && this.tiempoLimiteMinutos() >= 1
      && this.cantidadPreguntas() >= 1,
  );

  ngOnInit(): void {
    this.cargar();
  }

  crear(): void {
    this.guardando.set(true);
    this.error.set(null);
    this.api
      .crear({
        nombre: this.nombre().trim(),
        descripcion: this.descripcion().trim(),
        tiempoLimiteMinutos: this.tiempoLimiteMinutos(),
        cantidadPreguntas: this.cantidadPreguntas(),
      })
      .subscribe({
        next: (creada) => {
          this.assessments.update((lista) => [creada, ...lista]);
          this.nombre.set('');
          this.descripcion.set('');
          this.tiempoLimiteMinutos.set(60);
          this.cantidadPreguntas.set(5);
          this.guardando.set(false);
        },
        error: (fallo: HttpErrorResponse) => {
          this.error.set(this.mensajeDeError(fallo));
          this.guardando.set(false);
        },
      });
  }

  private cargar(): void {
    this.api.listar().subscribe({
      next: (lista) => this.assessments.set(lista),
      error: (fallo: HttpErrorResponse) => this.error.set(this.mensajeDeError(fallo)),
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
