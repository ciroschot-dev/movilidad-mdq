import { Component, inject } from '@angular/core';
import { AuthService } from '../../services/auth';
import { Tema } from '../../services/tema';

@Component({
  selector: 'app-header',
  imports: [],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  protected readonly tema = inject(Tema);
  protected readonly auth = inject(AuthService);
}
