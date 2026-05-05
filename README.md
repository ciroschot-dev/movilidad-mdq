# MovilidadMDQ 🚗💨

**MovilidadMDQ** es una aplicación diseñada para comparar precios de diferentes servicios de transporte (Taxi, Uber,
Didi) en la ciudad de Mar del Plata. Utiliza factores dinámicos como el clima, el horario y la demanda para ofrecer
estimaciones precisas y actualizadas.

---

## 🚀 Tecnologías Utilizadas

- **Backend:** Java 25, Spring Boot 4.x, Spring Data JPA.
- **Frontend:** React 19, TypeScript, Vite, Tailwind CSS.
- **APIs Externas:** Google Maps (Distance Matrix, Places Autocomplete), OpenWeather API.
- **Infraestructura:** Docker & Docker Compose.

---

## 🛠️ Configuración Inicial

### 1. Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto basado en `.env.example`:

```env
# Base de Datos
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/movilidadmdq
DB_USER=root
DB_PASSWORD=tu_contraseña

# APIs
google.maps.api.key=TU_API_KEY_AQUI
openweather.api.key=TU_API_KEY_AQUI

# JWT
JWT_SECRET=tu_clave_secreta_super_segura_para_jwt_aqui_generala_con_base64
JWT_EXPIRATION=86400000

# Google OAuth2
GOOGLE_OAUTH_CLIENT_ID=tu_client_id_de_google_console
GOOGLE_OAUTH_CLIENT_SECRET=tu_client_secret_de_google_console
```

> **Nota:** Para el frontend, asegúrate de configurar también las variables necesarias en `frontend/.env` (como
`VITE_GOOGLE_MAPS_API_KEY`).

---

## 🐳 Despliegue con Docker

El proyecto incluye soporte para Docker, lo que facilita la ejecución del backend en un entorno aislado.

### Requisitos Previos

- Docker y Docker Compose instalados.
- Tener generado el archivo `.jar` del backend.

### Pasos para Ejecutar

1. **Compilar el proyecto (Generar el JAR):**
   Desde la raíz del proyecto, ejecuta:
   ```bash
   ./mvnw clean package -DskipTests
   ```
   Esto generará el archivo ejecutable en la carpeta `target/`.

2. **Levantar el contenedor:**
   ```bash
   docker compose up --build
   ```

### Detalles del Dockerfile

- **Imagen Base:** `eclipse-temurin:21-jdk` (Java 21).
- **Puerto expuesto:** `8080`.
- **Funcionamiento:** Toma cualquier archivo `.jar` dentro de `target/` y lo ejecuta al iniciar el contenedor.

---

## 💻 Ejecución Local (Desarrollo)

### Backend

1. Asegúrate de tener instalado JDK 21.
2. Ejecuta:
   ```bash
   ./mvnw spring-boot:run
   ```

### Frontend

1. Navega a la carpeta del frontend:
   ```bash
   cd frontend
   ```
2. Instala las dependencias:
   ```bash
   npm install
   ```
3. Inicia el servidor de desarrollo:
   ```bash
   npm run dev
   ```

---

## 📊 Estado del Proyecto

Actualmente, el sistema cuenta con:

- ✅ Cálculo de tarifas de Taxi (Diurna/Nocturna).
- ✅ Estimación dinámica para Uber y Didi (basada en clima y factores temporales).
- ✅ Autocompletado de direcciones en Mar del Plata.
- 🚧 Integración persistente con MySQL (En proceso).

---

## 📄 Licencia

Este proyecto es de uso académico para la materia Programación 3 (UTN).
