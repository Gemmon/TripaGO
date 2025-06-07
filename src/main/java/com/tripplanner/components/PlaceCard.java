package com.tripplanner.components;

import com.tripplanner.model.Place;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PlaceCard extends JPanel {
    private Place place;
    private boolean selected = false;

    public PlaceCard(Place place) {
        this.place = place;

        setPreferredSize(new Dimension(300, 80));
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = !selected;
                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(Color.LIGHT_GRAY);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(selected ? Color.CYAN : Color.WHITE);
                repaint();
            }
        });
    }

    // Keep the old constructor for backward compatibility
    public PlaceCard(String placeName, String city, String address, double rating) {
        this(new Place(placeName, city, address, rating, 0.0, 0.0, "unknown"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw place name
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.BLACK);
        g2d.drawString(place.getName(), 10, 20);

        // Draw address
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(Color.GRAY);
        g2d.drawString(place.getAddress(), 10, 40);

        // Draw rating
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.ORANGE);
        g2d.drawString("Ocena: " + String.format("%.1f", place.getRating()), 10, 60);

        // Draw selection indicator
        if (selected) {
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
        }
    }

    // Getters
    public boolean isSelected() {
        return selected;
    }

    public void setSelected() {
        selected = false;
    }

    public Place getPlace() {
        return place;
    }
}