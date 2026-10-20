import { Component, inject } from '@angular/core';
import { Tema } from '../../services/tema';

@Component({
  selector: 'app-header',
  imports: [],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  protected readonly tema = inject(Tema);
}
