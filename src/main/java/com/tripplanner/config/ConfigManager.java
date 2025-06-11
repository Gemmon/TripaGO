package com.tripplanner.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static ConfigManager instance;
    private Properties config;

    private ConfigManager() {}

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void loadConfig() throws IOException {
        config = new Properties();

        String configPaths = "config.properties";

        boolean configLoaded = false;

        File configFile = new File(configPaths);

        if (configFile.exists() && configFile.canRead()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                config.load(fis);
                System.out.println("Config loaded successfully ");
                configLoaded = true;
            } catch (IOException e) {
                System.err.println("Failed to load config");
            }
        }

    }

    public String getGoogleApiKey() {
        String key = config.getProperty("google.api.key", "GOOGLE_API_KEY");
        return key;
    }

    public String getWeatherApiKey() {
        String key = config.getProperty("weather.api.key", "WEATHER_API_KEY");
        return key;
    }

    public String getDatabaseUrl() {
        return config.getProperty("database.url", "jdbc:h2:./data/tripplanner");
    }

    public String getDatabaseUser() {
        return config.getProperty("database.user", "sa");
    }

    public String getDatabasePassword() {
        return config.getProperty("database.password", "");
    }
}