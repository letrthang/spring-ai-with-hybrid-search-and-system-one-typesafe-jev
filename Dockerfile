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
COPY --from=build /app/target/ragdemo-*.jar app.jar
# Render's free plan has 512 MB for the whole container. Budget: heap 50 % (~256 MB) + metaspace (~150 MB) + JIT
# code cache (48 MB, C1 only) + direct buffers for Weaviate's gRPC client (48 MB). Without the caps the total can pass
# 512 MB and the container is killed with no Java error. Out of memory → exit at once (logged), Render restarts it.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=50 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1 -XX:ReservedCodeCacheSize=48m -XX:MaxDirectMemorySize=48m -XX:+ExitOnOutOfMemoryError"
# Code-flow logs at INFO on Render (DEBUG locally — see application.yml).
ENV RAGDEMO_LOG_LEVEL=INFO
# Render sets PORT (application.yml reads it); 8080 locally.
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
