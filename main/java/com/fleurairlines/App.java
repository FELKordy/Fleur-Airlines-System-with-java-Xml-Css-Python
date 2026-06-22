package com.fleurairlines;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fleurairlines.database.DatabaseService;
import com.fleurairlines.ui.LoginScreen;
import com.fleurairlines.util.DatabaseException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws DatabaseException {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) ->
                logStartup("Uncaught exception on " + thread.getName() + ": " + error));
        logStartup("Application start");
        primaryStage.setTitle("Fleur Airlines | Global Reservation System");
        java.net.URL iconUrl = getClass().getResource("/images/app-icon.png");
        if (iconUrl != null) {
            primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
            logStartup("Loaded app icon");
        }
        
        // 1. Safely initialize the massive database using a try-catch block
        try {
            // This wakes up the DatabaseService and generates the 5,000+ flights safely!
            DatabaseService.getInstance(); 
            logStartup("Database initialized");
        } catch (Exception e) {
            logStartup("Database startup error: " + e.getMessage());
            System.err.println("Critical Database Startup Error: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to boot the flight database. Check your console logs.");
            alert.showAndWait();
            return; // Stops the app from launching broken
        }

        // 2. Initialize the premium Aegean-inspired interface with the login screen as the entry point
        Scene mainScene = new LoginScreen(primaryStage).createScene(1200, 700);
        logStartup("Login scene created");
        
        // 3. Inject the global stylesheet to ensure consistent rendering
        try {
            String cssPath = getClass().getResource("/application.css").toExternalForm();
            mainScene.getStylesheets().add(cssPath);
            logStartup("Stylesheet loaded");
        } catch (NullPointerException e) {
            logStartup("Stylesheet missing");
            System.err.println("Warning: application.css not found in resources folder. Running with default JavaFX theme.");
        }
        
        primaryStage.setScene(mainScene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        AtomicBoolean userRequestedClose = new AtomicBoolean(false);
        primaryStage.setOnCloseRequest(event -> {
            userRequestedClose.set(true);
            logStartup("Primary stage close requested");
        });
        primaryStage.setOnShown(event -> logStartup("Primary stage shown"));
        primaryStage.setOnHidden(event -> {
            logStartup("Primary stage hidden");
            if (!userRequestedClose.get()) {
                Platform.runLater(() -> {
                    logStartup("Restoring unexpectedly hidden primary stage");
                    primaryStage.show();
                    primaryStage.centerOnScreen();
                    primaryStage.toFront();
                    primaryStage.requestFocus();
                });
            }
        });
        primaryStage.centerOnScreen();
        primaryStage.show();
        primaryStage.centerOnScreen();
        primaryStage.toFront();
        primaryStage.requestFocus();
        logStartup("primaryStage.show() returned");
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void logStartup(String message) {
        try {
            Files.writeString(startupLogPath(),
                    LocalDateTime.now() + " - " + message + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // Startup logging must never stop the app from launching.
        }
    }

    private static Path startupLogPath() {
        try {
            Path codePath = Paths.get(App.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            Path basePath = Files.isRegularFile(codePath) ? codePath.getParent() : codePath;
            return basePath.resolve("fleur-airlines-startup.log");
        } catch (Exception ignored) {
            return Paths.get("fleur-airlines-startup.log");
        }
    }
}
