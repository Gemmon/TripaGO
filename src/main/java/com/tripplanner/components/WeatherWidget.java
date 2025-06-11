package com.tripplanner.components;

import javax.swing.*;
import java.awt.*;

public class WeatherWidget extends JPanel {
    private String location = "";
    private double temperature = 0.0;
    private String description = "";
    private int humidity = 0;
    private double windSpeed = 0.0;

    public WeatherWidget() {
        setPreferredSize(new Dimension(250, 150));
        setBorder(BorderFactory.createTitledBorder("Pogoda"));
        setBackground(new Color(230, 240, 255));
    }

    public void updateWeather(String location, double temperature, String description, int humidity, double windSpeed) {
        this.location = location;
        this.temperature = temperature;
        this.description = description;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (location.isEmpty()) {
            g2d.setColor(Color.GRAY);
            g2d.drawString("Brak danych pogodowych", 20, 50);
            return;
        }

        // lokalizacja
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.BLACK);
        g2d.drawString(location, 20, 30);

        // temperatura
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(temperature > 20 ? Color.RED : Color.BLUE);
        g2d.drawString(String.format("%.1f°C", temperature), 20, 60);

        // opis
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString(description, 20, 80);

        // wilgotność i wiatr
        g2d.drawString("Wilgotność: " + humidity + "%", 20, 100);
        g2d.drawString("Wiatr: " + String.format("%.1f", windSpeed) + " m/s", 20, 120);
    }
}
