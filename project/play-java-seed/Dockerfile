FROM sbtscala/scala-sbt:eclipse-temurin-alpine-21.0.8_9_1.12.11_3.8.3 AS build

WORKDIR /app
COPY . .

RUN sbt clean dist

# unzip dans build stage
RUN unzip target/universal/*.zip -d target/universal/app

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/universal/app/* .

EXPOSE 9000

CMD ["bin/play-java-seed"]