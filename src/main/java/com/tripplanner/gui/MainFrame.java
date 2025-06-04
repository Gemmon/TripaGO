package com.tripplanner.gui;

import com.tripplanner.components.PlaceCard;
import com.tripplanner.components.WeatherWidget;
import com.tripplanner.events.*;
import com.tripplanner.model.Place;
import com.tripplanner.model.Weather;
import com.tripplanner.services.GooglePlacesService;
import com.tripplanner.services.WeatherService;
import com.tripplanner.database.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainFrame extends JFrame {
    private JTextField locationField;
    private JComboBox<String> typeComboBox;
    private JPanel placesPanel;
    private WeatherWidget weatherWidget;
    private GooglePlacesService placesService;
    private WeatherService weatherService;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public MainFrame() {
        initializeServices();
        initializeComponents();
        setupLayout();
        registerEventHandlers();

        setTitle("Planowanie Wycieczek - Debug Version");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Dodaj przykładowe dane na start
        showStartupMessage();
    }

    private void showStartupMessage() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Aplikacja gotowa. Wprowadź lokalizację i kliknij 'Szukaj'");
        });
    }

    private void initializeServices() {
        placesService = new GooglePlacesService();
        weatherService = new WeatherService();
        EventBus.getInstance().register(this);
    }

    private void initializeComponents() {
        locationField = new JTextField("Warszawa", 20); // Dodaj domyślną wartość
        typeComboBox = new JComboBox<>(new String[]{
                "restaurant", "tourist_attraction", "lodging", "museum", "park"
        });

        placesPanel = new JPanel();
        placesPanel.setLayout(new BoxLayout(placesPanel, BoxLayout.Y_AXIS));
        placesPanel.setBorder(BorderFactory.createTitledBorder("Znalezione miejsca"));

        weatherWidget = new WeatherWidget();
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        statusLabel = new JLabel("Ładowanie aplikacji...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panel wyszukiwania
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Wyszukiwanie"));

        searchPanel.add(new JLabel("Lokalizacja:"));
        searchPanel.add(locationField);
        searchPanel.add(new JLabel("Typ:"));
        searchPanel.add(typeComboBox);

        JButton searchButton = new JButton("Szukaj");
        searchButton.addActionListener(this::performSearch);
        searchPanel.add(searchButton);

        JButton saveButton = new JButton("Zapisz wybrane");
        saveButton.addActionListener(this::saveSelectedPlaces);
        searchPanel.add(saveButton);

        // Dodaj przycisk do testowania
        JButton testButton = new JButton("Test");
        testButton.addActionListener(e -> testWithMockData());
        searchPanel.add(testButton);

        JButton savedButton = new JButton("Pokaż zapisane");
        savedButton.addActionListener(e -> showSavedPlacesDialog());
        searchPanel.add(savedButton);


        add(searchPanel, BorderLayout.NORTH);

        // Panel główny
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel miejsc
        JScrollPane placesScrollPane = new JScrollPane(placesPanel);
        placesScrollPane.setPreferredSize(new Dimension(600, 400));
        mainPanel.add(placesScrollPane, BorderLayout.CENTER);

        // Panel pogody
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(weatherWidget, BorderLayout.NORTH);

        // Dodaj panel z informacjami debugowymi
        JTextArea debugArea = new JTextArea(10, 30);
        debugArea.setEditable(false);
        debugArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        JScrollPane debugScroll = new JScrollPane(debugArea);
        debugScroll.setBorder(BorderFactory.createTitledBorder("Logi debugowe"));
        rightPanel.add(debugScroll, BorderLayout.CENTER);

        // Przekieruj System.out do debugArea
        redirectSystemOut(debugArea);

        mainPanel.add(rightPanel, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);

        // Panel statusu
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void redirectSystemOut(JTextArea textArea) {
        // Prosty system przekierowania logów do GUI
        System.setOut(new java.io.PrintStream(System.out) {
            @Override
            public void println(String s) {
                super.println(s);
                SwingUtilities.invokeLater(() -> {
                    textArea.append(s + "\n");
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                });
            }
        });
    }

    private void showSavedPlacesDialog() {
        SavedPlacesDialog dialog = new SavedPlacesDialog(this);
        dialog.setVisible(true);
    }


    private void registerEventHandlers() {
        EventBus.getInstance().register(this);
    }

    private void testWithMockData() {
        System.out.println("=== ROZPOCZĘCIE TESTU Z PRZYKŁADOWYMI DANYMI ===");
        statusLabel.setText("Test z przykładowymi danymi...");

        // Utwórz przykładowe dane
        List<Place> testPlaces = List.of(
                new Place("Test Restaurant", "ul. Testowa 1, Warszawa", 4.5, 52.2297, 21.0122, "restaurant"),
                new Place("Test Museum", "ul. Muzealna 2, Warszawa", 4.2, 52.2317, 21.0142, "museum"),
                new Place("Test Park", "ul. Parkowa 3, Warszawa", 4.8, 52.2287, 21.0102, "park")
        );

        // Wyślij event z przykładowymi danymi
        EventBus.getInstance().post(new PlacesFoundEvent(testPlaces));

        System.out.println("Wysłano event z " + testPlaces.size() + " przykładowymi miejscami");
        statusLabel.setText("Test zakończony - sprawdź czy miejsca się wyświetlają");
    }

    private void performSearch(ActionEvent e) {
        String location = locationField.getText().trim();
        String type = (String) typeComboBox.getSelectedItem();

        System.out.println("=== ROZPOCZĘCIE WYSZUKIWANIA ===");
        System.out.println("Lokalizacja: " + location);
        System.out.println("Typ: " + type);

        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Wprowadź lokalizację!");
            return;
        }

        statusLabel.setText("Wyszukiwanie...");
        progressBar.setVisible(true);

        // Wyczyść poprzednie wyniki
        placesPanel.removeAll();
        placesPanel.revalidate();
        placesPanel.repaint();

        // Asynchroniczne wyszukiwanie miejsc
        CompletableFuture<List<Place>> placesTask = placesService.searchPlaces(location, type);
        CompletableFuture<Weather> weatherTask = weatherService.getWeather(location);

        // Kombinowanie zadań
        CompletableFuture.allOf(placesTask, weatherTask).thenRunAsync(() -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    List<Place> places = placesTask.get();
                    Weather weather = weatherTask.get();

                    System.out.println("Otrzymano " + places.size() + " miejsc");
                    System.out.println("Pogoda: " + (weather != null ? "OK" : "BRAK"));

                    EventBus.getInstance().post(new PlacesFoundEvent(places));
                    if (weather != null) {
                        EventBus.getInstance().post(new WeatherUpdatedEvent(weather));
                    }

                    progressBar.setVisible(false);
                    statusLabel.setText("Wyszukiwanie zakończone. Znaleziono: " + places.size() + " miejsc");

                } catch (Exception ex) {
                    System.err.println("Błąd podczas wyszukiwania: " + ex.getMessage());
                    ex.printStackTrace();
                    progressBar.setVisible(false);
                    statusLabel.setText("Błąd podczas wyszukiwania");
                }
            });
        });
    }

    private void saveSelectedPlaces(ActionEvent e) {
        Component[] components = placesPanel.getComponents();
        int savedCount = 0;

        for (Component comp : components) {
            if (comp instanceof PlaceCard && ((PlaceCard) comp).isSelected()) {
                System.out.println("Zapisywanie: " + ((PlaceCard) comp).getPlaceName());
                savedCount++;
            }
        }

        if (savedCount > 0) {
            JOptionPane.showMessageDialog(this, "Zapisano " + savedCount + " miejsc");
            statusLabel.setText("Zapisano " + savedCount + " miejsc");
        } else {
            JOptionPane.showMessageDialog(this, "Nie wybrano żadnych miejsc do zapisania");
        }
    }

    @Subscribe
    public void handlePlacesFound(PlacesFoundEvent event) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("=== OBSŁUGA ZDARZENIA PLACES_FOUND ===");
            System.out.println("Liczba miejsc: " + event.getPlaces().size());

            placesPanel.removeAll();

            if (event.getPlaces().isEmpty()) {
                JLabel noResultsLabel = new JLabel("Brak wyników wyszukiwania");
                noResultsLabel.setHorizontalAlignment(JLabel.CENTER);
                placesPanel.add(noResultsLabel);
                System.out.println("Brak miejsc do wyświetlenia");
            } else {
                for (Place place : event.getPlaces()) {
                    PlaceCard card = new PlaceCard(place.getName(), place.getAddress(), place.getRating());
                    placesPanel.add(card);
                    placesPanel.add(Box.createVerticalStrut(5));
                    System.out.println("Dodano kartę: " + place.getName());
                }
            }

            placesPanel.revalidate();
            placesPanel.repaint();
            System.out.println("Panel miejsc odświeżony");
        });
    }

    @Subscribe
    public void handleWeatherUpdated(WeatherUpdatedEvent event) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("=== OBSŁUGA ZDARZENIA WEATHER_UPDATED ===");
            Weather weather = event.getWeather();
            weatherWidget.updateWeather(
                    weather.getLocation(),
                    weather.getTemperature(),
                    weather.getDescription(),
                    weather.getHumidity(),
                    weather.getWindSpeed()
            );

            // Zapis pogody do bazy danych
            try {
                DatabaseManager.getInstance().saveWeather(weather);
                System.out.println("Pogoda zapisana do bazy danych");
            } catch (Exception e) {
                System.err.println("Błąd zapisu pogody: " + e.getMessage());
            }
        });
    }
}