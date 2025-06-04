package com.tripplanner.events;

public class TripSearchEvent {
    private final String location;
    private final String type;

    public TripSearchEvent(String location, String type) {
        this.location = location;
        this.type = type;
    }

    public String getLocation() { return location; }
    public String getType() { return type; }
}