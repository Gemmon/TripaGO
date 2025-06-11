package com.tripplanner.gui;

import com.tripplanner.components.SavedPlaceCard;
import com.tripplanner.database.DatabaseManager;
import com.tripplanner.model.Place;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SavedPlacesDialog extends JDialog {
    private JPanel placesPanel;
    private JComboBox<String> filterComboBox;
    private JLabel countLabel;
    private List<Place> allPlaces;

    public SavedPlacesDialog(Frame parent) {
        super(parent, "Zapisane miejsca", true);
        initializeComponents();
        setupLayout();
        loadSavedPlaces();

        setSize(600, 500);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        placesPanel = new JPanel();
        placesPanel.setLayout(new BoxLayout(placesPanel, BoxLayout.Y_AXIS));
        placesPanel.setBackground(Color.WHITE);

        // Filtr typu
        filterComboBox = new JComboBox<>(new String[]{
                "Wszystkie", "restaurant", "tourist_attraction", "lodging", "museum", "park"
        });
        filterComboBox.addActionListener(this::filterPlaces);

        countLabel = new JLabel("Ładowanie...");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panel górny z filtrem
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Filtruj według typu:"));
        topPanel.add(filterComboBox);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(countLabel);

        JButton refreshButton = new JButton("Odśwież");
        refreshButton.addActionListener(e -> loadSavedPlaces());
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // Przewijany panel z miejscami
        JScrollPane scrollPane = new JScrollPane(placesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Panel dolny z przyciskami
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Zamknij");
        closeButton.addActionListener(e -> dispose());

        JButton exportButton = new JButton("Eksportuj do pliku");
        exportButton.addActionListener(this::exportPlaces);

        bottomPanel.add(exportButton);
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadSavedPlaces() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("ŁADOWANIE ZAPISANYCH MIEJSC");
            filterComboBox.setSelectedItem("Wszystkie");
            try {
                allPlaces = DatabaseManager.getInstance().getAllPlaces();
                System.out.println("Załadowano " + allPlaces.size() + " zapisanych miejsc");


                displayPlaces(allPlaces);
                updateCountLabel(allPlaces.size());

                if (allPlaces.isEmpty()) {
                    showNoPlacesMessage();
                }

            } catch (Exception e) {
                System.err.println("Błąd podczas ładowania zapisanych miejsc: " + e.getMessage());
                e.printStackTrace();

                JOptionPane.showMessageDialog(this,
                        "Błąd podczas ładowania zapisanych miejsc:\n" + e.getMessage(),
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);
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
        if (allPlaces == null) return;

        String selectedType = (String) filterComboBox.getSelectedItem();
        List<Place> filteredPlaces;

        if ("Wszystkie".equals(selectedType)) {
            filteredPlaces = allPlaces;
        } else {
            List<Place> list = new ArrayList<>();
            for (Place place : allPlaces) {
                if (selectedType.equals(place.getType())) {
                    list.add(place);
                }
            }
            filteredPlaces = list;
        }

        System.out.println("Filtrowanie według typu: " + selectedType + ", znaleziono: " + filteredPlaces.size());
        displayPlaces(filteredPlaces);
        updateCountLabel(filteredPlaces.size());
    }

    private void deletePlace(ActionEvent e) {
        try {
            Long placeId = Long.parseLong(e.getActionCommand());
            System.out.println("Usuwanie miejsca o ID: " + placeId);

            boolean deleted = DatabaseManager.getInstance().deletePlace(placeId);

            if (deleted) {
                loadSavedPlaces(); // Odśwież listę
            } else {
                JOptionPane.showMessageDialog(this, "Nie udało się usunąć miejsca", "Błąd", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            System.err.println("Błąd podczas usuwania miejsca: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Błąd podczas usuwania miejsca", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPlaces(ActionEvent e) {
        if (allPlaces == null || allPlaces.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Brak miejsc do eksportu", "Informacja", JOptionPane.INFORMATION_MESSAGE);
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
                    writer.println("Liczba miejsc: " + allPlaces.size());
                    writer.println();

                    for (Place place : allPlaces) {
                        writer.println("Nazwa: " + place.getName());
                        writer.println("Adres: " + place.getAddress());
                        writer.println("Ocena: " + place.getRating());
                        writer.println("Typ: " + place.getType());
                    }
                }

                JOptionPane.showMessageDialog(this, "Miejsca zostały wyeksportowane do pliku", "Sukces", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                System.err.println("Błąd podczas eksportu: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Błąd podczas eksportu do pliku", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateCountLabel(int count) {
        countLabel.setText("Liczba miejsc: " + count);
    }
}