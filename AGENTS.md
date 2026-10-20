# MovilidadMDQ — instrucciones del proyecto

Back en Spring Boot (`src/`) y front en Angular (`frontend/`). Equipo: Ciro Schot y Tiago Fueyo Vuillermoz.

## Front (Angular): solo lo que vimos en Progra IV

Todo lo que entra al front tiene que poder defenderse en el coloquio. Por eso se usa solo lo visto en clase y en el primer parcial.

- Angular 22 standalone. Nada de NgModules, `*ngIf` ni `*ngFor`: se usan `@if` y `@for`.
- Servicios con `@Service()` e `inject()`. HTTP con `provideHttpClient()` y `HttpClient`.
- Estado con `signal`, `computed`, `linkedSignal` y `toSignal()`. Formularios con Signal Forms (`form()`, `[formField]`) o con `ReactiveFormsModule`.
- Rutas planas con `RouterLink` / `RouterLinkActive` y ruta comodín. Componentes con `input()`, `output()` y `model()`.
- Estilos: CSS común por componente, colores siempre con `var(--…)`, Flexbox y Grid. Sin Tailwind, Bootstrap ni librerías de UI.
- Ninguna dependencia nueva sin el OK de los dos.
- Ante la duda, angular.dev.
