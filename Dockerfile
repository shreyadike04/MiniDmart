# Build stage: compile the servlet/DAO/filter/model classes.
# javax.servlet-api is provided by Tomcat at runtime, not bundled in the repo,
# so it's fetched here purely as a compile-time dependency.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY src/main/java ./src/main/java
COPY src/main/webapp/WEB-INF/lib ./lib

RUN curl -sL -o /tmp/servlet-api.jar \
      https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar \
    && find src/main/java -name "*.java" > /tmp/sources.txt \
    && mkdir -p /build/classes \
    && javac -encoding UTF-8 -nowarn \
         -cp "/tmp/servlet-api.jar:lib/*" \
         -d /build/classes @/tmp/sources.txt

# Runtime stage: deploy as ROOT so the public URL has no context path.
FROM tomcat:9.0-jre21-temurin
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY src/main/webapp/ /usr/local/tomcat/webapps/ROOT/
COPY --from=build /build/classes/ /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/

# DB credentials come from environment variables at runtime (see DBUtil.java),
# not from a properties file — nothing secret is baked into the image.
EXPOSE 8080
CMD ["catalina.sh", "run"]
