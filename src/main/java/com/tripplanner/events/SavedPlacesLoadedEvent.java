package com.tripplanner.events;

import com.tripplanner.model.Place;
import java.util.List;

public class SavedPlacesLoadedEvent {
    private final List<Place> savedPlaces;

    public SavedPlacesLoadedEvent(List<Place> savedPlaces) {
        this.savedPlaces = savedPlaces;
    }

    public List<Place> getSavedPlaces() {
        return savedPlaces;
    }
}