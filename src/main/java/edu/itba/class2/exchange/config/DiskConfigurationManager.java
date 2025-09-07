package edu.itba.class2.exchange.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DiskConfigurationManager implements ConfigurationManager {
    private static final String CONFIG_FILE = "application.properties";
    private final Properties properties;

    public DiskConfigurationManager() {
        this.properties = loadProperties(CONFIG_FILE);
    }

    private Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + fileName);
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading " + fileName, e);
        }
        return props;
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

}