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

        // Sprawdź różne możliwe lokalizacje pliku config
        String[] configPaths = {
                "config.properties",
                "./config.properties",
                "src/main/resources/config.properties",
                System.getProperty("user.dir") + "/config.properties"
        };

        boolean configLoaded = false;

        for (String path : configPaths) {
            File configFile = new File(path);
            System.out.println("Checking config file at: " + configFile.getAbsolutePath());

            if (configFile.exists() && configFile.canRead()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    config.load(fis);
                    System.out.println("Config loaded successfully from: " + configFile.getAbsolutePath());
                    configLoaded = true;
                    break;
                } catch (IOException e) {
                    System.err.println("Failed to load config from " + path + ": " + e.getMessage());
                }
            }
        }

        if (!configLoaded) {
            System.out.println("No config file found, using defaults");
            config = new Properties();

            // Sprawdź zmienne środowiskowe
            String weatherApiKey = System.getenv("WEATHER_API_KEY");
            String googleApiKey = System.getenv("GOOGLE_API_KEY");

            if (weatherApiKey != null) {
                config.setProperty("weather.api.key", weatherApiKey);
                System.out.println("Weather API key loaded from environment variable");
            }

            if (googleApiKey != null) {
                config.setProperty("google.api.key", googleApiKey);
                System.out.println("Google API key loaded from environment variable");
            }
        }

        // Debug: pokaż jakie klucze są dostępne (bez ujawniania wartości)
        System.out.println("Available config keys:");
        for (String key : config.stringPropertyNames()) {
            String value = config.getProperty(key);
            if (key.toLowerCase().contains("key") || key.toLowerCase().contains("password")) {
                System.out.println("  " + key + " = " + (value != null && !value.isEmpty() ? "[SET]" : "[NOT SET]"));
            } else {
                System.out.println("  " + key + " = " + value);
            }
        }
    }

    public String getGoogleApiKey() {
        String key = config.getProperty("google.api.key", "GOOGLE_API_KEY");
        if ("YOUR_GOOGLE_API_KEY".equals(key)) {
            System.err.println("Warning: Google API key is not configured!");
        }
        return key;
    }

    public String getWeatherApiKey() {
        String key = config.getProperty("weather.api.key", "WEATHER_API_KEY");
        if ("WEATHER_API_KEY".equals(key)) {
            System.err.println("Warning: Weather API key is not configured!");
        }
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