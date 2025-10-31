package com.example;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {
    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
    //contains business logic
    //skicka meddelande till ntfy och ta emot nya

    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }
}
