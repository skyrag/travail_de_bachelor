FROM eclipse-temurin:21-jdk-alpine

RUN apk add --no-cache bash curl

WORKDIR /app

COPY project/play-java-seed/ .

RUN sbt update

CMD ["sbt", "test"]