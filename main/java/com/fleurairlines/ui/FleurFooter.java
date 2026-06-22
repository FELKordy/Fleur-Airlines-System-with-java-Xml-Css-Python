package com.fleurairlines.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import java.awt.Desktop;
import java.net.URI;

public class FleurFooter extends HBox {

    public FleurFooter() {
        setPadding(new Insets(10, 20, 10, 20));
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #1a2744;");

        Label contact = new Label("Contact us:");
        contact.setTextFill(Color.web("#c9a84c"));
        contact.setStyle("-fx-font-weight: bold;");

        Label email = new Label("✉  fleurairlines@gmail.com");
        email.setTextFill(Color.WHITE);

        Label phone = new Label("📞  +393520025012");
        phone.setTextFill(Color.WHITE);

        Hyperlink facebook  = link("Facebook",  "https://www.facebook.com/share/1FiRebYLrQ/");
        Hyperlink instagram = link("Instagram", "https://www.instagram.com/fleurailrines?igsh=NGthczc1emJ5dHV5");
        Hyperlink threads   = link("Threads",   "https://www.threads.com/@fleurailrines");

        Label sep1 = separator();
        Label sep2 = separator();
        Label sep3 = separator();
        Label sep4 = separator();

        Region spacerL = new Region();
        Region spacerR = new Region();
        HBox.setHgrow(spacerL, Priority.ALWAYS);
        HBox.setHgrow(spacerR, Priority.ALWAYS);

        getChildren().addAll(
                contact, spacerL,
                email, sep1, phone, sep2,
                facebook, sep3, instagram, sep4, threads,
                spacerR
        );
    }

    private Hyperlink link(String text, String url) {
        Hyperlink h = new Hyperlink(text);
        h.setStyle("-fx-text-fill: #c9a84c; -fx-border-color: transparent;");
        h.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (java.io.IOException | java.net.URISyntaxException ex) {
                System.err.println("Could not open: " + url);
            }
        });
        return h;
    }

    private Label separator() {
        Label l = new Label("  |  ");
        l.setTextFill(Color.web("#c9a84c"));
        return l;
    }
}