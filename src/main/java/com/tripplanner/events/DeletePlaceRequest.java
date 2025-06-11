package com.tripplanner.events;

public class DeletePlaceRequest {
    private final Long placeId;

    public DeletePlaceRequest(Long placeId) {
        this.placeId = placeId;
    }

    public Long getPlaceId() { return placeId; }
}