# =========================
# 1) STAGE BUILD (Maven)
# =========================
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Copiamos todo el proyecto
COPY . .

# Compilamos el jar (sin tests para acelerar)
RUN mvn clean package -DskipTests

# =========================
# 2) STAGE RUNTIME (Java)
# =========================
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Traemos SOLO el jar generado en el stage anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]