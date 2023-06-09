FROM openjdk:11-jre-slim
COPY build/libs/user-api.jar user-api.jar
ENTRYPOINT ["java", "-DSprlsingD.profiles.active-prod", "-Dmailgun.key=${MAILGUN_KEY}", "-jar", "user-api.jar"]