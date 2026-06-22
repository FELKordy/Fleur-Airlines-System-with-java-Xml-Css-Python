package com.fleurairlines.ui;

import java.util.ArrayList;
import java.util.List;

import com.fleurairlines.model.Flight;
import com.fleurairlines.model.MealType;
import com.fleurairlines.model.Passenger;
import com.fleurairlines.model.Seat;
import com.fleurairlines.util.InputSanitizer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class BookingExtrasScreen {

    private static final double PET_FEE = 80.0;

    private final Stage stage;
    private final Flight flight;
    private final Passenger primaryPassenger;
    private final List<Seat> selectedSeats;

    private final Label summaryLabel = new Label();
    private final ComboBox<String> luggageBox = new ComboBox<>();
    private final ComboBox<MealType> mealBox = new ComboBox<>();
    private final ComboBox<String> drinkBox = new ComboBox<>();
    private final CheckBox petCheckbox = new CheckBox("Add In-Cabin Pet Carrier (€80.00)");
    private final List<TicketHolder> ticketHolders = new ArrayList<>();

    public BookingExtrasScreen(Stage stage, Flight flight, Passenger passenger, List<Seat> selectedSeats) {
        this.stage = stage;
        this.flight = flight;
        this.primaryPassenger = passenger;
        this.selectedSeats = new ArrayList<>(selectedSeats);
    }

    public Scene createScene(double width, double height) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");

        HBox topNav = new HBox(16);
        topNav.setPadding(new Insets(20, 32, 10, 32));
        topNav.setAlignment(Pos.CENTER_LEFT);
        topNav.setStyle("-fx-background-color: #0f2b5c;");

        Label headerTitle = new Label("✈ FLEUR AIRLINES");
        headerTitle.setTextFill(Color.WHITE);
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button backBtn = new Button("← Back");
        backBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-radius: 4px; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(e -> SceneTransitionUtil.standardFade(stage, new SeatSelectionScreen(stage, flight, primaryPassenger, selectedSeats.get(0).getSeatClass()).createScene(width, height)));

        topNav.getChildren().addAll(headerTitle, spacer, backBtn);

        VBox header = new VBox(topNav, FlightSearchScreen.buildProgressStepper(3));

        VBox content = new VBox(24);
        content.setPadding(new Insets(40));
        content.setMaxWidth(850);
        content.setAlignment(Pos.TOP_CENTER);

        Label pageTitle = new Label("Passengers & Popular Extras");
        pageTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f2b5c;");
        
        VBox ticketsBox = new VBox(16);
        for (int i = 0; i < selectedSeats.size(); i++) {
            TicketHolder holder = new TicketHolder(selectedSeats.get(i), i == 0);
            ticketHolders.add(holder);
            ticketsBox.getChildren().add(holder.createRow());
        }

        VBox extrasBox = buildExtrasGrid();
        updateSummary();

        Button continueBtn = new Button("Continue");
        continueBtn.setStyle("-fx-background-color: #0073bc; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 14px 48px; -fx-background-radius: 24px; -fx-cursor: hand;");
        continueBtn.setOnAction(e -> onContinue(width, height));

        HBox bottomAction = new HBox(continueBtn);
        bottomAction.setAlignment(Pos.CENTER_RIGHT);
        bottomAction.setPadding(new Insets(20, 0, 0, 0));

        content.getChildren().addAll(pageTitle, ticketsBox, new Separator(), extrasBox, new Separator(), summaryLabel, bottomAction);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setTop(header);
        root.setCenter(scroll);
        root.setBottom(new FleurFooter());

        return new Scene(root, width, height);
    }

    private VBox buildExtrasGrid() {
        VBox box = new VBox(16);
        Label title = new Label("Enhance your trip");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f2b5c;");

        // Styling the controls explicitly so text is never invisible
        String comboStyle = "-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-text-fill: #0f2b5c; -fx-font-size: 13px; -fx-padding: 4px;";
        
        luggageBox.getItems().addAll("1x Personal Item (Free)", "Cabin Baggage 15kg (€45.00)", "Checked Baggage 23kg (€70.00)", "Heavy Baggage 32kg (€110.00)");
        luggageBox.getSelectionModel().selectFirst();
        luggageBox.setStyle(comboStyle);
        luggageBox.setPrefWidth(300);

        mealBox.getItems().addAll(MealType.STANDARD, MealType.KOSHER, MealType.HALAL, MealType.VEGAN, MealType.SEAFOOD);
        mealBox.getSelectionModel().select(MealType.STANDARD);
        mealBox.setStyle(comboStyle);
        mealBox.setPrefWidth(300);

        drinkBox.getItems().addAll("Complimentary Water/Juice", "Premium Soft Drink (€6.00)", "Exclusive Wine/Spirits (€25.00)");
        drinkBox.getSelectionModel().selectFirst();
        drinkBox.setStyle(comboStyle);
        drinkBox.setPrefWidth(300);
        
        petCheckbox.setStyle("-fx-text-fill: #0f2b5c; -fx-font-weight: bold; -fx-font-size: 14px;");

        HBox row1 = new HBox(24, buildExtraItem("Baggage", luggageBox), buildExtraItem("Pet in Cabin", petCheckbox));
        HBox row2 = new HBox(24, buildExtraItem("Meals", mealBox), buildExtraItem("Refreshments", drinkBox));

        box.getChildren().addAll(title, row1, row2);

        luggageBox.valueProperty().addListener((obs, o, n) -> updateSummary());
        mealBox.valueProperty().addListener((obs, o, n) -> updateSummary());
        drinkBox.valueProperty().addListener((obs, o, n) -> updateSummary());
        petCheckbox.selectedProperty().addListener((obs, o, n) -> updateSummary());

        return box;
    }

    private VBox buildExtraItem(String labelText, javafx.scene.Node control) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(400);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f2b5c; -fx-font-size: 14px;");
        
        card.getChildren().addAll(lbl, control);
        return card;
    }

    private void onContinue(double width, double height) {
        try {
            List<OrderConfirmationScreen.OrderTicket> tickets = new ArrayList<>();
            boolean primaryUserPresent = false;
            
            for (TicketHolder holder : ticketHolders) {
                String name = holder.nameField.getText().trim();
                String ageStr = holder.ageField.getText().trim();
                String passport = holder.passportField.getText().trim().toUpperCase();
                
                if (name.isEmpty() || ageStr.isEmpty() || passport.isEmpty()) {
                    throw new IllegalArgumentException("All ticket fields are required to secure your reservation.");
                }
                int age = Integer.parseInt(ageStr);
                if (!InputSanitizer.isValidPassport(passport)) {
                    throw new IllegalArgumentException("Invalid passport structure for: " + name);
                }
                if (name.equalsIgnoreCase(primaryPassenger.getName())) primaryUserPresent = true;
                
                double adjustedPrice = calculateSeatPrice(holder.seat, age);
                tickets.add(new OrderConfirmationScreen.OrderTicket(holder.seat, name, age, passport, adjustedPrice));
            }
            if (!primaryUserPresent) throw new IllegalArgumentException("Primary traveler must be assigned a seat.");

            SceneTransitionUtil.standardSlide(stage, new OrderConfirmationScreen(
                    stage, flight, primaryPassenger, tickets, mealBox.getValue(), 
                    drinkBox.getValue(), getSelectedLuggageCount(), petCheckbox.isSelected(), calculateExtrasCost()
            ).createScene(width, height));
        } catch (Exception ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
        }
    }

    private int getSelectedLuggageCount() {
        return luggageBox.getSelectionModel().getSelectedIndex();
    }

    private void updateSummary() {
        double seatTotal = ticketHolders.stream().mapToDouble(h -> {
            try { return calculateSeatPrice(h.seat, Integer.parseInt(h.ageField.getText().trim())); } 
            catch (Exception ex) { return h.seat.getPrice(); }
        }).sum();

        summaryLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f2b5c;");
        summaryLabel.setText(String.format("Total Value: €%.2f", (seatTotal + calculateExtrasCost())));
    }

    private double calculateSeatPrice(Seat seat, int age) {
        if (age < 2) {
            if (seat.getSeatClass() == Seat.SeatClass.ECONOMY) return 0.00;
            return seat.getPrice() * 0.5;
        }
        return seat.getPrice();
    }

    private double calculateExtrasCost() {
        double luggageCost = switch (luggageBox.getSelectionModel().getSelectedIndex()) {
            case 1 -> 45.00; case 2 -> 70.00; case 3 -> 110.00; default -> 0.00;
        };
        double mealCost = (mealBox.getValue() == MealType.SEAFOOD) ? 50.00 : 0.00;
        double drinkCost = drinkBox.getValue().contains("€6") ? 6.00 : (drinkBox.getValue().contains("€25") ? 25.00 : 0.00);
        return luggageCost + mealCost + drinkCost + (petCheckbox.isSelected() ? PET_FEE : 0.00);
    }

    private final class TicketHolder {
        final Seat seat;
        final TextField nameField = new TextField();
        final TextField ageField = new TextField();
        final TextField passportField = new TextField();

        TicketHolder(Seat seat, boolean isPrimary) {
            this.seat = seat;
            String fieldStyle = "-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-padding: 8px; -fx-text-fill: #0f2b5c;";
            nameField.setPromptText("First & Last Name");     nameField.setStyle(fieldStyle);
            ageField.setPromptText("Age");                   ageField.setStyle(fieldStyle);
            passportField.setPromptText("Passport ID");       passportField.setStyle(fieldStyle);

            if (isPrimary) {
                nameField.setText(primaryPassenger.getName());
                ageField.setText(String.valueOf(primaryPassenger.getAge()));
                passportField.setText(primaryPassenger.getPassportNumber());
            }
            nameField.textProperty().addListener((obs, o, n) -> updateSummary());
            ageField.textProperty().addListener((obs, o, n) -> updateSummary());
        }

        VBox createRow() {
            Label seatLabel = new Label(String.format("Adult (Seat %s — %s)", seat.getSeatNumber(), seat.getSeatClass()));
            seatLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f2b5c; -fx-font-size: 16px;");
            
            HBox fieldsRow = new HBox(16, nameField, ageField, passportField);
            HBox.setHgrow(nameField, Priority.ALWAYS); 
            
            VBox row = new VBox(16, seatLabel, fieldsRow);
            row.setPadding(new Insets(24));
            row.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");
            return row;
        }
    }
}