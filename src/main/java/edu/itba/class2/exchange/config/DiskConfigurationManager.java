package edu.itba.class2.exchange.config;

import edu.itba.class2.exchange.exception.CouldNotLoadPropertiesException;
import edu.itba.class2.exchange.interfaces.ConfigurationManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
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
        try (InputStream input = openResource(configFileName)) {
            props.load(input);
        } catch (IOException e) {
            throw new CouldNotLoadPropertiesException("Error loading " + configFileName, e);
        }
        return props;
    }

    private InputStream openResource(String name) throws IOException {
        return Optional.ofNullable(
                getClass().getClassLoader().getResourceAsStream(name)
        ).orElseThrow(() -> new FileNotFoundException("Unable to find " + name));
    }

    @Override
    public String getProperty(final String key) {
        return properties.getProperty(key);
    }
}
