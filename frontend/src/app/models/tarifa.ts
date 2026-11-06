// Los cinco valores con los que el back calcula cuánto sale un viaje en taxi (sistema de fichas)
export interface Tarifa {
  bajadaBanderaDia: number; // lo que se cobra al subir, de día (6 a 22)
  bajadaBanderaNoche: number; // lo mismo, de noche (22 a 6)
  valorFichaDia: number; // cuánto sale cada ficha de día
  valorFichaNoche: number; // cuánto sale cada ficha de noche
  metrosPorFicha: number; // cada cuántos metros recorridos se cobra una ficha nueva
}
