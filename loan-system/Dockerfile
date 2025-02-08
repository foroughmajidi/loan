FROM openjdk:21-jdk-slim
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

RUN ls -l /app/target

COPY target/loan-system-0.0.1-SNAPSHOT.jar /app/loan-system.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "loan-system.jar"]
