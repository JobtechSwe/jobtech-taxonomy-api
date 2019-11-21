 
FROM clojure:openjdk-8-lein as builder

COPY . /

RUN pwd

WORKDIR /

RUN lein uberjar

FROM  openjdk:8-alpine

COPY --from=builder target/uberjar/jobtech-taxonomy-api.jar /jobtech-taxonomy-api/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/jobtech-taxonomy-api/app.jar"]