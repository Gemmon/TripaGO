package com.tripplanner.components;

import com.tripplanner.model.Place;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import com.tripplanner.model.Weather;
import com.tripplanner.services.WeatherService;


public class SavedPlaceCard extends JPanel {
    private Place place;
    private ActionListener deleteListener;
    private JLabel weatherLabel;
    private static final WeatherService weatherService = new WeatherService();


    public SavedPlaceCard(Place place, ActionListener deleteListener) {
        this.place = place;
        this.deleteListener = deleteListener;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 100));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(Color.WHITE);

        // Panel główny z informacjami
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        weatherLabel = new JLabel("Pogoda: ładowanie...");
        weatherLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        weatherLabel.setForeground(Color.DARK_GRAY);
        infoPanel.add(weatherLabel);

        loadWeather();


        // Nazwa miejsca
        JLabel nameLabel = new JLabel(place.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(Color.BLACK);
        infoPanel.add(nameLabel);

        JLabel cityLabel = new JLabel(place.getCity());
        cityLabel.setFont(new Font("Arial", Font.BOLD, 16));
        cityLabel.setForeground(Color.BLACK);
        infoPanel.add(cityLabel);

        // Adres
        JLabel addressLabel = new JLabel(place.getAddress());
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        addressLabel.setForeground(Color.GRAY);
        infoPanel.add(addressLabel);


        JLabel ratingLabel = new JLabel("Ocena: " + String.format("%.1f", place.getRating()));
        ratingLabel.setFont(new Font("Arial", Font.BOLD, 12));
        ratingLabel.setForeground(Color.ORANGE);
        infoPanel.add(ratingLabel);

        JLabel typeLabel = new JLabel(" | " + place.getType());
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        typeLabel.setForeground(Color.BLUE);
        infoPanel.add(typeLabel);

        add(infoPanel, BorderLayout.CENTER);

        // Panel z przyciskami
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.WHITE);

        JButton deleteButton = new JButton("Usuń");
        deleteButton.setFont(new Font("Arial", Font.PLAIN, 10));
        deleteButton.setPreferredSize(new Dimension(60, 25));
        deleteButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Czy na pewno chcesz usunąć miejsce: " + place.getName() + "?",
                    "Potwierdzenie usunięcia",
                    JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION && deleteListener != null) {
                deleteListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, place.getId().toString()));
            }
        });

        JButton showOnMapButton = new JButton("Mapa");
        showOnMapButton.setFont(new Font("Arial", Font.PLAIN, 10));
        showOnMapButton.setPreferredSize(new Dimension(60, 25));
        showOnMapButton.addActionListener(e -> {
            String url = String.format("https://www.google.com/maps/search/?api=1&query=%f,%f",
                    place.getLatitude(), place.getLongitude());
            try {
                Desktop.getDesktop().browse(new URL(url).toURI());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Nie udało się otworzyć Google Maps:\n" + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(showOnMapButton);

        add(buttonPanel, BorderLayout.EAST);
    }

    private void loadWeather() {
        weatherService.getWeather(place.getCity()).thenAccept(weather -> {
            if (weather != null) {
                SwingUtilities.invokeLater(() -> {
                    weatherLabel.setText(String.format("Pogoda: %.1f°C, %s", weather.getTemperature(), weather.getDescription()));
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    weatherLabel.setText("Pogoda: brak danych");
                });
            }
        });
    }


    public Place getPlace() {
        return place;
    }
}