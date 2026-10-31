import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth';
import { Tema } from '../../services/tema';

// Barra de arriba de todas las pantallas: la marca, las pestañas y el botón del tema
@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  protected readonly tema = inject(Tema);
  protected readonly auth = inject(AuthService);
}
