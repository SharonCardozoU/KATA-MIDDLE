import { Injectable, signal } from '@angular/core';

export interface PedidoConfirmacion {
  titulo: string;
  mensaje: string;
  confirmar: string;
}

@Injectable({ providedIn: 'root' })
export class Confirmacion {
  readonly pedido = signal<PedidoConfirmacion | null>(null);
  private resolver: ((ok: boolean) => void) | null = null;

  pedir(pedido: PedidoConfirmacion): Promise<boolean> {
    this.pedido.set(pedido);
    return new Promise((resolve) => {
      this.resolver = resolve;
    });
  }

  responder(ok: boolean): void {
    this.pedido.set(null);
    this.resolver?.(ok);
    this.resolver = null;
  }
}
