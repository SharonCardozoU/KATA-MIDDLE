export function formatearSegundos(segundos: number): string {
  const total = Math.max(0, Math.floor(segundos));
  const horas = Math.floor(total / 3600);
  const minutos = Math.floor((total % 3600) / 60);
  const resto = total % 60;
  const mm = String(minutos).padStart(2, '0');
  const ss = String(resto).padStart(2, '0');
  return horas > 0 ? `${horas}:${mm}:${ss}` : `${mm}:${ss}`;
}

export function segundosRestantes(iniciadoEn: string, tiempoLimiteMinutos: number): number {
  const limite = tiempoLimiteMinutos * 60;
  const transcurrido = (Date.now() - new Date(iniciadoEn).getTime()) / 1000;
  return Math.max(0, Math.floor(limite - transcurrido));
}

export function segundosConsumidos(iniciadoEn: string): number {
  return Math.max(0, Math.floor((Date.now() - new Date(iniciadoEn).getTime()) / 1000));
}

export function etiquetaEstado(estado: string): string {
  switch (estado) {
    case 'CORRECTA':
      return 'Correcta';
    case 'INCORRECTA':
      return 'Incorrecta';
    default:
      return 'Pendiente';
  }
}
