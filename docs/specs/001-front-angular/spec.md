# Spec 001 — MovilidadMDQ en Angular (backlog en Jira)

**Estado**: cerrada ✅ · **Fecha**: 2026-09-15 · **Épica**: (se crean en Jira)

> Los textos entre comillas son los de hoy; se corrigen según A-19/A-20.
> 📄 Los RF01–RF25 salen del documento del TP (ya no se usa: esta spec pasa a ser la fuente). Los nuevos son RF26+.

## Objetivo
Rehacer el frontend (hoy React 19 + Vite + Tailwind) en Angular con las mismas funciones que hoy, aprovechando para corregir textos, bugs y detalles de la interfaz. El backend (Spring Boot en Render) no cambia. Todo el backlog va a Jira (con sprints, puntos y responsables) sincronizado con GitHub.

## Alcance
- ✅ **Entra (E0–E4)**: todo lo que la app hace hoy, incluido el mapa con la ruta (`InputForm.tsx:356` → `MapView`), más las mejoras de A-19.
- ❌ **No entra ni se carga en Jira**: RF09 distancia en opciones, RF10 guardar la opción elegida, RF18 lista/filtro de viajes favoritos, RF22 ver tarifa actual, RF23 historial de precios, RF24 medios de transporte, colectivos; cambios al backend; funciones nuevas.

## Actores
- 🧑 **Usuario** (role USER): sin cuenta solo ve login/registro; con cuenta cotiza, historial, favoritos, perfil, tema.
- 🛡️ **Admin** (role ADMIN): todo lo del usuario + tarifas, usuarios, destinos populares y auditoría. No se puede registrar: el backend lo crea al arrancar si no hay ninguno, con `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD` (variables de entorno en Render). Registro y Google siempre crean USER.

## Precondiciones
Backend en Render; clave de Google Maps/Places; login con Google en el backend (OAuth2 → vuelve con `?token=`).

---

## E1 — Cuenta y sesión
- **RF01** Como usuario quiero crear una cuenta para usar la app.
  - CUANDO completa usuario, email y contraseña y toca REGISTRARME, EL SISTEMA crea la cuenta y lo deja logueado en cotizar. SI falla, muestra "No se pudo crear el usuario."
  - Escenario: Dado "juan" nuevo · Cuando REGISTRARME · Entonces ve cotizar. Dado "juan" ya existe · Entonces el mensaje de error.
- **RF02** Como usuario quiero iniciar y cerrar sesión para acceder a mis viajes y proteger mi cuenta.
  - CUANDO ingresa credenciales válidas, EL SISTEMA abre cotizar; SI son incorrectas, muestra "Usuario o contrasena incorrectos." CUANDO toca "Cerrar sesion", vuelve al login. MIENTRAS haya sesión guardada, entra directo. SI la API dice que venció (401/403), cierra sesión y muestra "Tu sesion vencio. Inicia sesion otra vez."
  - Escenario: Dado juan/1234 · Cuando ENTRAR · Entonces cotizar. Dado juan/mala · Entonces error. Dado token vencido · Cuando cotiza · Entonces login con aviso.
- **RF03** Como usuario quiero iniciar sesión con Google para autenticarme sin crear contraseña.
  - CUANDO toca "Continuar con Google" y Google lo autoriza, EL SISTEMA lo deja logueado y limpia el token de la URL. SI el token no es válido, muestra "No se pudo completar el inicio con Google."
  - Escenario: Dado vuelve con `?token=ok` · Entonces logueado en `/`. Dado `?token=basura` · Entonces error.
- **RF04** Como usuario quiero ver mi perfil para saber con qué datos estoy registrado.
  - CUANDO toca "Mi perfil", EL SISTEMA muestra "Editar Perfil" con su usuario y email.
  - Escenario: Dado juan · Cuando toca Mi perfil · Entonces ve juan y juan@mail.com.
- **RF05** Como usuario quiero modificar mi perfil para mantener mis datos al día.
  - CUANDO guarda, EL SISTEMA actualiza (contraseña vacía = no se cambia) y confirma. SI falla, "No se pudo actualizar el perfil."
  - Escenario: Dado email nuevo y contraseña vacía · Cuando guarda · Entonces confirma y la contraseña vieja sigue andando.
- **RF06** Como usuario quiero eliminar mi cuenta para dejar de usar la app.
  - CUANDO confirma "Eliminar cuenta", EL SISTEMA la borra y cierra sesión. SI falla, "No se pudo eliminar la cuenta."
  - Escenario: Dado juan · Cuando confirma · Entonces login y juan/1234 ya no entra.
- **RF29 (nuevo)** Como usuario quiero elegir tema claro u oscuro para ver la app cómoda.
  - CUANDO cambia el tema, EL SISTEMA lo aplica y lo recuerda.
  - Escenario: Dado claro · Cuando pasa a oscuro y recarga · Entonces sigue oscuro.

