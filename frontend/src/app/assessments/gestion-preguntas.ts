import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { Assessment } from '../modelos/assessment';
import { PeticionPregunta, Pregunta } from '../modelos/pregunta';
import { AssessmentApi } from '../servicios/assessment-api';
import { EvaluacionApi } from '../servicios/evaluacion-api';

@Component({
  selector: 'app-gestion-preguntas',
  imports: [RouterLink],
  templateUrl: './gestion-preguntas.html',
  styleUrl: './gestion-preguntas.css',
})
export class GestionPreguntas implements OnInit {
  private readonly api = inject(AssessmentApi);
  private readonly motor = inject(EvaluacionApi);
  private readonly ruta = inject(ActivatedRoute);

  readonly assessment = signal<Assessment | null>(null);
  readonly preguntas = signal<Pregunta[]>([]);
  readonly lenguajes = signal<string[]>([]);
  readonly error = signal<string | null>(null);
  readonly guardando = signal(false);
  readonly editandoId = signal<number | null>(null);

  readonly nombre = signal('');
  readonly descripcion = signal('');
  readonly lenguaje = signal('python');
  readonly puntaje = signal(10);
  readonly casos = signal<{ entrada: string; salidaEsperada: string }[]>([
    { entrada: '', salidaEsperada: '' },
  ]);

  readonly cupoLleno = computed(() => {
    const item = this.assessment();
    return item !== null && this.preguntas().length >= item.cantidadPreguntas;
  });

  readonly mostrarFormulario = computed(() => this.editandoId() !== null || !this.cupoLleno());

  readonly puedeGuardar = computed(
    () =>
      !this.guardando() &&
      this.nombre().trim().length > 0 &&
      this.descripcion().trim().length > 0 &&
      this.puntaje() > 0 &&
      this.casos().some((caso) => caso.salidaEsperada.trim().length > 0),
  );

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.motor.lenguajes().subscribe({
      next: (lista) => {
        this.lenguajes.set(lista);
        if (lista.length > 0 && this.editandoId() === null) {
          this.lenguaje.set(lista.includes('python') ? 'python' : lista[0]);
        }
      },
    });
    this.api.obtener(id).subscribe({
      next: (item) => this.assessment.set(item),
      error: () => this.error.set('No se pudo cargar la evaluación.'),
    });
    this.api.listarPreguntas(id).subscribe({
      next: (lista) => this.preguntas.set(lista),
      error: (fallo: HttpErrorResponse) => this.error.set(this.mensajeDeError(fallo)),
    });
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

  editar(pregunta: Pregunta): void {
    this.editandoId.set(pregunta.id);
    this.nombre.set(pregunta.nombre);
    this.descripcion.set(pregunta.descripcion);
    this.lenguaje.set(pregunta.lenguaje);
    this.puntaje.set(pregunta.puntaje);
    this.casos.set(
      pregunta.casos.length > 0
        ? pregunta.casos.map((caso) => ({ entrada: caso.entrada, salidaEsperada: caso.salidaEsperada }))
        : [{ entrada: '', salidaEsperada: '' }],
    );
    this.error.set(null);
  }

  cancelar(): void {
    this.limpiarFormulario();
  }

  guardar(): void {
    const item = this.assessment();
    if (!item) {
      return;
    }
    const peticion: PeticionPregunta = {
      nombre: this.nombre().trim(),
      descripcion: this.descripcion().trim(),
      lenguaje: this.lenguaje(),
      puntaje: this.puntaje(),
      casos: this.casos().filter((caso) => caso.salidaEsperada.trim().length > 0),
    };
    this.guardando.set(true);
    this.error.set(null);
    const editando = this.editandoId();
    const pedido =
      editando === null
        ? this.api.crearPregunta(item.id, peticion)
        : this.api.actualizarPregunta(item.id, editando, peticion);
    pedido.subscribe({
      next: (guardada) => {
        if (editando === null) {
          this.preguntas.update((lista) => [...lista, guardada]);
        } else {
          this.preguntas.update((lista) => lista.map((p) => (p.id === guardada.id ? guardada : p)));
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

  private limpiarFormulario(): void {
    this.editandoId.set(null);
    this.nombre.set('');
    this.descripcion.set('');
    this.puntaje.set(10);
    this.casos.set([{ entrada: '', salidaEsperada: '' }]);
    const primero = this.lenguajes()[0];
    if (primero) {
      this.lenguaje.set(primero);
    }
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
