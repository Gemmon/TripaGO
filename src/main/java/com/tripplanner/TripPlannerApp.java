package com.tripplanner;

import com.tripplanner.gui.MainFrame;
import com.tripplanner.config.ConfigManager;
import com.tripplanner.database.DatabaseManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JOptionPane;

public class TripPlannerApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Loading configuration...");
                ConfigManager.getInstance().loadConfig();

                System.out.println("Initializing database...");
                DatabaseManager.getInstance().initializeDatabase();

                System.out.println("Starting main frame...");
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);

                System.out.println("Application started successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}