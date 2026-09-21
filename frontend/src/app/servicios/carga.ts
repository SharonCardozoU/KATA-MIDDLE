import { HttpInterceptorFn } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class Carga {
  private readonly pendientes = signal(0);
  readonly visible = signal(false);
  private demora: ReturnType<typeof setTimeout> | undefined;

  empezar(): void {
    this.pendientes.update((n) => n + 1);
    if (this.demora) {
      return;
    }
    this.demora = setTimeout(() => {
      if (this.pendientes() > 0) {
        this.visible.set(true);
      }
    }, 150);
  }

  terminar(): void {
    this.pendientes.update((n) => Math.max(0, n - 1));
    if (this.pendientes() > 0) {
      return;
    }
    clearTimeout(this.demora);
    this.demora = undefined;
    this.visible.set(false);
  }
}

export const interceptorCarga: HttpInterceptorFn = (req, next) => {
  const carga = inject(Carga);
  carga.empezar();
  return next(req).pipe(finalize(() => carga.terminar()));
};
