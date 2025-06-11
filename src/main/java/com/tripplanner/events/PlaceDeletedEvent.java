package com.tripplanner.events;

public class PlaceDeletedEvent {
    private final Long placeId;
    private final boolean success;
    private final String errorMessage;

    public PlaceDeletedEvent(Long placeId, boolean success, String errorMessage) {
        this.placeId = placeId;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public Long getPlaceId() { return placeId; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}