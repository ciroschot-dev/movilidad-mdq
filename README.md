🚕 MovilidadMDQ

Aplicación web para comparar opciones de transporte (Taxi, Uber y Didi) en Mar del Plata 🇦🇷 en una sola pantalla.

⸻

🎯 ¿Qué hace?

MovilidadMDQ permite al usuario:

* Ingresar un origen y un destino
* Ver precios estimados de distintos transportes
* Comparar tiempo y costo
* Elegir una opción y ser redirigido directamente a la app correspondiente

⸻

🧠 Problema que resuelve

Hoy, para saber cuál es la mejor forma de viajar, el usuario debe:

* Abrir Uber
* Abrir Didi
* Estimar el taxi manualmente

👉 MovilidadMDQ centraliza todo en un solo lugar, ahorrando tiempo y esfuerzo.

⸻

⚙️ Tecnologías utilizadas

🧱 Backend

* Java
* Spring Boot
* API REST

🎨 Frontend

* React
* TypeScript
* Vite
* Tailwind CSS

⸻

🚀 Cómo ejecutar el proyecto

1. Clonar el repositorio

git clone
cd movilidadmdq

⸻

⚠️ Configuración de Base de Datos (OBLIGATORIO)

Antes de ejecutar el backend, debes configurar tu base de datos.

Variables de entorno (.env)

La app usa variables de entorno definidas en:
src/main/resources/application.properties

Spring Boot NO carga automáticamente el archivo .env, por lo que debes cargarlas manualmente.

⸻

1) Crear archivo .env

Crear un archivo .env en la raíz del proyecto usando .env.example como base.

⸻

2) Completar variables

Variables necesarias:

* SPRING_DATASOURCE_URL → URL JDBC de MySQL
  Ej: jdbc:mysql://localhost:3306/movilidadmdq
* DB_USER → Usuario de la base de datos
* DB_PASSWORD → Contraseña de la base de datos

⸻

3) Cargar variables en IntelliJ

Opción recomendada: plugin .env

1. Ir a Run/Debug Configurations
2. Activar el plugin .env (EnvFile)
3. Seleccionar el archivo .env
4. Ejecutar la app normalmente

⸻

▶️ Ejecutar Backend

Abrir el proyecto en IntelliJ y correr:

MovilidadMdqApplication.java

👉 El backend correrá en:
http://localhost:8080

⸻

▶️ Ejecutar Frontend

Desde la carpeta del frontend:

cd frontend
npm install
npm run dev

👉 Abrir en el navegador:
http://localhost:5173

⸻

🧪 Cómo usar la app

1. Ingresar:
   📍 Origen
   📍 Destino
2. Presionar:
   CALCULAR 🚀
3. Ver resultados:
   🚕 Taxi
   🚗 Uber
   🚙 Didi
4. Presionar:
   Elegir

👉 Se abrirá la app correspondiente o se iniciará una llamada (Taxi).

⸻

🔗 Integraciones

* API propia (Spring Boot)
* Deep linking a Uber y Didi
* Llamada telefónica para Taxi (tel:)

⸻

⚠️ Notas importantes

* Los precios son estimativos
* Uber y Didi se calculan mediante lógica aproximada
* Taxi utiliza tarifa real (bajada de bandera + fichas)

⸻

📌 Estado del proyecto

🟢 MVP funcional completo:

* Backend conectado
* Frontend funcional
* Comparación de precios
* Redirección a apps externas

⸻

🚀 Próximas mejoras

* Integración con Google Maps (distancia real)
* Autocompletado de direcciones
* Mejor estimación de precios (clima, horario)
* Deploy en producción