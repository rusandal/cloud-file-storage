FROM openjdk:11-jre
VOLUME /tmp
EXPOSE 8081
COPY target/cloud_storage-0.0.1-SNAPSHOT.jar springbootapp.jar
CMD ["java", "-jar", "/springbootapp.jar"]