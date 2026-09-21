import { Carga } from './carga';

describe('Carga', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('no muestra el overlay si la peticion termina rapido', () => {
    const carga = new Carga();
    carga.empezar();
    expect(carga.visible()).toBe(false);
    carga.terminar();
    vi.advanceTimersByTime(300);
    expect(carga.visible()).toBe(false);
  });

  it('muestra el overlay si la peticion tarda', () => {
    const carga = new Carga();
    carga.empezar();
    vi.advanceTimersByTime(150);
    expect(carga.visible()).toBe(true);
    carga.terminar();
    expect(carga.visible()).toBe(false);
  });
});
