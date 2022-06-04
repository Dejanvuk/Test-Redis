FROM openjdk:11

ENV REDIS_PORT='6379'

EXPOSE 6379

ADD ./target/redis-1.0.0-SNAPSHOT-shaded.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]