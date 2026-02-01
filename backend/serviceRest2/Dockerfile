FROM openjdk:17-jdk-alpine
VOLUME /tmp
ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS
ENV DATABASE_URL=jdbc:mysql://mysql:3306/133-Client2-Maoo

COPY target/ex6-0.0.1-SNAPSHOT.jar servicerest2.jar
EXPOSE 8082
ENTRYPOINT exec java $JAVA_OPTS -jar servicerest2.jar
# For Spring-Boot project, use the entrypoint below to reduce Tomcat startup time.
#ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar servicerest2.jar
