package com.tripplanner.events;

import com.tripplanner.model.Weather;

public class WeatherUpdatedEvent {
    private final Weather weather;

    public WeatherUpdatedEvent(Weather weather) {
        this.weather = weather;
    }

    public Weather getWeather() { return weather; }
}
