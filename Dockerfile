# Use the official OpenJDK image for Java 17 as the base image
FROM eclipse-temurin:17-jdk

# Add Maintainer Info
LABEL maintainer="syedus06@gmail.com"

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ARG JAR_FILE=target/*.jar

# Add the application's jar to the container
ADD ${JAR_FILE} pharmacy-hub.jar

# Run the jar file
ENTRYPOINT ["java","-jar","/pharmacy-hub.jar"]
