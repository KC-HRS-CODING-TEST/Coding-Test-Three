FROM openjdk:8-jre-slim as runtime-environment

WORKDIR /code

COPY docker_entrypoint.sh /usr/bin/docker_entrypoint.sh
RUN chmod 100 /usr/bin/docker_entrypoint.sh

ENTRYPOINT ["docker_entrypoint.sh"]

FROM maven:3-openjdk-8-slim as build-stage

WORKDIR /code

RUN apt-get update

COPY ./project /code

RUN mvn clean compile assembly:single

FROM runtime-environment

COPY --from=build-stage /code/target/test-1.0-jar-with-dependencies.jar /code/test.jar
