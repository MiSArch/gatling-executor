FROM gradle:jdk21 AS server-build
WORKDIR /app
ADD gatling-server .
ARG module
RUN gradle clean build


FROM gradle:jdk21 AS test-build
# TODO this does not really work yet the build is not preserved
WORKDIR /gatling
COPY gatling-test/src/ ./src
COPY gatling-test/buildSrc/ ./buildSrc
COPY gatling-test/gradle/ ./gradle
COPY gatling-test/build.gradle.kts gatling-test/settings.gradle.kts gatling-test/gradle.properties gatling-test/gradlew ./
RUN ./gradlew clean build

FROM eclipse-temurin:21

WORKDIR /gatling
COPY --from=test-build /gatling ./

ARG module
WORKDIR /home/java
COPY --from=server-build /app/build/libs/*.jar server.jar

EXPOSE 8889

CMD java -jar ./server.jar