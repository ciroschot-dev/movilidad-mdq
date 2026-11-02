// Cartel que aparece abajo de la pantalla y se va solo: verde si salió bien, rojo si falló
export interface Aviso {
  texto: string;
  tipo: 'exito' | 'error';
}

// Ventana con Aceptar y Cancelar, en lugar del confirm() del navegador
export interface Pregunta {
  texto: string;
  alAceptar: () => void; // lo que se hace si el usuario toca Aceptar
}
