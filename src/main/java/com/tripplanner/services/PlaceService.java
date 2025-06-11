package com.tripplanner.services;


import com.tripplanner.database.DatabaseManager;
import com.tripplanner.events.*;
import com.tripplanner.model.Place;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlaceService {
    private static PlaceService instance;

    private PlaceService() {
        EventBus.getInstance().register(this);
    }

    public static synchronized PlaceService getInstance() {
        if (instance == null) {
            instance = new PlaceService();
        }
        return instance;
    }

    @Subscribe
    public void handleLoadRequest(LoadSavedPlacesRequest request) {
        try {
            List<Place> allPlaces = DatabaseManager.getInstance().getAllPlaces();

            // filtrowanie jeśli potrzebne
            List<Place> filteredPlaces = allPlaces;
            if (request.getFilterType() != null && !"Wszystkie".equals(request.getFilterType())) {
                filteredPlaces = allPlaces.stream()
                        .filter(place -> request.getFilterType().equals(place.getType()))
                        .collect(Collectors.toList());
            }

            EventBus.getInstance().post(new SavedPlacesLoadedEvent(filteredPlaces));

        } catch (Exception e) {
            EventBus.getInstance().post(new SavedPlacesLoadedEvent(e.getMessage()));
        }
    }

    @Subscribe
    public void handleDeletePlaceRequest(DeletePlaceRequest request) {
        CompletableFuture.supplyAsync(() -> {
            try {
                DatabaseManager.getInstance().deletePlace(request.getPlaceId());
                System.out.println("Usunięto miejsce o ID: " + request.getPlaceId());
                return new PlaceDeletedEvent(request.getPlaceId(), true, null);
            } catch (Exception e) {
                System.err.println("Błąd podczas usuwania miejsca: " + e.getMessage());
                e.printStackTrace();
                return new PlaceDeletedEvent(request.getPlaceId(), false, e.getMessage());
            }
        }).thenAccept(event -> {
            SwingUtilities.invokeLater(() -> {
                EventBus.getInstance().post(event);

                // po usunięciu przeładowanie listy
                if (event.isSuccess()) {
                    EventBus.getInstance().post(new LoadSavedPlacesRequest());
                }
            });
        });
    }

}