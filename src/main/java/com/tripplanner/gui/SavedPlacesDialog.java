package com.tripplanner.gui;

import com.tripplanner.components.SavedPlaceCard;
import com.tripplanner.events.*;
import com.tripplanner.model.Place;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class SavedPlacesDialog extends JDialog {
    private JPanel placesPanel;
    private JComboBox<String> filterComboBox;
    private JLabel countLabel;
    private List<Place> currentPlaces;

    public SavedPlacesDialog(Frame parent) {
        super(parent, "Zapisane miejsca", true);
        initializeComponents();
        setupLayout();

        EventBus.getInstance().register(this);

        // żądanie załadowania danych
        EventBus.getInstance().post(new LoadSavedPlacesRequest());

        setSize(600, 500);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        placesPanel = new JPanel();
        placesPanel.setLayout(new BoxLayout(placesPanel, BoxLayout.Y_AXIS));
        placesPanel.setBackground(Color.WHITE);

        filterComboBox = new JComboBox<>(new String[]{
                "Wszystkie", "restaurant", "tourist_attraction", "lodging",
                "museum", "park", "movie_theater", "night_club", "clothing_store", "gym"
        });
        filterComboBox.addActionListener(this::filterPlaces);

        countLabel = new JLabel("Ładowanie...");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // panel górny z filtrem
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Filtruj według typu:"));
        topPanel.add(filterComboBox);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(countLabel);

        JButton refreshButton = new JButton("Odśwież");
        refreshButton.addActionListener(e -> {
            String currentFilter = (String) filterComboBox.getSelectedItem();
            EventBus.getInstance().post(new LoadSavedPlacesRequest(currentFilter));
        });
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // przewijany panel z miejscami
        JScrollPane scrollPane = new JScrollPane(placesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(23);
        add(scrollPane, BorderLayout.CENTER);

        // panel dolny z przyciskami
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Zamknij");
        closeButton.addActionListener(e -> dispose());

        JButton exportButton = new JButton("Eksportuj do pliku");
        exportButton.addActionListener(this::exportPlaces);

        bottomPanel.add(exportButton);
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Subscribe
    public void onPlacesLoaded(SavedPlacesLoadedEvent event) {
        SwingUtilities.invokeLater(() -> {
            if (event.isSuccess()) {
                this.currentPlaces = event.getPlaces();
                displayPlaces(currentPlaces);
                updateCountLabel(currentPlaces.size());

                if (currentPlaces.isEmpty()) {
                    showNoPlacesMessage();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Błąd podczas ładowania zapisanych miejsc:\n" + event.getErrorMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    @Subscribe
    public void onPlaceDeleted(PlaceDeletedEvent event) {
        SwingUtilities.invokeLater(() -> {
            if (!event.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Nie udało się usunąć miejsca: " + event.getErrorMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void displayPlaces(List<Place> places) {
        placesPanel.removeAll();

        if (places.isEmpty()) {
            showNoPlacesMessage();
        } else {
            for (Place place : places) {
                SavedPlaceCard card = new SavedPlaceCard(place, this::deletePlace);
                placesPanel.add(card);
                placesPanel.add(Box.createVerticalStrut(5));
            }
        }

        placesPanel.revalidate();
        placesPanel.repaint();
    }

    private void showNoPlacesMessage() {
        JLabel noPlacesLabel = new JLabel("Brak zapisanych miejsc", SwingConstants.CENTER);
        noPlacesLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        noPlacesLabel.setForeground(Color.GRAY);
        noPlacesLabel.setPreferredSize(new Dimension(400, 50));
        placesPanel.add(noPlacesLabel);
    }

    private void filterPlaces(ActionEvent e) {
        String selectedType = (String) filterComboBox.getSelectedItem();
        EventBus.getInstance().post(new LoadSavedPlacesRequest(selectedType));
    }

    private void deletePlace(ActionEvent e) {
        try {
            Long placeId = Long.parseLong(e.getActionCommand());
            EventBus.getInstance().post(new DeletePlaceRequest(placeId));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Błąd parsowania ID miejsca", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPlaces(ActionEvent e) {
        if (currentPlaces == null || currentPlaces.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Brak miejsc do eksportu",
                    "Informacja", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("zapisane_miejsca.txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                    writer.println("ZAPISANE MIEJSCA");
                    writer.println("Data eksportu: " + java.time.LocalDateTime.now());
                    writer.println("Liczba miejsc: " + currentPlaces.size());
                    writer.println();

                    for (Place place : currentPlaces) {
                        writer.println("Nazwa: " + place.getName());
                        writer.println("Adres: " + place.getAddress());
                        writer.println("Ocena: " + place.getRating());
                        writer.println("Typ: " + place.getType());
                        writer.println();
                    }
                }

                JOptionPane.showMessageDialog(this,
                        "Miejsca zostały wyeksportowane do pliku", "Sukces",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Błąd podczas eksportu do pliku: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateCountLabel(int count) {
        countLabel.setText("Liczba miejsc: " + count);
    }

    @Override
    public void dispose() {
        EventBus.getInstance().unregister(this);
        super.dispose();
    }
}