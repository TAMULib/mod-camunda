# build base image
FROM maven:3-openjdk-11-slim as maven

# copy pom.xml
COPY ./pom.xml ./pom.xml

# copy src
COPY ./src ./src

# build
RUN mvn package

# final base image
FROM openjdk:11-jre-slim

# Upgrade to latest patch versions of packages: https://pythonspeed.com/articles/security-updates-in-docker/
RUN apt-get update \
 && apt-get upgrade -y \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

# set deployment directory
WORKDIR /mod-camunda

# copy over the built artifact from the maven image
COPY --from=maven /target/mod-camunda*.jar ./mod-camunda.jar

# environment
ENV SERVER_PORT='9000'

# expose port
EXPOSE ${SERVER_PORT}

# run java command
CMD java -jar -Xmx4096m ./mod-camunda.jar
