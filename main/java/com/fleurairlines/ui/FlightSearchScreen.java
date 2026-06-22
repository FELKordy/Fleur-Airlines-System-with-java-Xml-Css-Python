package com.fleurairlines.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fleurairlines.model.Flight;
import com.fleurairlines.model.Passenger;
import com.fleurairlines.model.Seat;
import com.fleurairlines.service.FlightService;
import com.fleurairlines.util.DatabaseException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class FlightSearchScreen {

    private final Stage         stage;
    private final Passenger     passenger;
    private final FlightService flightService;
    private final VBox          resultsContainer = new VBox(16);
    private Flight              selectedOutboundFlight;
    private Flight              selectedReturnFlight;
    private Seat.SeatClass      selectedOutboundCabin;
    private Seat.SeatClass      selectedReturnCabin;
    private boolean             isReturnTrip = false;
    private final Label         selectionStatus = new Label("Select your outbound flight.");
    private final Button        continueBtn = new Button("Continue to Seats");

    public FlightSearchScreen(Stage stage, Passenger passenger) {
        this.stage     = stage;
        this.passenger = passenger;
        try {
            this.flightService = new FlightService();
        } catch (DatabaseException e) {
            throw new IllegalStateException("Critical Initialization Failure.", e);
        }
    }

    public Scene createScene(double width, double height) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");

        // ── Top Navigation & Progress Stepper ─────────────────────────────────
        VBox header = new VBox();
        HBox topNav = new HBox(16);
        topNav.setPadding(new Insets(20, 32, 10, 32));
        topNav.setAlignment(Pos.CENTER_LEFT);
        topNav.setStyle("-fx-background-color: #0f2b5c;");

        Label logo = new Label("✈  FLEUR AIRLINES");
        logo.setTextFill(Color.WHITE);
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button back = new Button("← Dashboard");
        back.setStyle("-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-radius: 4px; -fx-text-fill: white; -fx-cursor: hand;");
        back.setOnAction(e -> {
            try {
                SceneTransitionUtil.standardFade(stage, new PassengerDashboard(stage, passenger).createScene(width, height));
            } catch (DatabaseException e1) {
                new Alert(Alert.AlertType.ERROR, e1.getMessage()).showAndWait();
            }
        });
        topNav.getChildren().addAll(logo, spacer, back);

        HBox stepper = buildProgressStepper(1);
        header.getChildren().addAll(topNav, stepper);

        // ── Search Control Hub ───────────────────────────────────────────────
        VBox searchBarCard = new VBox(12);
        searchBarCard.setPadding(new Insets(24));
        searchBarCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        HBox rowInputs = new HBox(16);
        rowInputs.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> originBox = new ComboBox<>();
        ComboBox<String> destBox   = new ComboBox<>();
        originBox.getItems().addAll(airports());
        destBox.getItems().addAll(airports());
        originBox.setPromptText("Where from?");
        destBox.setPromptText("To");
        originBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4px;");
        destBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4px;");
        originBox.setPrefWidth(180);
        destBox.setPrefWidth(180);

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());

        DatePicker returnDatePicker = new DatePicker();
        returnDatePicker.setValue(LocalDate.now().plusDays(3));
        returnDatePicker.setPromptText("Return date");
        returnDatePicker.setPrefWidth(180);
        returnDatePicker.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4px;");
        returnDatePicker.setDisable(true);
        returnDatePicker.setVisible(false);

        ToggleGroup tripTypeGroup = new ToggleGroup();
        RadioButton oneWayRadio = new RadioButton("One-way");
        RadioButton returnRadio = new RadioButton("Return");
        oneWayRadio.setToggleGroup(tripTypeGroup);
        returnRadio.setToggleGroup(tripTypeGroup);
        oneWayRadio.setSelected(true);
        oneWayRadio.setStyle("-fx-text-fill: #0f2b5c; -fx-font-weight: bold;");
        returnRadio.setStyle("-fx-text-fill: #0f2b5c; -fx-font-weight: bold;");

        HBox tripTypeRow = new HBox(12, oneWayRadio, returnRadio, returnDatePicker);
        tripTypeRow.setAlignment(Pos.CENTER_LEFT);

        Button searchBtn = new Button("Search");
        searchBtn.setDefaultButton(true);
        searchBtn.setStyle("-fx-background-color: #0073bc; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 32; -fx-background-radius: 24px; -fx-cursor: hand;");

        tripTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            isReturnTrip = newToggle == returnRadio;
            returnDatePicker.setDisable(!isReturnTrip);
            returnDatePicker.setVisible(isReturnTrip);
            if (!isReturnTrip) {
                selectedReturnFlight = null;
                selectedReturnCabin = null;
            }
            updateSelectionStatus();
        });

        rowInputs.getChildren().addAll(
                originBox, new Label("⇄"), destBox,
                datePicker,
                searchBtn
        );
        searchBarCard.getChildren().addAll(rowInputs, tripTypeRow);

        searchBtn.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            LocalDate selectedReturn = returnDatePicker.getValue();
            if (originBox.getValue() == null || destBox.getValue() == null || selectedDate == null) {
                new Alert(Alert.AlertType.WARNING, "Please specify origin, destination, and date.").showAndWait();
                return;
            }
            if (isReturnTrip && selectedReturn == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a return date for a round-trip booking.").showAndWait();
                return;
            }
            if (isReturnTrip && !selectedReturn.isAfter(selectedDate)) {
                new Alert(Alert.AlertType.WARNING, "Return date must be after the outbound date.").showAndWait();
                return;
            }
            String oCode = originBox.getValue().split(" ")[0];
            String dCode = destBox.getValue().split(" ")[0];
            selectedOutboundFlight = null;
            selectedReturnFlight = null;
            selectedOutboundCabin = null;
            selectedReturnCabin = null;
            continueBtn.setDisable(true);
            updateSelectionStatus();
            executeSearch(oCode, dCode, selectedDate, selectedReturn);
        });

        continueBtn.setDisable(true);
        continueBtn.setStyle("-fx-background-color: #0073bc; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 28; -fx-background-radius: 24px; -fx-cursor: hand;");
        continueBtn.setOnAction(e -> {
            if (!isReturnTrip) {
                SceneTransitionUtil.standardSlide(stage, new SeatSelectionScreen(stage, selectedOutboundFlight, passenger, selectedOutboundCabin).createScene(width, height));
            } else {
                SceneTransitionUtil.standardSlide(stage, new SeatSelectionScreen(stage, selectedOutboundFlight, selectedOutboundCabin, selectedReturnFlight, selectedReturnCabin, passenger).createScene(width, height));
            }
        });

        VBox layoutContainer = new VBox(24, searchBarCard, selectionStatus, resultsContainer, continueBtn);
        layoutContainer.setPadding(new Insets(32, 64, 32, 64));
        
        ScrollPane scroll = new ScrollPane(layoutContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.setTop(header);
        root.setCenter(scroll);
        root.setBottom(new FleurFooter());

        return new Scene(root, width, height);
    }

    private void executeSearch(String origin, String dest, LocalDate selectedDate, LocalDate returnDate) {
        resultsContainer.getChildren().clear();
        updateSelectionStatus();

        Label ribbonTitle = new Label("Flexible Dates - Lowest Fares");
        ribbonTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f2b5c; -fx-font-size: 14px;");
        
        HBox calendarRibbon = new HBox(8);
        calendarRibbon.setAlignment(Pos.CENTER_LEFT);
        
        for (int i = -3; i <= 3; i++) {
            LocalDate testDate = selectedDate.plusDays(i);
            VBox dayCard = buildDayPricingCard(origin, dest, testDate, testDate.equals(selectedDate));
            dayCard.setOnMouseClicked(e -> executeSearch(origin, dest, testDate, returnDate));
            calendarRibbon.getChildren().add(dayCard);
        }
        
        ScrollPane ribbonScroll = new ScrollPane(calendarRibbon);
        ribbonScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ribbonScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        VBox flightsList = new VBox(24);
        Label outboundSectionTitle = new Label("Outbound Flights");
        outboundSectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f2b5c;");
        flightsList.getChildren().add(outboundSectionTitle);

        try {
            List<Flight> flights = flightService.searchFlights(origin, dest, selectedDate.toString());
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
            
            if (flights.isEmpty()) {
                Label noFlights = new Label("No outbound flights found for this date. Please try another day.");
                noFlights.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
                flightsList.getChildren().add(noFlights);
            } else {
                for (Flight f : flights) {
                    double ecoPrice = f.findAvailableSeats(Seat.SeatClass.ECONOMY).stream().mapToDouble(Seat::getPrice).min().orElse(0.0);
                    double busPrice = f.findAvailableSeats(Seat.SeatClass.BUSINESS).stream().mapToDouble(Seat::getPrice).min().orElse(0.0);

                    if (ecoPrice > 0 || busPrice > 0) {
                        flightsList.getChildren().add(buildAegeanFlightCard(f, timeFmt, ecoPrice, busPrice, false));
                    }
                }
            }
        } catch (DatabaseException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }

        if (isReturnTrip && returnDate != null) {
            VBox returnFlightsList = new VBox(24);
            Label returnSectionTitle = new Label("Return Flights");
            returnSectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f2b5c;");
            returnFlightsList.getChildren().add(returnSectionTitle);

            try {
                List<Flight> returnFlights = flightService.searchFlights(dest, origin, returnDate.toString());
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
                
                if (returnFlights.isEmpty()) {
                    Label noFlights = new Label("No return flights found for the selected return date.");
                    noFlights.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
                    returnFlightsList.getChildren().add(noFlights);
                } else {
                    for (Flight f : returnFlights) {
                        double ecoPrice = f.findAvailableSeats(Seat.SeatClass.ECONOMY).stream().mapToDouble(Seat::getPrice).min().orElse(0.0);
                        double busPrice = f.findAvailableSeats(Seat.SeatClass.BUSINESS).stream().mapToDouble(Seat::getPrice).min().orElse(0.0);

                        if (ecoPrice > 0 || busPrice > 0) {
                            returnFlightsList.getChildren().add(buildAegeanFlightCard(f, timeFmt, ecoPrice, busPrice, true));
                        }
                    }
                }
            } catch (DatabaseException ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
            }
            resultsContainer.getChildren().addAll(ribbonTitle, ribbonScroll, flightsList, returnFlightsList);
        } else {
            resultsContainer.getChildren().addAll(ribbonTitle, ribbonScroll, flightsList);
        }
    }

    private HBox buildAegeanFlightCard(Flight flight, DateTimeFormatter timeFmt, double ecoPrice, double busPrice, boolean isReturn) {
        HBox card = new HBox(32);
        card.setPadding(new Insets(24));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        // Left Side: Flight Details
        VBox detailsBox = new VBox(8);
        HBox timesRow = new HBox(16);
        timesRow.setAlignment(Pos.CENTER_LEFT);
        
        VBox depBox = new VBox();
        Label depTime = new Label(flight.getDepartureTime().format(timeFmt));
        depTime.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f2b5c;");
        Label depCode = new Label(flight.getOrigin().getAirportCode());
        depCode.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
        depBox.getChildren().addAll(depTime, depCode);

        VBox durationBox = new VBox();
        durationBox.setAlignment(Pos.CENTER);
        Label duration = new Label(flight.getDurationFormatted());
        duration.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        Separator line = new Separator();
        line.setPrefWidth(80);
        Label direct = new Label("Direct");
        direct.setStyle("-fx-text-fill: #0073bc; -fx-font-size: 10px;");
        durationBox.getChildren().addAll(duration, line, direct);

        VBox arrBox = new VBox();
        Label arrTime = new Label(flight.getArrivalTime().format(timeFmt));
        arrTime.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f2b5c;");
        Label arrCode = new Label(flight.getDestination().getAirportCode());
        arrCode.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
        arrBox.getChildren().addAll(arrTime, arrCode);

        timesRow.getChildren().addAll(depBox, durationBox, arrBox);
        
        Label operatedBy = new Label("Operated by Fleur Airlines");
        operatedBy.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        Label legBadge = new Label(isReturn ? "RETURN" : "OUTBOUND");
        legBadge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12px; -fx-font-size: 11px;");
        detailsBox.getChildren().addAll(timesRow, operatedBy, legBadge);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right Side: Pricing Cards
        HBox pricingBox = new HBox(16);
        
        if (ecoPrice > 0) {
            VBox ecoCard = buildPriceCard("Economy", ecoPrice, "#0073bc");
            ecoCard.setOnMouseClicked(e -> {
                if (isReturn) {
                    selectedReturnFlight = flight;
                    selectedReturnCabin = Seat.SeatClass.ECONOMY;
                } else {
                    selectedOutboundFlight = flight;
                    selectedOutboundCabin = Seat.SeatClass.ECONOMY;
                }
                updateSelectionStatus();
            });
            pricingBox.getChildren().add(ecoCard);
        }
        if (busPrice > 0) {
            VBox busCard = buildPriceCard("Business", busPrice, "#0f2b5c");
            busCard.setOnMouseClicked(e -> {
                if (isReturn) {
                    selectedReturnFlight = flight;
                    selectedReturnCabin = Seat.SeatClass.BUSINESS;
                } else {
                    selectedOutboundFlight = flight;
                    selectedOutboundCabin = Seat.SeatClass.BUSINESS;
                }
                updateSelectionStatus();
            });
            pricingBox.getChildren().add(busCard);
        }

        card.getChildren().addAll(detailsBox, spacer, pricingBox);
        return card;
    }

    private void updateSelectionStatus() {
        if (!isReturnTrip) {
            if (selectedOutboundFlight == null || selectedOutboundCabin == null) {
                selectionStatus.setText("Select your outbound flight and cabin.");
                continueBtn.setDisable(true);
            } else {
                selectionStatus.setText(String.format("Outbound selected: %s %s (%s → %s).",
                        selectedOutboundFlight.getFlightNumber(), selectedOutboundCabin,
                        selectedOutboundFlight.getOrigin().getAirportCode(), selectedOutboundFlight.getDestination().getAirportCode()));
                continueBtn.setDisable(false);
            }
        } else {
            if (selectedOutboundFlight == null || selectedOutboundCabin == null) {
                selectionStatus.setText("Select your outbound flight and cabin.");
                continueBtn.setDisable(true);
            } else if (selectedReturnFlight == null || selectedReturnCabin == null) {
                selectionStatus.setText(String.format("Outbound selected: %s %s. Now select your return flight.",
                        selectedOutboundFlight.getFlightNumber(), selectedOutboundCabin));
                continueBtn.setDisable(true);
            } else {
                selectionStatus.setText(String.format("Round trip selected: %s %s outbound and %s %s return.",
                        selectedOutboundFlight.getFlightNumber(), selectedOutboundCabin,
                        selectedReturnFlight.getFlightNumber(), selectedReturnCabin));
                continueBtn.setDisable(false);
            }
        }
    }

    private VBox buildPriceCard(String title, double price, String colorHex) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(140, 100);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: " + colorHex + "; -fx-border-width: 1 1 4 1; -fx-border-radius: 4px; -fx-cursor: hand;");
        
        Label tLbl = new Label(title);
        tLbl.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 14px;");
        Label fromLbl = new Label("from");
        fromLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        Label pLbl = new Label(String.format("€ %.2f", price));
        pLbl.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        card.getChildren().addAll(tLbl, fromLbl, pLbl);
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-border-color: " + colorHex + "; -fx-border-width: 1 1 4 1; -fx-border-radius: 4px; -fx-cursor: hand;"));
        card.setOnMouseExited(e  -> card.setStyle("-fx-background-color: #ffffff; -fx-border-color: " + colorHex + "; -fx-border-width: 1 1 4 1; -fx-border-radius: 4px; -fx-cursor: hand;"));
        
        return card;
    }

    private VBox buildDayPricingCard(String origin, String dest, LocalDate date, boolean isSelected) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(120, 70);
        
        String bg = isSelected ? "-fx-background-color: #ffffff; -fx-border-color: #0073bc; -fx-border-width: 0 0 3 0;" : "-fx-background-color: #ffffff; -fx-border-color: transparent;";
        card.setStyle(bg + " -fx-cursor: hand;");
        
        Label dateLbl = new Label(date.format(DateTimeFormatter.ofPattern("EEE, d MMM")));
        dateLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f2b5c; -fx-font-size: 12px;");
        
        Label priceLbl = new Label("—");
        priceLbl.setStyle("-fx-text-fill: #0073bc; -fx-font-size: 14px; -fx-font-weight: bold;");

        try {
            List<Flight> flights = flightService.searchFlights(origin, dest, date.toString());
            double minPrice = Double.MAX_VALUE;
            for (Flight f : flights) {
                double p = f.findAvailableSeats().stream().mapToDouble(Seat::getPrice).min().orElse(Double.MAX_VALUE);
                if (p < minPrice) minPrice = p;
            }
            if (minPrice != Double.MAX_VALUE) {
                priceLbl.setText(String.format("from €%.0f", minPrice));
            } else {
                priceLbl.setText("-");
                priceLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
            }
        } catch (DatabaseException ignored) {
            // The pricing ribbon may fail if the flight search is unavailable.
        }

        card.getChildren().addAll(dateLbl, priceLbl);
        return card;
    }

    public static HBox buildProgressStepper(int currentStep) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(12, 0, 12, 0));
        box.setStyle("-fx-background-color: #0f2b5c; -fx-border-color: #1e3a8a; -fx-border-width: 1 0 0 0;");

        String[] steps = {"1. Flights", "2. Seats", "3. Extras", "4. Checkout"};
        
        for (int i = 0; i < steps.length; i++) {
            Label lbl = new Label(steps[i]);
            if (i + 1 == currentStep) {
                lbl.setStyle("-fx-text-fill: #0f2b5c; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-color: #ffffff; -fx-padding: 4 12; -fx-background-radius: 12px;");
            } else {
                lbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: bold; -fx-font-size: 13px;");
            }
            box.getChildren().add(lbl);
            if (i < steps.length - 1) {
                Label line = new Label("——");
                line.setStyle("-fx-text-fill: #cbd5e1;");
                box.getChildren().add(line);
            }
        }
        return box;
    }

    private List<String> airports() { return List.of("HBE (Alexandria)", "CAI (Cairo)", "CDG (Paris)", "DXB (Dubai)", "LHR (London)", "JFK (New York)", "BCN (Barcelona)", "FCO (Rome)"); }
}