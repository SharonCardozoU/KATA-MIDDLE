import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Portada } from './portada';

describe('Portada', () => {
  it('muestra las dos vistas', async () => {
    await TestBed.configureTestingModule({
      imports: [Portada],
      providers: [provideRouter([])],
    }).compileComponents();

    const fixture = TestBed.createComponent(Portada);
    fixture.detectChanges();

    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Administrador');
    expect(texto).toContain('Candidato');
  });
});
