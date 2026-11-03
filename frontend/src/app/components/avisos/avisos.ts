import { Component, inject } from '@angular/core';
import { AvisoService } from '../../services/avisos';

// Dibuja el aviso y la ventana de confirmación que pide AvisoService.
// Va una sola vez en app.html, así aparece arriba de cualquier pantalla.
@Component({
  selector: 'app-avisos',
  imports: [],
  templateUrl: './avisos.html',
  styleUrl: './avisos.css',
})
export class Avisos {
  protected readonly avisos = inject(AvisoService);
}
