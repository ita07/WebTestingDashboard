# Multi-stage build: First stage for building the application
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory in container
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Second stage: Runtime image
FROM eclipse-temurin:17-jdk-jammy

# Set working directory in container
WORKDIR /app

# Install dependencies and browsers for Selenium tests
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    unzip \
    curl \
    software-properties-common \
    xvfb \
    && rm -rf /var/lib/apt/lists/*

# Install Chrome
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable

# Install Firefox from Mozilla's official repository
RUN apt-get update \
    && apt-get install -y wget gpg \
    && wget -q -O - https://packages.mozilla.org/apt/repo-signing-key.gpg | gpg --dearmor -o /etc/apt/keyrings/packages.mozilla.org.gpg \
    && echo "deb [signed-by=/etc/apt/keyrings/packages.mozilla.org.gpg] https://packages.mozilla.org/apt mozilla main" | tee -a /etc/apt/sources.list.d/mozilla.list > /dev/null \
    && echo 'Package: *\nPin: origin packages.mozilla.org\nPin-Priority: 1000' | tee /etc/apt/preferences.d/mozilla \
    && apt-get update \
    && apt-get install -y firefox \
    && which firefox \
    && firefox --version

# Install GeckoDriver manually for Firefox
RUN GECKODRIVER_VERSION=$(curl -s "https://api.github.com/repos/mozilla/geckodriver/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/' | tr -d '\r\n') \
    && echo "GeckoDriver version: $GECKODRIVER_VERSION" \
    && if [ -n "$GECKODRIVER_VERSION" ] && [ "$GECKODRIVER_VERSION" != "null" ]; then \
         wget -O /tmp/geckodriver.tar.gz "https://github.com/mozilla/geckodriver/releases/download/${GECKODRIVER_VERSION}/geckodriver-${GECKODRIVER_VERSION}-linux64.tar.gz" \
         && tar -C /usr/local/bin -xzf /tmp/geckodriver.tar.gz \
         && chmod +x /usr/local/bin/geckodriver \
         && rm /tmp/geckodriver.tar.gz \
         && echo "GeckoDriver installed successfully"; \
       else \
         echo "Failed to get GeckoDriver version, using fallback version..." \
         && wget -O /tmp/geckodriver.tar.gz "https://github.com/mozilla/geckodriver/releases/download/v0.36.0/geckodriver-v0.36.0-linux64.tar.gz" \
         && tar -C /usr/local/bin -xzf /tmp/geckodriver.tar.gz \
         && chmod +x /usr/local/bin/geckodriver \
         && rm /tmp/geckodriver.tar.gz \
         && echo "GeckoDriver fallback version installed"; \
       fi

# Install Microsoft Edge
RUN curl -fsSL https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor | tee /etc/apt/trusted.gpg.d/microsoft.gpg > /dev/null \
    && echo "deb [arch=amd64,arm64,armhf signed-by=/etc/apt/trusted.gpg.d/microsoft.gpg] https://packages.microsoft.com/repos/edge stable main" | tee /etc/apt/sources.list.d/microsoft-edge.list \
    && apt-get update \
    && apt-get install -y microsoft-edge-stable

# Install EdgeDriver manually for Microsoft Edge
RUN EDGEDRIVER_VERSION=$(curl -s "https://msedgedriver.azureedge.net/LATEST_STABLE" | sed 's/\xEF\xBB\xBF//g' | tr -d '\r\n\000-\037' | grep -o '[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+') \
    && echo "EdgeDriver version: $EDGEDRIVER_VERSION" \
    && if [ -n "$EDGEDRIVER_VERSION" ]; then \
         wget -O /tmp/edgedriver.zip "https://msedgedriver.azureedge.net/${EDGEDRIVER_VERSION}/edgedriver_linux64.zip" \
         && unzip /tmp/edgedriver.zip -d /usr/local/bin/ \
         && chmod +x /usr/local/bin/msedgedriver \
         && rm /tmp/edgedriver.zip; \
       else \
         echo "Failed to get EdgeDriver version, skipping EdgeDriver installation"; \
       fi

# Clean up package cache to reduce image size
RUN rm -rf /var/lib/apt/lists/*

# Copy the built JAR from the build stage
COPY --from=build /app/target/webTestingDashboard-0.0.1-SNAPSHOT.jar app.jar

# Create reports and screenshots directories
RUN mkdir -p /app/reports /app/screenshots

# Expose port
EXPOSE 8080

# Set environment variables for containerized database
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/WebTestingDashboardDB
ENV SPRING_DATASOURCE_USERNAME=admin
ENV SPRING_DATASOURCE_PASSWORD=admin
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=update

# Set environment variables for WebDriver paths
ENV PATH="/usr/local/bin:${PATH}"
ENV DISPLAY=:99

# Run the application
CMD ["java", "-jar", "app.jar"]
