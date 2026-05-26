FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# cache de dependências separado do código-fonte
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress

# ─── Imagem final — só JRE, sem Maven ───────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# usuário não-root por segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/financeira-moreira-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# JVM tunada para containers: heap máximo = 75% da RAM disponível
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
