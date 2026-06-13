# =====================================================================
# Stage 1: Build
# =====================================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies dulu — layer ini cuma rebuild kalau pom.xml berubah
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Build aplikasi (skip test biar image build cepat)
COPY src ./src
RUN mvn -B clean package -DskipTests

# =====================================================================
# Stage 2: Runtime
# =====================================================================
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Jalan sebagai non-root user
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

# Ambil jar hasil build, exclude *.jar.original
COPY --from=build /app/target/tracking-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]