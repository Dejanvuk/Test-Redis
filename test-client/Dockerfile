FROM openjdk:11

ENV REDIS_PORT='6379'
ENV REDIS_HOST='redisserver'

ADD ./target/test-client-1.0.0-SNAPSHOT-shaded.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]