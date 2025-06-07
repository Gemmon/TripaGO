module com.tripplanner {
    requires java.sql;
    requires java.desktop;
    requires java.persistence;
    requires java.naming;
    requires java.xml.bind;

    // Automatic modules
    requires org.apache.commons.configuration2;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires commons.beanutils;
    requires org.json;

    // Hibernate and H2 are on the classpath as automatic modules
    requires transitive org.hibernate.orm.core;
    requires com.h2database;

    exports com.tripplanner;
    exports com.tripplanner.gui;
    exports com.tripplanner.model;
    exports com.tripplanner.services;
    exports com.tripplanner.events;

    // Hibernate
    opens com.tripplanner.model to org.hibernate.orm.core;
}