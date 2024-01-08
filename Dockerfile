FROM maven:3.6.3-openjdk-17 AS build
WORKDIR /home/app
COPY . .
RUN mvn clean install

FROM openjdk:17
WORKDIR /usr/app

COPY --from=build /home/app/warehouse-rest-starter/target/ /usr/app/
COPY --from=build /home/app/new-post-proxy-service/target/ /usr/app/
COPY --from=build /home/app/email-notification-service/target/ /usr/app/
COPY --from=build /home/app/warehouse-csv-sdk/target/ /usr/app/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/usr/app/new-post-proxy-service-1.0-SNAPSHOT.jar"]