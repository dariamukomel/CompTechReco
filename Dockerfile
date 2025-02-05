FROM openjdk:17.0.1-jdk-slim
COPY ./target/comptechrich-1.1-SNAPSHOT-jar-with-dependencies.jar /comptechrich-1.1-SNAPSHOT-jar-with-dependencies.jar
CMD ["java", "-jar", "comptechrich-1.1-SNAPSHOT-jar-with-dependencies.jar"]