## E2 — Búsqueda y comparación de viajes
- **RF07** Como usuario quiero buscar un viaje ingresando origen y destino para saber cuánto me sale.
  - CUANDO toca CALCULAR, EL SISTEMA cotiza y guarda el viaje en su historial. SI falla, "No se pudieron calcular las opciones."
  - Escenario: Dado Centro → Puerto · Cuando CALCULAR · Entonces opciones y el viaje en Historial.
- **RF08** Como usuario quiero autocompletar direcciones y verlas sobre un mapa para cargar rápido y confirmar la ruta.
  - CUANDO escribe, EL SISTEMA sugiere direcciones de Google Places; CUANDO elige origen y destino, el mapa muestra los marcadores y la ruta. CUANDO elige una dirección favorita, completa el campo.
  - Escenario: Dado escribe "Güemes 23" · Entonces sugerencias; Dado elige ambos · Entonces ruta dibujada.
- **RF09 (parcial hoy)** Como usuario quiero ver las opciones Taxi, Uber y Didi con precio y tiempo para compararlas.
  - CUANDO cotiza, EL SISTEMA muestra una tarjeta por opción con precio en ARS y minutos.
  - Escenario: Dado Centro → Puerto · Entonces 3 tarjetas con precio y minutos.
- **RF11 + RF12** Como usuario quiero tocar una opción para abrir Uber/Didi (app o web) o llamar al taxi.
  - CUANDO toca Uber o Didi, EL SISTEMA abre la app (en celular) o la web; CUANDO toca Taxi, inicia la llamada (`tel:`).
  - Escenario: Dado celular con Uber · Cuando toca Uber · Entonces abre la app. Dado Taxi · Entonces abre la llamada.
- **RF26 (nuevo)** Como usuario quiero que el origen se complete con mi ubicación para ahorrar tiempo.
  - CUANDO abre cotizar y permite la ubicación, EL SISTEMA completa el origen. SI no la permite, queda vacío sin error.
  - Escenario: Dado permite · Entonces origen = su dirección. Dado niega · Entonces vacío.

## E3 — Historial y favoritos
- **RF13** Como usuario quiero ver mi historial de viajes para recordar qué coticé.
  - CUANDO abre Historial, EL SISTEMA lista fecha, origen → destino, minutos, km y precios Taxi/Uber/Didi. SI no hay, "Todavia no tenes viajes guardados."
  - Escenario: Dado 2 viajes · Entonces 2 filas. Dado 0 · Entonces el mensaje.
- **RF14** Como usuario quiero eliminar un viaje de mi historial.
  - CUANDO toca "Borrar", EL SISTEMA lo quita y recalcula el viaje frecuente.
  - Escenario: Dado 2 viajes · Cuando borra 1 · Entonces queda 1.
- **RF15** Como usuario quiero ver mi viaje frecuente para repetir lo que más hago.
  - MIENTRAS tenga uno, EL SISTEMA muestra "Viaje frecuente: origen → destino. Lo hiciste N veces". SI no tiene, no aparece.
  - Escenario: Dado 3 veces Centro → Puerto · Entonces "Lo hiciste 3 veces".
- **RF16** Como usuario quiero repetir una búsqueda desde el historial o el viaje frecuente para no cargarla otra vez.
  - CUANDO toca "Repetir" o la tarjeta del frecuente, EL SISTEMA vuelve a cotizar ese viaje.
  - Escenario: Dado Centro → Puerto en historial · Cuando Repetir · Entonces cotizar con opciones.
- **RF17** Como usuario quiero marcar una búsqueda como favorita para guardar sus direcciones.
  - CUANDO toca la ⭐, EL SISTEMA alterna favorito y actualiza sus direcciones favoritas.
  - Escenario: Dado estrella vacía · Cuando la toca · Entonces llena y la dirección en Favoritos.
- **RF18 (parcial hoy)** Como usuario quiero ver mis direcciones favoritas.
  - CUANDO abre Favoritos, EL SISTEMA lista sus direcciones. SI no hay, "No tienes direcciones favoritas".
  - Escenario: Dado 0 · Entonces el mensaje.
- **RF27 (nuevo)** Como usuario quiero renombrar o quitar una dirección favorita para tenerlas ordenadas ("Casa", "Trabajo").
  - CUANDO guarda un nombre, EL SISTEMA lo actualiza (vacío = "Sin nombre"); CUANDO confirma quitarla, la borra.
  - Escenario: Dado un favorito · Cuando lo renombra "Casa" · Entonces "Casa". Cuando lo quita · Entonces desaparece.

## E4 — Administración
- **RF19** Como admin quiero iniciar y cerrar sesión para entrar al panel de administración.
  - CUANDO entra alguien con role ADMIN, EL SISTEMA muestra además las pestañas Admin y Auditoría; a cualquier otro no, aunque se llame "admin".
  - Escenario: Dado juan USER · Entonces 3 pestañas. Dado un USER registrado como "admin" · Entonces 3 pestañas. Dado el admin del sistema · Entonces 4.
- **RF20** Como admin quiero ver y filtrar el historial de búsquedas de los usuarios para auditar el uso.
  - CUANDO filtra por usuario, origen, destino, fechas o favoritos, EL SISTEMA lista usuario, ruta, fecha, detalles y elección ("Sólo consulta" si no hay). SI no hay, "No se encontraron registros".
  - Escenario: Dado filtro "juan" · Entonces sus búsquedas.
