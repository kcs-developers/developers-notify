FROM openjdk:17-jdk
ADD /build/libs/notify-0.0.1-SNAPSHOT.jar springbootApp.jar
EXPOSE 9003
ENTRYPOINT ["java", "-jar", "springbootApp.jar"]
