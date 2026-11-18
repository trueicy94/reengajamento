# build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew
COPY src src
RUN ./gradlew --no-daemon bootJar -x test

# execução
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV SPRING_DATASOURCE_URL=jdbc:h2:file:/data/notificacoes
VOLUME /data
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
