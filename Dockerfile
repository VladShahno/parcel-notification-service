FROM maven:3.6.3-openjdk-17 AS build
WORKDIR /home/app
COPY . .
RUN mvn clean install

FROM openjdk:17
WORKDIR /usr/app

COPY --from=build /home/app/warehouse-rest-starter/target/*.jar /usr/app/warehouse-rest-starter.jar
COPY --from=build /home/app/new-post-proxy-service/target/*.jar /usr/app/new-post-proxy-service.jar
COPY --from=build /home/app/email-notification-service/target/*.jar /usr/app/email-notification-service.jar
COPY --from=build /home/app/warehouse-csv-sdk/target/*.jar /usr/app/warehouse-csv-sdk.jar

COPY --from=build /home/app/new-post-proxy-service/target/*.jar /usr/app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/app/app.jar"]