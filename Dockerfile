FROM folioci/alpine-jre-openjdk21:latest

# Install latest patch versions of packages: https://pythonspeed.com/articles/security-updates-in-docker/
USER root
RUN apk upgrade --no-cache
USER folio

ENV VERTICLE_FILE=mod-camunda.jar

# Set the location of the verticles.
ENV VERTICLE_HOME=/usr/verticles

# Copy your jar to the container.
COPY target/mod-camunda*.jar ${VERTICLE_HOME}/${VERTICLE_FILE}

# Expose this port locally in the container.
EXPOSE 8081
