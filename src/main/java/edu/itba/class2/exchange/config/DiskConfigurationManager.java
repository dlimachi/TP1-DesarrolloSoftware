package edu.itba.class2.exchange.config;

import edu.itba.class2.exchange.exception.CouldNotLoadPropertiesException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DiskConfigurationManager implements ConfigurationManager {
    private final String configFileName;
    private final Properties properties;

    public DiskConfigurationManager(final String configFileName) {
        this.configFileName = configFileName;
        this.properties = loadProperties();
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (input == null) {
                throw new FileNotFoundException("Unable to find " + configFileName);
            }
            props.load(input);
        } catch (IOException e) {
            throw new CouldNotLoadPropertiesException("Error loading " + configFileName, e);
        }
        return props;
    }

    @Override
    public String getProperty(final String key) {
        return properties.getProperty(key);
    }
}
