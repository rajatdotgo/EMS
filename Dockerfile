FROM openjdk:8
EXPOSE 8080
ADD target/demoemp.jar demoemp.jar
ENTRYPOINT [ "java","-jar","/demoemp.jar" ]