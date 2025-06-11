package com.tripplanner.services;

import com.tripplanner.config.ConfigManager;
import com.tripplanner.model.Weather;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class WeatherService {
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";

    public CompletableFuture<Weather> getWeather(String location) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String apiKey = ConfigManager.getInstance().getWeatherApiKey();

                String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
                String urlStr = WEATHER_API_URL + "?q=" + encodedLocation + "&appid=" + apiKey + "&units=metric&lang=pl";

                System.out.println("Weather API URL: " + urlStr.replace(apiKey, "***"));

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "TripPlanner/1.0");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                System.out.println("Weather API Response Code: " + responseCode);

                if (responseCode == 200) {
                    String response = IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);
                    return parseWeatherResponse(response, location);
                } else {
                    String errorResponse = "";
                    try {
                        errorResponse = IOUtils.toString(conn.getErrorStream(), StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        errorResponse = "Could not read error response";
                    }
                    System.err.println("Weather API Error " + responseCode + ": " + errorResponse);
                    return null;
                }

            } catch (Exception e) {
                System.err.println("Exception in WeatherService: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    private Weather parseWeatherResponse(String jsonResponse, String location) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject main = json.getJSONObject("main");
            JSONObject weatherObj = json.getJSONArray("weather").getJSONObject(0);
            JSONObject wind = json.optJSONObject("wind");

            double temperature = main.getDouble("temp");
            String description = weatherObj.getString("description");
            int humidity = main.getInt("humidity");
            double windSpeed = wind != null ? wind.optDouble("speed", 0.0) : 0.0;

            return new Weather(location, temperature, description, humidity, windSpeed);
        } catch (Exception e) {
            System.err.println("Error parsing weather response: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}