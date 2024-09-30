FROM openjdk:21-jdk

WORKDIR /app

COPY target/email-service-0.0.1-SNAPSHOT.jar /app/email-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/email-service.jar"]
