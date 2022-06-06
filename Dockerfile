FROM openjdk:17-alpine

RUN ["adduser", "--disabled-password", "technobot"]
USER technobot
WORKDIR /app

COPY ./target/TechnoBot-1.0.jar .

CMD ["java", "-jar", "/app/TechnoBot-1.0.jar"]
