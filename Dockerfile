FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/authentication.jar app.jar

EXPOSE 8015

ENTRYPOINT ["java", "-jar", "app.jar"]