FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system app \
    && useradd --system --gid app --home-dir /app app \
    && mkdir -p /app/uploads/receipts

ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=docker \
    SERVER_PORT=8080 \
    RECEIPT_STORAGE_PATH=/app/uploads/receipts

COPY --from=build /workspace/target/*.jar /app/app.jar
COPY docker/docker-entrypoint.sh /app/docker-entrypoint.sh

RUN chmod +x /app/docker-entrypoint.sh \
    && chown -R app:app /app

USER app

EXPOSE 8080
VOLUME ["/app/uploads/receipts"]

ENTRYPOINT ["/app/docker-entrypoint.sh"]
