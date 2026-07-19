# Paso 1: Compilar la aplicación
# Paso 1: Compilar la aplicación usando Java 21 y Maven
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
COPY . .
RUN mvn clean package -DskipTests

# Paso 2: Ejecutar la aplicación con una imagen ligera de Java 21
FROM eclipse-temurin:21-jre-jammy
COPY --from=build /target/*.jar app.jar

# Configuración crucial para no superar los 512MB de RAM de Render
ENV JAVA_OPTS="-XX:+UseSerialGC -Xss512k -XX:MaxRAMPercentage=70.0"

EXPOSE 8080

# Arrancamos pasando las optimizaciones de memoria (JAVA_OPTS)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]