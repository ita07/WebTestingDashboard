package com.ita07.webTestingDashboard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Properties;

@Service
public class EnvConfigService {

    private static final Logger logger = LoggerFactory.getLogger(EnvConfigService.class);
    private static final String FILE_PATH = "src/main/resources/env.properties";

    private final Properties props = new Properties();

    public EnvConfigService() {
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = new FileInputStream(FILE_PATH)) {
            props.load(input);
            logger.info("Loaded properties from env.properties");
        } catch (IOException e) {
            logger.warn("Could not load env.properties: {}", e.getMessage());
        }
    }

    public synchronized String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public synchronized void setProperty(String key, String value) {
        props.setProperty(key, value);
        try (OutputStream output = new FileOutputStream(FILE_PATH)) {
            props.store(output, null);
            logger.info("Updated {} to {} in env.properties", key, value);
        } catch (IOException e) {
            logger.error("Failed to write to env.properties: {}", e.getMessage());
        }
    }
}