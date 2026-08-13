# =========================
# 1) STAGE BUILD (Maven)
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build
# Compilamos y ejecutamos con Java 21 para respetar la versión del proyecto.

WORKDIR /app
COPY pom.xml .
# Descarga dependencias primero (cache de Docker layers)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# =========================
# 2) STAGE RUNTIME (Java 21)
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
# Memoria optimizada para free tier (~512 MB)
ENTRYPOINT ["java", "-Xmx384m", "-Xms128m", "-XX:+UseSerialGC", "-jar", "app.jar"]
