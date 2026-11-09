import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Mapa } from '../../components/mapa/mapa';
import { DireccionFavorita, Lugar } from '../../models/lugar';
import { CalculoViaje, OpcionTransporte, ViajeARepetir } from '../../models/viaje';
import { LugarService } from '../../services/lugares';
import { ViajeService } from '../../services/viaje';

// Los dos campos del formulario que tienen autocompletado
type Campo = 'origen' | 'destino';

// Pantalla para cotizar: se eligen origen y destino de la lista de Google, el mapa muestra la ruta
// y el back devuelve cuánto sale en Taxi, Uber y Didi
@Component({
  selector: 'app-cotizar',
  imports: [ReactiveFormsModule, Mapa],
  templateUrl: './cotizar.html',
  styleUrl: './cotizar.css',
})
export class Cotizar {
  private readonly viajes = inject(ViajeService);
  private readonly lugares = inject(LugarService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  // Lo que devolvió el back: una tarjeta por opción (RF09)
  protected readonly opciones = signal<OpcionTransporte[]>([]);
  // El back manda el tipo en mayúsculas (TAXI); en la tarjeta se ve con el nombre de siempre
  protected readonly nombres = { TAXI: 'Taxi', UBER: 'Uber', DIDI: 'Didi' };
  // Aviso que se muestra si el back no pudo cotizar
  protected readonly error = signal('');
  // true mientras espera al back: bloquea el botón para no cotizar dos veces
  protected readonly cargando = signal(false);
  // true mientras busca la ubicación del celular: bloquea su botón para no pedirla de nuevo
  protected readonly ubicando = signal(false);

  // El texto que se ve en cada campo
  protected readonly busquedaForm = this.fb.nonNullable.group({
    origen: ['', Validators.required],
    destino: ['', Validators.required],
  });

  // La dirección elegida de la lista para cada campo; null = todavía no se eligió
  protected readonly origen = signal<Lugar | null>(null);
  protected readonly destino = signal<Lugar | null>(null);
  // Las sugerencias de Google y el campo debajo del que se muestran
  protected readonly sugerencias = signal<google.maps.places.PlacePrediction[]>([]);
  protected readonly campoActivo = signal<Campo | null>(null);
  // El camino entre las dos direcciones; el mapa lo dibuja cuando no es null
  protected readonly ruta = signal<google.maps.DirectionsResult | null>(null);
  // Las direcciones favoritas del usuario (RF08), para elegirlas en origen o destino sin escribirlas.
  // Solo las que tienen los datos de Google, que el mapa y el back necesitan
  private readonly favoritas = signal<DireccionFavorita[]>([]);

  constructor() {
    // Las favoritas se piden una sola vez al abrir la pantalla. Si falla no se avisa: se cotiza igual,
    // solo que escribiendo las direcciones
    this.viajes.direccionesFavoritas().subscribe({
      next: (favoritas) =>
        this.favoritas.set(
          favoritas.filter((f) => f.placeId !== null && f.lat !== null && f.lng !== null),
        ),
      error: () => {},
    });

    // Si se llegó desde "Repetir" en el historial (RF16), vienen las dos direcciones: se cotiza ese viaje
    // y no se pide la ubicación, que pisaría el origen del viaje que se quiere repetir
    const repetir = this.router.currentNavigation()?.extras.state?.['repetir'] as
      ViajeARepetir | undefined;
    if (repetir) {
      this.repetir(repetir);
      return;
    }

    // Al abrir la pantalla, si el navegador ya tiene el permiso de ubicación dado, el origen se completa solo (RF26).
    // Si al usuario todavía no se lo pidió, acá no se pide: los navegadores no muestran el cartel de permiso
    // sin un toque del usuario, así que en ese caso queda el link "Usar mi ubicación" debajo del campo
    navigator.permissions
      ?.query({ name: 'geolocation' })
      .then((permiso) => {
        if (permiso.state === 'granted') {
          this.usarMiUbicacion();
        }
      })
      // Si el navegador no sabe responder por los permisos, todo sigue por el link, como siempre
      .catch(() => {});
  }

  // Error solo si el campo está vacío Y el usuario ya lo tocó; si no, se vería en rojo apenas abre la página
  protected mostrarError(control: Campo) {
    return this.busquedaForm.get(control)?.invalid && this.busquedaForm.get(control)?.touched;
  }

  // Hay texto pero no se eligió de la lista: el back necesita la dirección de Google, no solo el texto
  protected faltaElegir(campo: Campo) {
    const control = this.busquedaForm.controls[campo];
    return control.valid && control.touched && this.lugar(campo)() === null;
  }

  // Se llama con cada letra que se escribe en origen o destino
  protected async escribir(campo: Campo) {
    const texto = this.busquedaForm.controls[campo].value.trim();
    // Si cambió el texto, la dirección elegida antes ya no corresponde: hay que volver a elegir.
    // También se borran la ruta y los precios, que eran de las direcciones viejas
    this.lugar(campo).set(null);
    this.ruta.set(null);
    this.opciones.set([]);
    this.campoActivo.set(campo);

    // Con menos de 3 letras las sugerencias no sirven, así no se le pide a Google de más
    if (texto.length < 3) {
      this.sugerencias.set([]);
      return;
    }
    try {
      const sugerencias = await this.lugares.sugerir(texto);
      // Si mientras esperaba se siguió escribiendo o se pasó al otro campo, esta respuesta ya es vieja:
      // se descarta, así no aparecen las sugerencias del origen debajo del destino
      if (
        this.campoActivo() === campo &&
        this.busquedaForm.controls[campo].value.trim() === texto
      ) {
        this.sugerencias.set(sugerencias);
      }
    } catch {
      // Si Google no responde, el campo sigue andando como texto, solo que sin sugerencias
      this.sugerencias.set([]);
    }
  }

  // Al entrar a un campo se muestran las favoritas, así se pueden elegir sin escribir nada
  protected abrir(campo: Campo) {
    this.campoActivo.set(campo);
  }

  // Las favoritas que coinciden con lo escrito en el campo (por nombre o por dirección); con el campo vacío,
  // todas. Hasta 5, para que la lista no tape el mapa
  protected favoritasPara(campo: Campo) {
    const texto = this.busquedaForm.controls[campo].value.trim().toLowerCase();
    return this.favoritas()
      .filter(
        (f) =>
          !texto ||
          f.direccion.toLowerCase().includes(texto) ||
          !!f.nombre?.toLowerCase().includes(texto),
      )
      .slice(0, 5);
  }

  // Al tocar una favorita: completa el campo como si se hubiera elegido de Google, sin pedirle nada (RF08)
  protected elegirFavorita(campo: Campo, favorita: DireccionFavorita) {
    this.cerrarSugerencias();
    this.error.set('');
    // Los precios y la ruta que hubiera eran de la dirección anterior
    this.opciones.set([]);
    this.ruta.set(null);
    this.busquedaForm.controls[campo].setValue(favorita.direccion);
    this.lugar(campo).set({
      // Si todavía no se le puso nombre ("Casa"), el nombre corto es el primer pedazo de la dirección
      nombre: favorita.nombre || favorita.direccion.split(',')[0],
      direccion: favorita.direccion,
      placeId: favorita.placeId!,
      lat: favorita.lat!,
      lng: favorita.lng!,
    });
    this.trazarRuta();
  }

  // Al tocar una sugerencia: completa el campo, guarda la dirección y, si ya están las dos, arma la ruta
  protected async elegir(campo: Campo, prediccion: google.maps.places.PlacePrediction) {
    this.cerrarSugerencias();
    // Borra el aviso de un intento anterior, así no queda un error viejo arriba del botón
    this.error.set('');
    try {
      const lugar = await this.lugares.elegir(prediccion);
      this.busquedaForm.controls[campo].setValue(lugar.direccion);
      this.lugar(campo).set(lugar);
      this.trazarRuta();
    } catch {
      this.error.set('No se pudo cargar esa dirección. Probá de nuevo.');
    }
  }

  // Completa el origen con la ubicación del celular (RF26). El navegador muestra su cartel de permiso
  // y, si el usuario dice que no, el campo queda vacío y sin error, como pide la regla de la historia
  protected usarMiUbicacion() {
    // Un navegador viejo puede no tener ubicación: ahí el botón no hace nada
    if (!navigator.geolocation) {
      return;
    }
    this.ubicando.set(true);
    // Tope de 10 segundos: si el navegador no contesta (por ejemplo, porque el cartel de permiso quedó
    // abierto y nadie lo tocó), el botón vuelve a la normalidad en vez de quedar en "Ubicando…" para siempre
    setTimeout(() => this.ubicando.set(false), 10000);
    try {
      navigator.geolocation.getCurrentPosition(
        async (posicion) => {
          try {
            const lugar = await this.lugares.desdeCoordenadas(
              posicion.coords.latitude,
              posicion.coords.longitude,
            );
            // Queda igual que una dirección elegida de la lista: el texto en el campo y la dirección guardada
            this.busquedaForm.controls.origen.setValue(lugar.direccion);
            this.origen.set(lugar);
            this.error.set('');
            this.trazarRuta();
          } catch {
            // Si Google no encuentra la dirección, el origen queda como estaba
          } finally {
            this.ubicando.set(false);
          }
        },
        // Sin permiso o sin señal de GPS no se muestra nada: el usuario completa el origen a mano
        () => this.ubicando.set(false),
        // Si el celular no tiene forma de ubicarse, el navegador avisa con error a los 10 segundos
        { timeout: 10000 },
      );
    } catch {
      // Un navegador que bloquea la ubicación puede fallar en el acto: se destraba el botón
      this.ubicando.set(false);
    }
  }

  // Carga las dos direcciones del viaje a repetir como si se hubieran elegido de la lista y cotiza (RF16)
  private repetir({ origen, destino }: ViajeARepetir) {
    this.busquedaForm.setValue({ origen: origen.direccion, destino: destino.direccion });
    this.origen.set(origen);
    this.destino.set(destino);
    this.trazarRuta();
    this.calcular();
  }

  // Al salir del campo se esconde la lista (por eso las sugerencias se eligen con mousedown, que llega antes)
  protected cerrarSugerencias() {
    this.sugerencias.set([]);
    this.campoActivo.set(null);
  }

  protected calcular() {
    const origen = this.origen();
    const destino = this.destino();
    // El botón ya está bloqueado sin las dos direcciones; esto es por si se aprieta Enter igual
    if (!origen || !destino) {
      return;
    }
    this.error.set('');
    this.cargando.set(true);
    // Arma el pedido: el texto de los campos más los datos de Google de cada dirección
    const pedido: CalculoViaje = {
      ...this.busquedaForm.getRawValue(),
      origenAddressLine1: origen.nombre,
      origenAddressLine2: origen.direccion,
      origenPlaceId: origen.placeId,
      origenLat: origen.lat,
      origenLng: origen.lng,
      destinoAddressLine1: destino.nombre,
      destinoAddressLine2: destino.direccion,
      destinoPlaceId: destino.placeId,
      destinoLat: destino.lat,
      destinoLng: destino.lng,
    };
    this.viajes.calcular(pedido).subscribe({
      // Salió bien: guarda las opciones y libera el botón
      next: (opciones) => {
        // Si mientras esperaba se cambió alguna dirección, estos precios son del viaje anterior: no se muestran
        if (this.origen() === origen && this.destino() === destino) {
          this.opciones.set(opciones);
        }
        this.cargando.set(false);
      },
      // Salió mal: borra las opciones viejas y muestra el motivo que manda el back.
      // Si el back no respondió, no hay motivo y queda el aviso genérico.
      error: (respuesta: HttpErrorResponse) => {
        this.opciones.set([]);
        this.error.set(respuesta.error?.message ?? 'No se pudieron calcular las opciones.');
        this.cargando.set(false);
      },
    });
  }

  // Al tocar una tarjeta se le avisa al back cuál eligió el usuario (RF10). No se espera la respuesta
  // ni se bloquea el link: si el back falla, la app o el teléfono se abren igual
  protected registrarEleccion(opcion: OpcionTransporte) {
    if (opcion.viajeId === null) {
      return; // el back no pudo guardar el viaje, así que no hay nada que actualizar
    }
    this.viajes.elegir(opcion.viajeId, opcion.tipo).subscribe({
      error: () => {
        // Se traga el error a propósito: perder la estadística no puede impedir abrir Uber o Didi
      },
    });
  }

  // El back puede mandar centavos (4532.67): se redondea y se separan los miles con punto, como en Argentina ($ 4.533)
  protected formatearPrecio(precio: number) {
    return '$ ' + Math.round(precio).toLocaleString('es-AR');
  }

  // El signal que guarda la dirección de cada campo
  private lugar(campo: Campo) {
    return campo === 'origen' ? this.origen : this.destino;
  }

  // Con las dos direcciones elegidas, le pide a Google el camino para que el mapa lo dibuje
  private async trazarRuta() {
    const origen = this.origen();
    const destino = this.destino();
    if (!origen || !destino) {
      return;
    }
    try {
      const ruta = await this.lugares.ruta(origen, destino);
      // Si mientras esperaba se cambió alguna dirección, esta ruta ya no corresponde
      if (this.origen() === origen && this.destino() === destino) {
        this.ruta.set(ruta);
      }
    } catch {
      // Sin ruta el mapa queda como estaba; se puede cotizar igual
      this.ruta.set(null);
    }
  }
}
