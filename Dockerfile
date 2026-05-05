# =========================
# 1) STAGE BUILD (Maven)
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build
# (Nota: Usamos imagen 21 para compilar ya que es la más estable en Docker Hub, 
# pero el runtime será Java 25 para tu código)

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# =========================
# 2) STAGE RUNTIME (Java 21)
# =========================
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
