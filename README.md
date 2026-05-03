# Movilidad MDQ

Backend-focused full-stack application for comparing transport options in Mar del Plata, Argentina. The app exposes a Spring Boot REST API that estimates Taxi, Uber and DiDi options, then returns prices, estimated time and external app links to a React/Vite interface.

## Why this project matters

Users usually need to open multiple apps to compare transport options. Movilidad MDQ centralizes that decision in one flow: origin, destination, estimated prices and a direct action link.

## Backend highlights

- REST endpoint for trip calculation using Spring Boot.
- Layered structure with controller, service, DTO, model and repository packages.
- Request validation with Jakarta Validation.
- MySQL configuration through environment variables.
- Google Maps Distance Matrix integration for real distance and duration when an API key is available.
- OpenWeather integration to apply weather-based price factors.
- Fallback behavior when external APIs are unavailable.
- Transport result sorting by lowest estimated price.

## Tech stack

### Backend

- Java 25
- Spring Boot 4
- Spring MVC
- Spring Data JPA
- Hibernate Validator
- MySQL
- Maven
- Google Maps Services
- OpenWeather API

### Frontend

- React
- TypeScript
- Vite
- CSS

## API

Base URL:

```text
http://localhost:8080
```

### Calculate transport options

```http
POST /viajes/calcular
Content-Type: application/json
```

Request:

```json
{
  "origen": "Plaza Colon",
  "destino": "Terminal de Omnibus"
}
```

Response:

```json
[
  {
    "tipo": "DIDI",
    "precioMin": 2500.00,
    "precioMax": 3100.00,
    "tiempoMinutos": 15,
    "url": "https://www.didiglobal.com/"
  }
]
```

## Project structure

```text
src/main/java/com/example/movilidadmdq
|-- controller   # REST endpoints
|-- dto          # API request/response objects
|-- enums        # Transport types
|-- model        # JPA entities
|-- repository   # Spring Data repositories
`-- service      # Business logic and external API integrations
```

## Local setup

### 1. Clone the repository

```bash
git clone https://github.com/ciroschot-dev/movilidadMDQ.git
cd movilidadMDQ
```

### 2. Configure environment variables

Create a `.env` file from `.env.example` and set your local values:

```bash
cp .env.example .env
```

Required backend variables:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/movilidadmdq
DB_USER=root
DB_PASSWORD=your_password
GOOGLE_MAPS_KEY=your_google_maps_key
WEATHER_API_KEY=your_openweather_key
```

Spring Boot does not load `.env` files automatically. Load the variables through your IDE run configuration, terminal session or an env-file plugin.

### 3. Run the backend

```bash
./mvnw spring-boot:run
```

The API will run at:

```text
http://localhost:8080
```

### 4. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will run at:

```text
http://localhost:5173
```

## Current status

Functional MVP:

- Backend REST API connected to the frontend.
- Taxi, Uber and DiDi price estimation.
- Google Maps distance/duration integration with fallback.
- Weather factor integration with fallback.
- External app links for transport selection.

## Next improvements

- Move all transport fare configuration fully into database records.
- Add backend tests for fare calculation, sorting and URL generation.
- Add global error responses with `@RestControllerAdvice`.
- Add production deployment with Docker.
- Improve address autocomplete through Google Places.
