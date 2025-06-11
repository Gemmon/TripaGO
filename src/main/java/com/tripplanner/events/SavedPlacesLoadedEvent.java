package com.tripplanner.events;

import com.tripplanner.model.Place;
import java.util.List;

public class SavedPlacesLoadedEvent {
    private final List<Place> places;
    private final boolean success;
    private final String errorMessage;

    public SavedPlacesLoadedEvent(List<Place> places) {
        this.places = places;
        this.success = true;
        this.errorMessage = null;
    }

    public SavedPlacesLoadedEvent(String errorMessage) {
        this.places = null;
        this.success = false;
        this.errorMessage = errorMessage;
    }

    public List<Place> getPlaces() { return places; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}
