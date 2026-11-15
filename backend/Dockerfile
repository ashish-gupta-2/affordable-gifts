FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Dependencies first, so they are cached until pom.xml changes.
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN useradd --system --no-create-home appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 10000

# Shell form so ${PORT} is expanded; exec keeps java as PID 1 for clean shutdown.
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-10000} -jar app.jar"]
