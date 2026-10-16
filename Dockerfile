# ---- build: Maven + the Vaadin production bundle (the Vaadin plugin downloads Node.js itself) ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -B -q dependency:go-offline || true
COPY src src
RUN ./mvnw -B -q -DskipTests package

# ---- run: a plain JRE ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/ragdemo-1.0-SNAPSHOT.jar app.jar
# Render's free plan has 512 MB: let the heap use 70 % of it, the rest is for threads and metaspace.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"
# Render sets PORT (application.yml reads it); 8080 locally.
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
