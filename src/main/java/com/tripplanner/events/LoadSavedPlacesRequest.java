package com.tripplanner.events;

public class LoadSavedPlacesRequest {
    private final String filterType;

    public LoadSavedPlacesRequest() {
        this.filterType = null; // wszystkie
    }

    public LoadSavedPlacesRequest(String filterType) {
        this.filterType = filterType;
    }

    public String getFilterType() {
        return filterType;
    }
}