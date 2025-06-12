package com.tripplanner.services;

import com.tripplanner.config.ConfigManager;
import com.tripplanner.model.Place;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GooglePlacesService {
    private static final String PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";

    public CompletableFuture<List<Place>> searchPlaces(String location, String type) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String apiKey = ConfigManager.getInstance().getGoogleApiKey();

                String query = URLEncoder.encode(type + " in " + location, StandardCharsets.UTF_8);
                String urlStr = PLACES_API_URL + "?query=" + query + "&key=" + apiKey;

                System.out.println("Google Places API URL: " + urlStr.replace(apiKey, "***"));

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "TripPlanner/1.0");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                System.out.println("Google Places API Response Code: " + responseCode);

                if (responseCode == 200) {
                    String response = IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);
                    System.out.println("Google Places API Response length: " + response.length());

                    List<Place> places = parsePlacesResponse(response, location);
                    System.out.println("Found " + places.size() + " places");
                    return places;
                }

            } catch (Exception e) {
                System.err.println("Exception in GooglePlacesService: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
            return null;
        });
    }

    private List<Place> parsePlacesResponse(String jsonResponse, String query) {
        List<Place> places = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(jsonResponse);

            // status odpowiedzi
            String status = json.optString("status", "UNKNOWN");
            System.out.println("Google Places API Status: " + status);

            if (!"OK".equals(status)) {
                System.err.println("Google Places API returned status: " + status);
                if (json.has("error_message")) {
                    System.err.println("Error message: " + json.getString("error_message"));
                }
                return places;
            }

            if (!json.has("results")) {
                System.err.println("No 'results' field in response");
                return places;
            }

            JSONArray results = json.getJSONArray("results");
            System.out.println("Processing " + results.length() + " results from Google Places API");

            for (int i = 0; i < results.length(); i++) {
                try {

                    JSONObject placeJson = results.getJSONObject(i);
                    System.out.println(results.getJSONObject(i));
                    String city = capitalizeFirstLetter(query);
                    String name = placeJson.optString("name", "Unknown Place");
                    String address = placeJson.optString("formatted_address", "");
                    String cleanedAddress = removePostalCodeFromAddress(address);


                    System.out.println("Miasto: " + city);

                    double rating = placeJson.optDouble("rating", 0.0);

                    // współrzędne
                    double lat = 0.0, lng = 0.0;
                    if (placeJson.has("geometry") && placeJson.getJSONObject("geometry").has("location")) {
                        JSONObject location = placeJson.getJSONObject("geometry").getJSONObject("location");
                        lat = location.optDouble("lat", 0.0);
                        lng = location.optDouble("lng", 0.0);
                    }

                    // typ
                    String placeType = "";
                    if (placeJson.has("types")) {
                        JSONArray types = placeJson.getJSONArray("types");
                        if (types.length() > 0) {
                            placeType = types.getString(0);
                        }
                    }

                    Place place = new Place(name, city, cleanedAddress, rating, lat, lng, placeType);
                    places.add(place);

                    System.out.println("Added place: " + name + " (" + rating + "★)");

                } catch (Exception e) {
                    System.err.println("Error parsing place at index " + i + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing Google Places response: " + e.getMessage());
            e.printStackTrace();
        }
        return places;
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private String removePostalCodeFromAddress(String address) {

        return address.replaceAll("\\b\\d{2,3}-\\d{3}\\b", "").trim().replaceAll(" ,", ",");
    }


}