import { etiquetaEstado, formatearSegundos, segundosRestantes } from './reloj';

describe('reloj', () => {
  it('formatea minutos y segundos', () => {
    expect(formatearSegundos(65)).toBe('01:05');
    expect(formatearSegundos(3723)).toBe('1:02:03');
    expect(formatearSegundos(-8)).toBe('00:00');
  });

  it('etiqueta el estado de una pregunta', () => {
    expect(etiquetaEstado('CORRECTA')).toBe('Correcta');
    expect(etiquetaEstado('INCORRECTA')).toBe('Incorrecta');
    expect(etiquetaEstado('PENDIENTE')).toBe('Pendiente');
  });

  it('calcula los segundos que quedan del intento', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-01-01T00:01:00.000Z'));
    expect(segundosRestantes('2026-01-01T00:00:00.000Z', 2)).toBe(60);
    expect(segundosRestantes('2026-01-01T00:00:00.000Z', 1)).toBe(0);
    vi.useRealTimers();
  });
});
