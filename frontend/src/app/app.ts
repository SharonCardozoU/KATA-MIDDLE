import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';

import { Carga } from './servicios/carga';
import { Confirmacion } from './servicios/confirmacion';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  private readonly router = inject(Router);
  readonly carga = inject(Carga);
  readonly confirmacion = inject(Confirmacion);

  readonly url = toSignal(
    this.router.events.pipe(
      filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd),
      map(() => this.router.url),
      startWith(this.router.url),
    ),
    { initialValue: this.router.url },
  );

  readonly vista = computed(() => {
    const actual = this.url();
    if (actual.startsWith('/admin')) {
      return 'admin';
    }
    if (actual.startsWith('/resolver')) {
      return 'candidato';
    }
    return 'portada';
  });
}
