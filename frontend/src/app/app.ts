import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Avisos } from './components/avisos/avisos';
import { Header } from './components/header/header';

@Component({
  imports: [RouterOutlet, Header, Avisos],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {}
