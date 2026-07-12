FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src src

RUN ./mvnw --batch-mode --no-transfer-progress clean verify

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/ai-gamified-career-platform-0.0.1-SNAPSHOT.jar app.jar

RUN groupadd --gid 10001 app \
    && useradd --uid 10001 --gid app --home-dir /app --no-create-home --shell /usr/sbin/nologin app \
    && chown app:app /app/app.jar

USER app

EXPOSE 8080

CMD ["sh", "-c", "exec java ${JAVA_OPTS:-} -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
