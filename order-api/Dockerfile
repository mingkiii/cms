FROM openjdk:11-jre-slim
COPY build/libs/order-api.jar order-api.jar
ENTRYPOINT ["java", "-DSprlsingD.profiles.active-prod", "-Dmailgun.key=${MAILGUN_KEY}", "-jar", "user-api.jar"]