import { PLANTILLAS, PLANTILLAS_VACIAS } from './plantillas';

describe('plantillas', () => {
  const lenguajes = ['python', 'java', 'javascript', 'typescript', 'cobol'];

  it('tiene plantilla vacia y de ejemplo para cada lenguaje', () => {
    for (const lenguaje of lenguajes) {
      expect(PLANTILLAS_VACIAS[lenguaje].length).toBeGreaterThan(0);
      expect(PLANTILLAS[lenguaje].length).toBeGreaterThan(0);
    }
  });

  it('exige la clase Solucion en Java', () => {
    expect(PLANTILLAS['java']).toContain('public class Solucion');
  });

  it('usa el esqueleto COBOL que el motor puede compilar', () => {
    expect(PLANTILLAS['cobol']).toContain('IDENTIFICATION DIVISION');
    expect(PLANTILLAS['cobol']).toContain('PROGRAM-ID. SOLUCION');
  });
});
