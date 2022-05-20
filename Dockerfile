FROM adoptopenjdk:11-jre-hotspot
RUN mkdir /opt/app
COPY target/cloud_storage-0.0.1-SNAPSHOT.jar /opt/app/springbootstarapp.jar
CMD ["java", "-jar", "/opt/app/springbootstarapp.jar"]
