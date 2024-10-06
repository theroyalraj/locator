FROM azul/zulu-openjdk:21-latest
VOLUME /tmp
COPY build/libs/*.jar app.jar
EXPOSE 80
ENTRYPOINT ["java","-jar","/app.jar"]