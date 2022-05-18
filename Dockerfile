FROM openjdk:11-jre
VOLUME /tmp
EXPOSE 8081
ADD target/cloud_storage-0.0.1-SNAPSHOT.jar springbootapp.jar
ENTRYPOINT ["java", "-jar", "/springbootapp.jar"]