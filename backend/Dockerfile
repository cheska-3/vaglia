# ── Build stage: compile with Maven, produce the runnable jar ──
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only what's needed to resolve dependencies first, so Docker can cache
# this layer and skip re-downloading everything when only source code changes.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src src
RUN mvn package -DskipTests -B

# ── Run stage: just the JRE + the jar, no Maven/JDK in the final image ──
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
