package com.fleurairlines.ui ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fleurairlines.model.Flight;
import com.fleurairlines.model.Passenger;
import com.fleurairlines.model.Seat;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class SeatSelectionScreen {

    private final Stage stage;
    private final Flight flight;
    private final Flight returnFlight;
    private final Passenger passenger;
    private final Seat.SeatClass targetCabin;
    private final Seat.SeatClass returnCabin;
    private final boolean roundTrip;

    private final Map<Seat, Button> seatButtonsCache = new HashMap<>();
    private final Set<Seat> selectedSeats = new HashSet<>();
    private final Label priceLbl = new Label("No seats selected");
    private final Button confirmBtn = new Button("Confirm Selection");

    private static final String SEAT_BASE_STYLE = "-fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 6px 6px 2px 2px; -fx-cursor: hand; -fx-border-radius: 6px 6px 2px 2px;";

    public SeatSelectionScreen(Stage stage, Flight flight, Passenger passenger) {
        this(stage, flight, passenger, null, null, null);
    }

    public SeatSelectionScreen(Stage stage, Flight flight, Passenger passenger, Seat.SeatClass targetCabin) {
        this(stage, flight, passenger, targetCabin, null, null);
    }

    public SeatSelectionScreen(Stage stage, Flight outboundFlight, Seat.SeatClass outboundCabin, Flight returnFlight, Seat.SeatClass returnCabin, Passenger passenger) {
        this(stage, outboundFlight, passenger, outboundCabin, returnFlight, returnCabin);
    }

    private SeatSelectionScreen(Stage stage, Flight flight, Passenger passenger,
                                Seat.SeatClass targetCabin, Flight returnFlight, Seat.SeatClass returnCabin) {
        this.stage = stage;
        this.flight = flight;
        this.passenger = passenger;
        this.targetCabin = targetCabin;
        this.returnFlight = returnFlight;
        this.returnCabin = returnCabin;
        this.roundTrip = returnFlight != null;
    }

    public Scene createScene(double width, double height) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");

        VBox header = new VBox();
        HBox topBox = new HBox(16);
        topBox.setPadding(new Insets(20, 24, 10, 24));
        topBox.setStyle("-fx-background-color: #0f2b5c;");
        topBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("SELECT SEATS: " + (targetCabin == null ? "ALL CLASSES" : targetCabin.name()));
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label flightInfo = new Label(String.format(" | Flight %s (%s → %s)", 
            flight.getFlightNumber(), flight.getOrigin().getAirportCode(), flight.getDestination().getAirportCode()));
        flightInfo.setTextFill(Color.web("#cbd5e1"));
        flightInfo.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));

        topBox.getChildren().addAll(title, flightInfo);
        header.getChildren().addAll(topBox, FlightSearchScreen.buildProgressStepper(2));

        VBox airplane = new VBox(12);
        airplane.setAlignment(Pos.TOP_CENTER);
        airplane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-width: 4px; " +
                          "-fx-border-radius: 120 120 20 20; -fx-background-radius: 120 120 20 20; " +
                          "-fx-padding: 60 40 40 40; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        airplane.setMaxWidth(480);

        Label cockpit = new Label("FRONT OF AIRCRAFT");
        cockpit.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 10px;");
        airplane.getChildren().add(cockpit);
        
        // Correct Column Setup (A, B, C, D, E, F)
        HBox letters = new HBox(12);
        letters.setAlignment(Pos.CENTER);
        letters.setPadding(new Insets(10, 0, 10, 0));
        String[] cols = {"A", "B", "C", "   ", "D", "E", "F"};
        for (String c : cols) {
            Label l = new Label(c);
            l.setPrefWidth(c.trim().isEmpty() ? 30 : 45);
            l.setAlignment(Pos.CENTER);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");
            letters.getChildren().add(l);
        }
        airplane.getChildren().add(letters);

        Map<Integer, Map<String, Seat>> structuralGridMap = new HashMap<>();
        for (Seat s : flight.getSeats()) {
            try {
                int rowNum = Integer.parseInt(s.getSeatNumber().replaceAll("\\D+", ""));
                String colLetter = s.getSeatNumber().replaceAll("\\d+", "").toUpperCase();
                structuralGridMap.computeIfAbsent(rowNum, r -> new HashMap<>()).put(colLetter, s);
            } catch (NumberFormatException | NullPointerException ignored) {}
        }

        int maxRow = structuralGridMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(1);

        for (int row = 1; row <= maxRow; row++) {
            Map<String, Seat> rowData = structuralGridMap.get(row);
            if (rowData == null) continue;
            
            boolean rowMatchesCabin = targetCabin == null || rowData.values().stream().anyMatch(s -> s.getSeatClass() == targetCabin);
            if (!rowMatchesCabin) continue;

            HBox rowUI = new HBox(12);
            rowUI.setAlignment(Pos.CENTER);

            for (String colLtr : new String[]{"A", "B", "C", "AISLE", "D", "E", "F"}) {
                if (colLtr.equals("AISLE")) {
                    Label rowNumLbl = new Label(String.valueOf(row));
                    rowNumLbl.setPrefWidth(30);
                    rowNumLbl.setAlignment(Pos.CENTER);
                    rowNumLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
                    rowUI.getChildren().add(rowNumLbl);
                    continue;
                }

                Seat seat = rowData.get(colLtr);
                Button btn = new Button();
                btn.setPrefSize(45, 48);

                if (seat == null) {
                    btn.setStyle("-fx-background-color: transparent;");
                    btn.setDisable(true);
                } else {
                    btn.setText(seat.getSeatNumber() + "\n$" + (int)seat.getPrice());
                    btn.setUserData(seat);
                    seatButtonsCache.put(seat, btn);
                    applyCachedStyle(seat, btn, false);

                    if (!seat.isAvailable()) {
                        btn.setDisable(true);
                    } else {
                        btn.setOnAction(e -> {
                            if (selectedSeats.contains(seat)) {
                                selectedSeats.remove(seat);
                                applyCachedStyle(seat, btn, false);
                            } else {
                                selectedSeats.add(seat);
                                applyCachedStyle(seat, btn, true);
                            }
                            updatePriceSummary();
                        });
                    }
                }
                rowUI.getChildren().add(btn);
            }
            airplane.getChildren().add(rowUI);
        }

        VBox planeContainer = new VBox(airplane);
        planeContainer.setAlignment(Pos.CENTER);
        planeContainer.setPadding(new Insets(32));

        ScrollPane scrollPane = new ScrollPane(planeContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        HBox bottomBar = new HBox(24);
        bottomBar.setPadding(new Insets(20, 32, 20, 32));
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        Button backBtn = new Button("← Back to Search");
        backBtn.setStyle("-fx-background-color: #cbd5e1; -fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6px; -fx-cursor: hand;");
        backBtn.setOnAction(e -> SceneTransitionUtil.standardFade(stage, new FlightSearchScreen(stage, passenger).createScene(width, height)));

        priceLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #0f2b5c; -fx-font-weight: bold;");
        confirmBtn.setDisable(true);
        confirmBtn.setDefaultButton(true);
        confirmBtn.setStyle("-fx-background-color: #0073bc; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 28; -fx-background-radius: 4px; -fx-cursor: hand;");
        confirmBtn.setOnAction(e -> {
            if (!selectedSeats.isEmpty()) {
                SceneTransitionUtil.standardSlide(stage, new BookingExtrasScreen(stage, flight, passenger, new ArrayList<>(selectedSeats)).createScene(width, height));
            }
        });

        HBox.setHgrow(priceLbl, Priority.ALWAYS);
        priceLbl.setMaxWidth(Double.MAX_VALUE);
        bottomBar.getChildren().addAll(backBtn, priceLbl, confirmBtn);

        root.setTop(header);
        root.setCenter(scrollPane);
        root.setBottom(bottomBar);

        return new Scene(root, width, height);
    }

    private void applyCachedStyle(Seat seat, Button btn, boolean isSelected) {
        if (isSelected) {
            btn.setStyle(SEAT_BASE_STYLE + " -fx-background-color: #10b981; -fx-text-fill: white; -fx-border-color: #047857; -fx-border-width: 0 0 4 0;");
        } else if (!seat.isAvailable()) {
            btn.setStyle(SEAT_BASE_STYLE + " -fx-background-color: #ef4444; -fx-text-fill: white; -fx-border-color: #b91c1c; -fx-border-width: 0 0 4 0;");
        } else {
            // First Class uses Gold colors, Business uses Blue, Economy uses Gray
            String hexColor;
            switch (seat.getSeatClass()) {
                case FIRST:
                    hexColor = "#c9a84c";
                    break;
                case BUSINESS:
                    hexColor = "#0073bc";
                    break;
                default:
                    hexColor = "#e2e8f0";
                    break;
            }
            String borderHex;
            switch (seat.getSeatClass()) {
                case FIRST:
                    borderHex = "#a08232";
                    break;
                case BUSINESS:
                    borderHex = "#005a96";
                    break;
                default:
                    borderHex = "#cbd5e1";
                    break;
            }
            String textHex = seat.getSeatClass() == Seat.SeatClass.ECONOMY ? "#334155" : "#ffffff";
            btn.setStyle(String.format("%s -fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 0 0 4 0; -fx-text-fill: %s;", SEAT_BASE_STYLE, hexColor, borderHex, textHex));
        }
    }

    private void updatePriceSummary() {
        if (selectedSeats.isEmpty()) {
            priceLbl.setText("No seats selected");
            confirmBtn.setDisable(true);
            return;
        }
        double total = selectedSeats.stream().mapToDouble(Seat::getPrice).sum();
        String summary = selectedSeats.stream().map(Seat::getSeatNumber).sorted().collect(Collectors.joining(", "));
        priceLbl.setText(String.format("Seats: [%s]  ·  Total Core Fare: $%.2f", summary, total));
        confirmBtn.setDisable(false);
    }
}