package com.tripplanner.events;

import com.tripplanner.model.Place;
import java.util.List;

public class PlacesFoundEvent {
    private final List<Place> places;

    public PlacesFoundEvent(List<Place> places) {
        this.places = places;
    }

    public List<Place> getPlaces() { return places; }
}