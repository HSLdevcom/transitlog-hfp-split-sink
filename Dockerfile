# syntax=docker/dockerfile:1
# check=error=true

FROM hsldevcom/infodevops-docker-base-images:1.0.2-25-java-jdk AS build

WORKDIR /usr/app
ARG GITHUB_ACTOR=github-actions

COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY .mvn/settings.xml /root/.m2/settings.xml
COPY eclipse-java-formatter.xml ./
COPY src src

RUN --mount=type=secret,id=github_token \
    --mount=type=secret,id=github_actor \
    export GITHUB_TOKEN="$(cat /run/secrets/github_token)" && \
    export GITHUB_ACTOR="$(cat /run/secrets/github_actor)" && \
    ./mvnw -B package -DskipTests

FROM hsldevcom/infodevops-docker-base-images:1.0.2-25-java-jre

WORKDIR /usr/app
COPY --from=build /usr/app/target/transitlog-hfp-split-sink.jar /usr/app/transitlog-hfp-split-sink.jar
COPY run /run
COPY --chmod=755 start-application.sh /start-application.sh

ENTRYPOINT ["/start-application.sh"]
