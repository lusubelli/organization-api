FROM openjdk:8-jdk-alpine

COPY target/organization-api-1.0-SNAPSHOT.jar organization-api-1.0-SNAPSHOT.jar
COPY target/libs/ libs/
COPY config/ config/
COPY ssl/ ssl/

EXPOSE 8686

# , "--ssl-keystore", "ssl/localhost.keystore", "--ssl-password", "password"
ENTRYPOINT ["java", "-DLogback.configurationFile=config/logback.xml", "-jar", "organization-api-1.0-SNAPSHOT.jar", "--auth-htpasswd-path", "config/.htpasswd", "--config", "config/application.conf", "-cp", "libs/*" ]
