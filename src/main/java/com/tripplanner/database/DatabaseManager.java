package com.tripplanner.database;

import com.tripplanner.config.ConfigManager;
import com.tripplanner.model.Place;
import javax.persistence.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static DatabaseManager instance;
    private EntityManagerFactory emf;

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initializeDatabase() {
        try {
            // create data if doesnt exist
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                System.out.println("Created data directory: " + dataDir.getAbsolutePath());
            }

            // creating EntityManagerFactory
            Map<String, Object> configOverrides = new HashMap<>();
            configOverrides.put("javax.persistence.jdbc.url", ConfigManager.getInstance().getDatabaseUrl());
            configOverrides.put("javax.persistence.jdbc.user", ConfigManager.getInstance().getDatabaseUser());
            configOverrides.put("javax.persistence.jdbc.password", ConfigManager.getInstance().getDatabasePassword());
            configOverrides.put("javax.persistence.jdbc.driver", "org.h2.Driver");
            configOverrides.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            configOverrides.put("hibernate.hbm2ddl.auto", "update");
            configOverrides.put("hibernate.show_sql", "false");
            configOverrides.put("hibernate.format_sql", "true");

            emf = Persistence.createEntityManagerFactory("tripplanner", configOverrides);

            // test connection
            testConnection();
            System.out.println("Database initialized successfully!");

        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void testConnection() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            // test query
            em.createQuery("SELECT COUNT(p) FROM Place p").getSingleResult();
            em.getTransaction().commit();
            System.out.println("Database connection test successful!");
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("Database connection test failed, but tables may be created now: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public boolean savePlace(Place place) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            List<Place> existingPlaces = em.createQuery(
                            "SELECT p FROM Place p WHERE p.name = :name AND p.address = :address", Place.class)
                    .setParameter("name", place.getName())
                    .setParameter("address", place.getAddress())
                    .getResultList();

            if (existingPlaces.isEmpty()) {
                em.persist(place);
                em.getTransaction().commit();
                System.out.println("Saved new place: " + place.getName());
                return true;
            } else {
                em.getTransaction().rollback();
                System.out.println("Place already exists: " + place.getName());
                return false;
            }
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.err.println("Failed to save place: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public List<Place> getAllPlaces() {
        EntityManager em = emf.createEntityManager();
        try {
            List<Place> places = em.createQuery("SELECT p FROM Place p ORDER BY p.name", Place.class).getResultList();
            System.out.println("Retrieved " + places.size() + " saved places from database");
            return places;
        } catch (Exception e) {
            System.err.println("Failed to get places: " + e.getMessage());
            return List.of(); // Return empty list instead of null
        } finally {
            em.close();
        }
    }

    public boolean deletePlace(Long placeId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Place place = em.find(Place.class, placeId);
            if (place != null) {
                em.remove(place);
                em.getTransaction().commit();
                System.out.println("Deleted place: " + place.getName());
                return true;
            } else {
                em.getTransaction().rollback();
                System.out.println("Place not found for deletion");
                return false;
            }
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.err.println("Failed to delete place: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("EntityManagerFactory closed.");
        }
    }


}
