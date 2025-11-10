# Base image with Java 21
FROM eclipse-temurin:21-jdk-jammy

# Install necessary tools: curl, unzip
RUN apt-get update && apt-get install -y curl unzip && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy source code
COPY . .

# Install sbt (Scala Build Tool)
RUN curl -L -o sbt.zip https://github.com/sbt/sbt/releases/download/v1.9.9/sbt-1.9.9.zip && \
    unzip sbt.zip && \
    mv sbt*/ /usr/local/sbt && \
    ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt && \
    rm sbt.zip

# Stage app (compile + prepare for prod run)
RUN sbt stage

# Expose default Play port
EXPOSE 9000

# Run the application (assuming app name is 'myapp')
CMD ["./target/universal/stage/bin/trello-service"]
