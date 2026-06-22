package com.fleurairlines.ui;

import java.util.Arrays;
import java.util.Locale;

import com.fleurairlines.database.DatabaseService;
import com.fleurairlines.model.Passenger;
import com.fleurairlines.pattern.SessionManager;
import com.fleurairlines.util.DatabaseException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class PassengerDashboard {

    private final Stage stage;
    private final Passenger passenger;

    public PassengerDashboard(Stage stage, Passenger passenger) {
        this.stage = stage;
        this.passenger = passenger;
    }

    public Scene createScene(double width, double height) throws DatabaseException {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");

        // ── Top Navigation ───────────────────────────────────────────────────
        HBox topNav = new HBox(16);
        topNav.setPadding(new Insets(20, 32, 20, 32));
        topNav.setAlignment(Pos.CENTER_LEFT);
        topNav.setStyle("-fx-background-color: #0f2b5c;");

        Label logo = new Label("✈  FLEUR AIRLINES");
        logo.setTextFill(Color.WHITE);
        logo.setFont(Font.font("System", FontWeight.BOLD, 18));
        logo.setStyle("-fx-letter-spacing: 1px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label nameLbl = new Label("Welcome, " + passenger.getName());
        nameLbl.setTextFill(Color.WHITE);
        nameLbl.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));

        Label loyaltyLbl = new Label("✦ " + passenger.getLoyaltyPoints() + " Miles");
        loyaltyLbl.setTextFill(Color.web("#0073bc"));
        loyaltyLbl.setFont(Font.font("System", FontWeight.BOLD, 13));

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-radius: 4px; -fx-text-fill: white; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            SessionManager.getInstance().logout();
            SceneTransitionUtil.standardFade(stage, new LoginScreen(stage).createScene(width, height));
        });

        topNav.getChildren().addAll(logo, spacer, nameLbl, loyaltyLbl, logoutBtn);

        // ── Center Content ───────────────────────────────────────────────────
        VBox center = new VBox(32);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(40));

        VBox welcomeBox = new VBox(8);
        welcomeBox.setAlignment(Pos.CENTER);
        Label welcome = new Label("Where to next, " + passenger.getName().split(" ")[0] + "?");
        welcome.setFont(Font.font("System", FontWeight.BOLD, 32));
        welcome.setTextFill(Color.web("#0f2b5c"));
        
        Label promo = new Label("Experience the Mediterranean sky with premium comfort and flexible bookings.");
        promo.setTextFill(Color.web("#64748b"));
        promo.setFont(Font.font("System", 16));
        welcomeBox.getChildren().addAll(welcome, promo);

        HBox highlightCards = new HBox(24);
        highlightCards.setAlignment(Pos.CENTER);
        highlightCards.getChildren().addAll(
            travelCard("Athens", "Historic views and premium Mediterranean dining", "athens.png"),
            travelCard("Paris", "City lights and seamless arrival experiences", "paris.png"),
            travelCard("Dubai", "Luxury lounges and priority boarding", "dubai.png")
        );

        HBox actionButtons = new HBox(16);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button searchBtn   = bigButton("🔍 Find Flights");
        Button bookingsBtn = bigButton("📋 Manage Trips");
        Button profileBtn  = bigButton("👤 Account Profile");

        searchBtn.setOnAction(e -> SceneTransitionUtil.standardSlide(stage, new FlightSearchScreen(stage, passenger).createScene(width, height)));
        bookingsBtn.setOnAction(e -> SceneTransitionUtil.standardSlide(stage, new MyBookingsScreen(stage, passenger).createScene(width, height)));
        profileBtn.setOnAction(e -> SceneTransitionUtil.standardFade(stage, createProfileScene(width, height)));

        actionButtons.getChildren().addAll(searchBtn, bookingsBtn, profileBtn);

        center.getChildren().addAll(welcomeBox, highlightCards, actionButtons);

        root.setTop(topNav);
        root.setCenter(center);
        root.setBottom(new FleurFooter());

        return new Scene(root, width, height);
    }

    private Scene createProfileScene(double width, double height) {
        BorderPane profileRoot = new BorderPane();
        profileRoot.setStyle("-fx-background-color: #f4f6f9;");

        HBox profileNav = new HBox(16);
        profileNav.setPadding(new Insets(20, 32, 20, 32));
        profileNav.setAlignment(Pos.CENTER_LEFT);
        profileNav.setStyle("-fx-background-color: #0f2b5c;");

        Label profileLogo = new Label("✈  FLEUR AIRLINES");
        profileLogo.setTextFill(Color.WHITE);
        profileLogo.setFont(Font.font("System", FontWeight.BOLD, 18));

        Region profileSpacer = new Region();
        HBox.setHgrow(profileSpacer, Priority.ALWAYS);

        Button backBtn = new Button("← Dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-radius: 4px; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(ev -> {
            try {
                SceneTransitionUtil.standardFade(stage, createScene(width, height));
            } catch (DatabaseException ex) {
                System.getLogger(PassengerDashboard.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        });
        profileNav.getChildren().addAll(profileLogo, profileSpacer, backBtn);

        VBox card = new VBox(20);
        card.setMaxWidth(600);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 8);");

        Label title = new Label("Traveler Profile");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f2b5c;");

        card.getChildren().addAll(
                title,
                profileRow("Legal Name", passenger.getName()),
                profileRow("Email Address", passenger.getEmail()),
                profileRow("Contact Number", passenger.getPhone()),
                profileRow("Passport ID", passenger.getPassportNumber()),
                profileRow("Nationality", passenger.getNationality()),
                profileRow("Preferred Currency", passenger.getPreferredCurrency()),
                profileRow("Award Miles", String.valueOf(passenger.getLoyaltyPoints()))
        );

        Button editBtn = new Button("Update Information");
        editBtn.setStyle("-fx-background-color: #0073bc; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12px 32px; -fx-background-radius: 6px; -fx-cursor: hand;");
        editBtn.setOnAction(e -> {
            if (showEditProfileDialog()) {
                SceneTransitionUtil.standardFade(stage, createProfileScene(width, height));
            }
        });

        HBox btnBox = new HBox(editBtn);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(24, 0, 0, 0));
        card.getChildren().add(btnBox);

        StackPane cardWrapper = new StackPane(card);
        cardWrapper.setPadding(new Insets(40));

        profileRoot.setTop(profileNav);
        profileRoot.setCenter(cardWrapper);
        profileRoot.setBottom(new FleurFooter());

        return new Scene(profileRoot, width, height);
    }

    private boolean showEditProfileDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Information");
        DialogPane pane = dialog.getDialogPane();
        pane.setPrefWidth(500);

        VBox content = new VBox(16);
        content.setPadding(new Insets(20));

        TextField nameField = new TextField(passenger.getName());
        TextField phoneField = new TextField(passenger.getPhone());
        
        ComboBox<String> nationalityBox = new ComboBox<>();
        populateNationalities(nationalityBox);
        nationalityBox.setValue(passenger.getNationality());
        
        ComboBox<String> currencyBox = new ComboBox<>();
        currencyBox.getItems().addAll("USD", "EUR", "EGP", "GBP");
        currencyBox.setValue(passenger.getPreferredCurrency() != null ? passenger.getPreferredCurrency() : "USD");

        Label errorLbl = new Label();
        errorLbl.setTextFill(Color.web("#dc2626"));
        errorLbl.setWrapText(true);

        content.getChildren().addAll(
            new Label("Full Name:"), nameField,
            new Label("Phone:"), phoneField,
            new Label("Nationality:"), nationalityBox,
            new Label("Currency:"), currencyBox,
            errorLbl
        );

        pane.setContent(content);
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()) {
                errorLbl.setText("Name and phone fields cannot be empty.");
                event.consume();
            }
        });

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            passenger.setName(nameField.getText().trim());
            passenger.setPhone(phoneField.getText().trim());
            passenger.setNationality(nationalityBox.getValue());
            passenger.setPreferredCurrency(currencyBox.getValue());
            try {
                DatabaseService.getInstance().savePassenger(passenger);
                return true;
            } catch (DatabaseException e) {
                new Alert(Alert.AlertType.ERROR, "Save failed: " + e.getMessage()).showAndWait();
            }
        }
        return false;
    }

    private void populateNationalities(ComboBox<String> box) {
        box.getItems().addAll(Arrays.stream(Locale.getISOCountries())
                .map(code -> new Locale("", code).getDisplayCountry())
                .sorted().toList());
    }

    private HBox profileRow(String labelText, String valueText) {
        Label keyLbl = new Label(labelText);
        keyLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-min-width: 160px;");
        Label valLbl = new Label(valueText != null ? valueText : "—");
        valLbl.setStyle("-fx-text-fill: #0f2b5c; -fx-font-weight: bold;");
        HBox row = new HBox(16, keyLbl, valLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 0, 12, 0));
        row.setStyle("-fx-border-color: transparent transparent #e2e8f0 transparent; -fx-border-width: 0 0 1 0;");
        return row;
    }

    private Button bigButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(220, 48);
        btn.setStyle("-fx-background-color: #0f2b5c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;");
        return btn;
    }

    private VBox travelCard(String title, String subtitle, String imageName) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-padding: 24px; -fx-border-color: #e2e8f0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 10, 0, 0, 5);");
        card.setPrefWidth(240);
        Image image = loadTravelImage(imageName, title);
        if (image != null) {
            ImageView photo = new ImageView(image);
            photo.setPreserveRatio(true);
            photo.setSmooth(true);
            photo.setFitWidth(220);
            photo.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.10), 14, 0, 0, 8); -fx-background-radius: 8px;");
            card.getChildren().add(photo);
        }
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f2b5c; -fx-font-size: 16px;");
        Label s = new Label(subtitle);
        s.setWrapText(true);
        s.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-line-spacing: 4px;");
        card.getChildren().addAll(t, s);
        return card;
    }

    private Image loadTravelImage(String imageName, String fallbackTitle) {
        java.net.URL imageUrl = getClass().getResource("/images/" + imageName);
        if (imageUrl != null) {
            return new Image(imageUrl.toExternalForm(), 220, 120, true, true);
        }
        return createTravelPlaceholder(fallbackTitle, 220, 120);
    }

    private Image createTravelPlaceholder(String title, int width, int height) {
        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();
        Color base = Color.web("#1e3a8a");
        Color accent = Color.web("#0f2b5c");
        for (int y = 0; y < height; y++) {
            double t = (double) y / Math.max(1, height - 1);
            Color rowColor = base.interpolate(accent, t * 0.8);
            for (int x = 0; x < width; x++) {
                writer.setColor(x, y, rowColor);
            }
        }
        int cx = width / 2;
        int cy = height / 2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double dx = x - cx + 20;
                double dy = y - cy + 10;
                if (Math.abs(dx * dx + dy * dy - 1800) < 120) {
                    writer.setColor(x, y, Color.web("#ffffff", 0.12));
                }
            }
        }
        for (int y = 24; y < 74; y++) {
            for (int x = 24; x < 196; x++) {
                if ((x + y) % 14 < 4) {
                    writer.setColor(x, y, Color.web("#ffffff", 0.08));
                }
            }
        }
        for (int i = 0; i < title.length(); i++) {
            char c = Character.toUpperCase(title.charAt(i));
            int x0 = 24 + i * 14;
            int y0 = 88;
            if (c == ' ') continue;
            for (int dx = 0; dx < 8; dx++) {
                for (int dy = 0; dy < 16; dy++) {
                    int x = x0 + dx;
                    int y = y0 + dy;
                    if (x < width && y < height) {
                        writer.setColor(x, y, Color.web("#ffffff"));
                    }
                }
            }
        }
        return image;
    }
}