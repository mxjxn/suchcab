FROM openjdk:8-alpine

COPY target/uberjar/suchcab.jar /suchcab/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/suchcab/app.jar"]
