import { Confirmacion } from './confirmacion';

describe('Confirmacion', () => {
  it('muestra el pedido y resuelve al confirmar', async () => {
    const confirmacion = new Confirmacion();
    const espera = confirmacion.pedir({
      titulo: 'Eliminar evaluación',
      mensaje: 'Se borran las preguntas',
      confirmar: 'Eliminar',
    });

    expect(confirmacion.pedido()?.titulo).toBe('Eliminar evaluación');
    confirmacion.responder(true);
    await expect(espera).resolves.toBe(true);
    expect(confirmacion.pedido()).toBeNull();
  });

  it('resuelve en falso si se cancela', async () => {
    const confirmacion = new Confirmacion();
    const espera = confirmacion.pedir({
      titulo: 'Eliminar',
      mensaje: 'No',
      confirmar: 'Eliminar',
    });
    confirmacion.responder(false);
    await expect(espera).resolves.toBe(false);
  });
});
