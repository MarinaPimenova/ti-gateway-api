FROM alpine/java:21-jre

COPY target/ti-gateway-0.0.1-SNAPSHOT.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]