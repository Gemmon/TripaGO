package com.tripplanner.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PlaceCard extends JPanel {
    private String placeName;
    private String address;
    private double rating;
    private boolean selected = false;

    public PlaceCard(String placeName, String address, double rating) {
        this.placeName = placeName;
        this.address = address;
        this.rating = rating;

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Rysowanie nazwy miejsca
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.BLACK);
        g2d.drawString(placeName, 10, 20);

        // Rysowanie adresu
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(Color.GRAY);
        g2d.drawString(address, 10, 40);

        // Rysowanie oceny
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.ORANGE);
        g2d.drawString("★ " + String.format("%.1f", rating), 10, 60);

        // Zaznaczenie
        if (selected) {
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
        }
    }

    public boolean isSelected() { return selected; }
    public String getPlaceName() { return placeName; }
}
