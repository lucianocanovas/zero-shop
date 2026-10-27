# ==========================================================
# Etapa 1: Compilación del código fuente con Maven (Java 21)
# ==========================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================================
# Etapa 2: Imagen liviana de ejecución (JRE 21)
# ==========================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear carpeta para almacenamiento de archivos y uploads
RUN mkdir -p /app/uploads/products

COPY --from=builder /app/target/*.jar app.jar

# Variables de entorno para optimizar memoria en contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
