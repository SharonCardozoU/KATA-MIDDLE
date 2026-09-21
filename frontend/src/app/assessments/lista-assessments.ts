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
  readonly editandoId = signal<number | null>(null);
  readonly eliminandoId = signal<number | null>(null);
  readonly porEliminar = signal<Assessment | null>(null);
  readonly error = signal<string | null>(null);

  readonly minimoPreguntas = computed(() => {
    const id = this.editandoId();
    if (id === null) {
      return 1;
    }
    const item = this.assessments().find((actual) => actual.id === id);
    return Math.max(1, item?.preguntasRegistradas ?? 1);
  });

  readonly puedeGuardar = computed(
    () =>
      !this.guardando() &&
      this.nombre().trim().length > 0 &&
      this.tiempoLimiteMinutos() >= 1 &&
      this.cantidadPreguntas() >= this.minimoPreguntas(),
  );

  ngOnInit(): void {
    this.cargar();
  }

  editar(item: Assessment): void {
    this.editandoId.set(item.id);
    this.nombre.set(item.nombre);
    this.descripcion.set(item.descripcion);
    this.tiempoLimiteMinutos.set(item.tiempoLimiteMinutos);
    this.cantidadPreguntas.set(item.cantidadPreguntas);
    this.error.set(null);
  }

  cancelar(): void {
    this.limpiarFormulario();
  }

  guardar(): void {
    this.guardando.set(true);
    this.error.set(null);
    const peticion = {
      nombre: this.nombre().trim(),
      descripcion: this.descripcion().trim(),
      tiempoLimiteMinutos: this.tiempoLimiteMinutos(),
      cantidadPreguntas: this.cantidadPreguntas(),
    };
    const editando = this.editandoId();
    const pedido = editando === null ? this.api.crear(peticion) : this.api.actualizar(editando, peticion);
    pedido.subscribe({
      next: (guardada) => {
        if (editando === null) {
          this.assessments.update((lista) => [guardada, ...lista]);
        } else {
          this.assessments.update((lista) => lista.map((item) => (item.id === guardada.id ? guardada : item)));
        }
        this.limpiarFormulario();
        this.guardando.set(false);
      },
      error: (fallo: HttpErrorResponse) => {
        this.error.set(this.mensajeDeError(fallo));
        this.guardando.set(false);
      },
    });
  }

  eliminar(item: Assessment): void {
    this.porEliminar.set(item);
  }

  cancelarEliminar(): void {
    this.porEliminar.set(null);
  }

  confirmarEliminar(): void {
    const item = this.porEliminar();
    if (!item) {
      return;
    }
    this.porEliminar.set(null);
    this.eliminandoId.set(item.id);
    this.error.set(null);
    this.api.eliminar(item.id).subscribe({
      next: () => {
        this.assessments.update((lista) => lista.filter((actual) => actual.id !== item.id));
        if (this.editandoId() === item.id) {
          this.limpiarFormulario();
        }
        this.eliminandoId.set(null);
      },
      error: (fallo: HttpErrorResponse) => {
        this.error.set(this.mensajeDeError(fallo));
        this.eliminandoId.set(null);
      },
    });
  }

  private cargar(): void {
    this.api.listar().subscribe({
      next: (lista) => this.assessments.set(lista),
      error: (fallo: HttpErrorResponse) => this.error.set(this.mensajeDeError(fallo)),
    });
  }

  private limpiarFormulario(): void {
    this.editandoId.set(null);
    this.nombre.set('');
    this.descripcion.set('');
    this.tiempoLimiteMinutos.set(60);
    this.cantidadPreguntas.set(5);
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
