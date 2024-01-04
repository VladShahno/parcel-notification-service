FROM maven:3.6.3-openjdk-17 AS build
WORKDIR /home/app
COPY . .
RUN mvn clean install

FROM openjdk:17
COPY --from=build /home/app/target/*.jar /usr/app/parcel-notification-service.jar
EXPOSE 8080
CMD java -jar -DskipTests /usr/app/parcel-notification-service.jar
