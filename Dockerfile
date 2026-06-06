# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd -r blog && useradd -r -g blog blog

COPY blog-web/target/blog-web-*.jar /app/app.jar

RUN mkdir -p /app/logs/csp \
    && chown -R blog:blog /app
USER blog

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -Dcsp.sentinel.log.dir=/app/logs/csp"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
