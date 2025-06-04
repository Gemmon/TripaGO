package com.tripplanner.components;

import com.tripplanner.model.Place;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SavedPlaceCard extends JPanel {
    private Place place;
    private ActionListener deleteListener;

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

        // Nazwa miejsca
        JLabel nameLabel = new JLabel(place.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(Color.BLACK);
        infoPanel.add(nameLabel);

        // Adres
        JLabel addressLabel = new JLabel(place.getAddress());
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        addressLabel.setForeground(Color.GRAY);
        infoPanel.add(addressLabel);

        // Ocena i typ
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        detailsPanel.setBackground(Color.WHITE);

        JLabel ratingLabel = new JLabel("★ " + String.format("%.1f", place.getRating()));
        ratingLabel.setFont(new Font("Arial", Font.BOLD, 12));
        ratingLabel.setForeground(Color.ORANGE);
        detailsPanel.add(ratingLabel);

        JLabel typeLabel = new JLabel(" | " + place.getType());
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        typeLabel.setForeground(Color.BLUE);
        detailsPanel.add(typeLabel);

        infoPanel.add(detailsPanel);

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
            // Tu można dodać funkcjonalność pokazania na mapie
            JOptionPane.showMessageDialog(this,
                    "Współrzędne:\nSzerokość: " + place.getLatitude() +
                            "\nDługość: " + place.getLongitude());
        });

        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(showOnMapButton);

        add(buttonPanel, BorderLayout.EAST);
    }

    public Place getPlace() {
        return place;
    }
}
