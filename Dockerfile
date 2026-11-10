# ---- Build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q package -DskipTests

# ---- Run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 parkshare
COPY --from=build /app/target/*.jar app.jar
USER parkshare
ENV TZ=Africa/Kigali
# Tuned for small hosts (e.g. Render free: 512 MB RAM, a fraction of a CPU):
# serial GC and quick JIT keep memory low and startup fast.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
EXPOSE 8080
# Hosts like Render pass the port to listen on in $PORT (Render's default is 10000).
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar app.jar"]
