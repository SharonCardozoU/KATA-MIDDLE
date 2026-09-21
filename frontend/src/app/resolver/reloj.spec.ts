import { etiquetaEstado, formatearSegundos } from './reloj';

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
});