- **RF21** Como admin quiero eliminar la cuenta de un usuario para dar de baja a quien corresponda.
  - CUANDO confirma, EL SISTEMA la borra y avisa "Usuario eliminado correctamente".
  - Escenario: Dado juan encontrado · Cuando confirma · Entonces el aviso.
- **RF22 (parcial hoy)** Como admin quiero modificar la tarifa del taxi (bajada de bandera y ficha día/noche, metros por ficha) para que las cotizaciones sean reales.
  - CUANDO guarda, EL SISTEMA la actualiza y confirma; SI falla, "No se pudo actualizar la tarifa. Verifica tus permisos."
  - Escenario: Dado ficha día 150 · Cuando guarda · Entonces confirmación.
- **RF25** Como admin quiero ver y filtrar los destinos más buscados por fechas y zona.
  - CUANDO filtra, EL SISTEMA muestra el top 10 con cantidad; sin filtro, "Aplica un filtro para ver estadísticas".
  - Escenario: Dado zona "Centro" · Cuando FILTRAR · Entonces máx. 10 destinos.
- **RF28 (nuevo)** Como admin quiero buscar un usuario por username o email para gestionarlo.
  - CUANDO busca, EL SISTEMA lo muestra; SI no existe, "Usuario no encontrado".
  - Escenario: Dado "juan@mail.com" · Entonces juan. Dado "nadie" · Entonces el mensaje.

## E0 — Base técnica (solo tareas)
Crear proyecto Angular + Tailwind · cliente HTTP con token y 401/403 · navegación y layout · carga de Google Maps/Places · deploy del front en Vercel (backend en Render desde `main`) · borrar el front React. Los RNF01–RNF08 del TP van en la descripción de esta épica.

## Criterios de finalización
- Cada RF de E1–E4 con su escenario probado en Angular; recorrido lado a lado React vs Angular (celular y escritorio): mismas funciones, con las mejoras de A-19.
- Backlog E0–E4 en Jira y un PR de prueba vinculado a su historia.

## Asunciones
- ✏️ 🔴 A-1: cambiada → ver RF19 y Actores (solo role ADMIN; hoy el front también acepta el nombre "admin", es un bug)
- ✅ 🔴 A-2: la sesión se guarda en el navegador y solo se corta cuando la API dice que venció → RF02
- ✅ 🔴 A-3: confirmado en el backend: el usuario borra la suya y el admin borra cualquiera; pide confirmación y es definitivo → RF06, RF21
- ✅ 🔴 A-4: Jira se carga con el MCP oficial de Atlassian (te logueás vos) y "GitHub for Jira" lo instalás vos → Finalización
- ✏️ A-5: cambiada → 2 actores: ver Actores
- ✏️ A-6: cambiada → se mejora lo que se pueda: ver A-19
- ✏️ A-7: cambiada → el mapa entra: ver RF08
- ✏️ A-8: cambiada → solo E0–E4: ver Alcance
- ✅ A-9: una historia por RF con su número en el título; RF11 y RF12 juntas
- ✏️ A-10: cambiada → nuevos RF26–RF29, colectivos borrados
- ✅ A-11: cada historia lleva en Jira su regla y su escenario; el original es la spec del repo
- ✅ A-12: cada historia tiene sus tareas técnicas
- ✏️ A-13: cambiada → sprints, puntos y responsables (Ciro y Tiago): ver A-21 a A-23
- ✏️ A-14: cambiada → ver A-24
- ✏️ A-16: se cae con RF18 completo fuera de alcance
- ✅ A-17: "igual" se comprueba mirando lado a lado, sin tests de píxeles
- ✏️ A-18: cambiada → el documento del TP no se usa más; esta spec es la fuente
- ✅ A-19: mejoras que entran: sacar "Viajes guardados en AWS RDS" y "guardar historial en AWS"; tildes (contraseña, sesión, todavía, ocurrió); cambiar los carteles del navegador (alert/confirm) por avisos propios; admin solo por role. Otro bug que aparezca → issue tipo Bug en Jira antes de arreglarlo → Alcance
- ✅ A-20: todos los textos en voseo ("¿No tenés cuenta? Registrate"); hoy mezcla "tienes" y "tenes" → A-19
- ✅ A-21: sprints de 2 semanas → Jira
- ✅ A-22: story points 1, 2, 3, 5, 8; los propongo yo y ustedes corrigen en la planning → Jira
- ✅ A-23: reparto propuesto: Ciro → E0 base + E2 búsqueda (mapa); Tiago → E1 cuenta + E3 historial; E4 admin a medias → Jira
- ✅ A-24: una rama por historia o bug que sale de `dev` (`feat/MDQ-12-login`, `fix/MDQ-30-tildes`), PR a `dev`, y `dev` → `main` al cerrar cada sprint → Finalización
- ✅ A-25: se mantienen los números del TP aunque queden huecos (no hay RF23 ni RF24) → RF
