FROM sbtscala/scala-sbt:eclipse-temurin-alpine-21.0.8_9_1.12.11_3.8.3

RUN apk add --no-cache bash curl

WORKDIR /app

COPY project/play-java-seed/ .

RUN sbt update

CMD ["sbt", "test"]