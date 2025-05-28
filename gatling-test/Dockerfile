FROM gradle:latest AS build
ENV HOME_DIR=/gatling
WORKDIR $HOME_DIR
COPY src/ $HOME_DIR/src
COPY buildSrc/ $HOME_DIR/buildSrc
COPY gradle/ $HOME_DIR/gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties gradlew $HOME_DIR/
RUN gradle build -x test