FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENV PORT 8080
EXPOSE 8080

ENTRYPOINT ["java", "-Xmx384m", "-Xss512k", "-XX:+UseSerialGC", "-jar", "app.jar"]