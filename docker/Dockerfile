FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/target/*.war app.war

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "/app/app.war"]