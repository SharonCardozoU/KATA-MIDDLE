import { routes } from './app.routes';

describe('rutas', () => {
  it('separa admin y candidato sin login', () => {
    const caminos = routes.map((ruta) => ruta.path);
    expect(caminos).toContain('');
    expect(caminos).toContain('admin');
    expect(caminos).toContain('resolver');
  });

  it('deja el laboratorio en la vista de administrador', () => {
    expect(routes.some((ruta) => ruta.path === 'admin/laboratorio')).toBe(true);
    expect(routes.find((ruta) => ruta.path === 'laboratorio')?.redirectTo).toBe('admin/laboratorio');
  });
});
