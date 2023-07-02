FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /opt/src/

COPY gradle/ ./gradle/
COPY gradlew ./
# just to download the Gradle wrapper
RUN ./gradlew --version

COPY ./ ./
RUN ./gradlew -i --no-daemon --build-cache --stacktrace build

FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/app/
COPY --from=build /opt/src/build/libs/photoupp-1.0.0-SNAPSHOT.jar server.jar
EXPOSE 8080
VOLUME /photo-upp-data
ENTRYPOINT ["java","-jar","server.jar","--storage.dir=/photo-upp-data/"]
