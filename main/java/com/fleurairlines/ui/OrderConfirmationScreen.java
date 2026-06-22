package com.fleurairlines.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fleurairlines.model.Flight;
import com.fleurairlines.model.MealType;
import com.fleurairlines.model.Passenger;
import com.fleurairlines.model.Seat;
import com.fleurairlines.service.BookingService;
import com.fleurairlines.util.DatabaseException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

public class OrderConfirmationScreen {

    private final Stage stage;
    private final Flight flight;
    private final Passenger passenger;
    private final List<OrderTicket> tickets;
    private final MealType mealType;
    private final String drinkSelection;
    private final int extraBagCount;
    private final boolean hasPet;
    private final double extrasCost;
    private final BookingService bookingService;

    public OrderConfirmationScreen(Stage stage, Flight flight, Passenger passenger,
                                   List<OrderTicket> tickets, MealType mealType,
                                   String drinkSelection, int extraBagCount,
                                   boolean hasPet, double extrasCost) {
        this.stage = stage;
        this.flight = flight;
        this.passenger = passenger;
        this.tickets = tickets;
        this.mealType = mealType;
        this.drinkSelection = drinkSelection;
        this.extraBagCount = extraBagCount;
        this.hasPet = hasPet;
        this.extrasCost = extrasCost;
        try {
            this.bookingService = new BookingService();
        } catch (DatabaseException e) {
            throw new IllegalStateException(e);
        }
    }

    public Scene createScene(double width, double height) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");

        // ── Top Navigation & Stepper ─────────────────────────────────────────
        VBox header = new VBox();
        HBox topBox = new HBox(16);
        topBox.setPadding(new Insets(20, 24, 10, 24));
        topBox.setStyle("-fx-background-color: #0f2b5c;");
        topBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("✈ FLEUR AIRLINES | CHECKOUT");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("← Modify Extras");
        backBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-radius: 4px; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            List<Seat> seats = new java.util.ArrayList<>();
            tickets.forEach(t -> seats.add(t.getSeat()));
            SceneTransitionUtil.standardFade(stage, new BookingExtrasScreen(stage, flight, passenger, seats).createScene(width, height));
        });

        topBox.getChildren().addAll(title, spacer, backBtn);
        header.getChildren().addAll(topBox, FlightSearchScreen.buildProgressStepper(4));

        // ── Main Content (Kiwi.com Style) ────────────────────────────────────
        VBox content = new VBox(24);
        content.setPadding(new Insets(40));
        content.setMaxWidth(850);
        content.setAlignment(Pos.TOP_CENTER);

        // Header Title
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label saverBadge = new Label("Saver Ticket");
        saverBadge.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 12px; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        Label routeHeader = new Label(flight.getOrigin().getName() + " → " + flight.getDestination().getName());
        routeHeader.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        titleRow.getChildren().addAll(saverBadge);
        
        VBox headerBox = new VBox(8, titleRow, routeHeader, new Label("OUTBOUND    Total duration: " + flight.getDurationFormatted()));
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Segment Card
        VBox segmentCard = buildSegmentCard();
        
        // Passengers Card
        VBox passengersCard = buildPassengersCard();

        // Additional Info
        VBox infoCards = buildInfoCards();

        // Total & Confirm
        HBox confirmRow = buildConfirmRow();

        content.getChildren().addAll(headerBox, segmentCard, passengersCard, infoCards, confirmRow);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        StackPane centerContainer = new StackPane(scroll);
        centerContainer.setAlignment(Pos.TOP_CENTER);

        root.setTop(header);
        root.setCenter(centerContainer);
        root.setBottom(new FleurFooter());

        return new Scene(root, width, height);
    }

    private VBox buildSegmentCard() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        card.setPadding(new Insets(24));

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm (hh:mm a)");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy");

        // Departure Row
        HBox depRow = new HBox(20);
        VBox depTimeBox = new VBox(2);
        depTimeBox.setMinWidth(120);
        Label depTime = new Label(flight.getDepartureTime().format(timeFmt));
        depTime.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label depDate = new Label(flight.getDepartureTime().format(dateFmt));
        depDate.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        depTimeBox.getChildren().addAll(depTime, depDate);

        VBox depLocBox = new VBox(2);
        Label depCode = new Label(flight.getOrigin().getAirportCode() + " " + flight.getOrigin().getLocation());
        depCode.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label depName = new Label(flight.getOrigin().getName());
        depName.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        depLocBox.getChildren().addAll(depCode, depName);
        
        Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        VBox carrierBox = new VBox(4);
        carrierBox.setAlignment(Pos.TOP_RIGHT);
        Label carrier = new Label("✈ Carrier: Fleur Airlines");
        carrier.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label fNum = new Label("Flight no: " + flight.getFlightNumber());
        fNum.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
        Label fDur = new Label("Duration: " + flight.getDurationFormatted());
        fDur.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
        carrierBox.getChildren().addAll(carrier, fNum, fDur);

        depRow.getChildren().addAll(depTimeBox, createTimelineDot(), depLocBox, spacer1, carrierBox);

        // Timeline connector
        HBox connectorRow = new HBox(20);
        Region offset = new Region(); offset.setMinWidth(120);
        VBox line = new VBox();
        line.setMinWidth(2); line.setMaxWidth(2); line.setMinHeight(40);
        line.setStyle("-fx-background-color: #0073bc;");
        HBox.setMargin(line, new Insets(0, 0, 0, 4));
        connectorRow.getChildren().addAll(offset, line);

        // Arrival Row
        HBox arrRow = new HBox(20);
        VBox arrTimeBox = new VBox(2);
        arrTimeBox.setMinWidth(120);
        Label arrTime = new Label(flight.getArrivalTime().format(timeFmt));
        arrTime.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label arrDate = new Label(flight.getArrivalTime().format(dateFmt));
        arrDate.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        arrTimeBox.getChildren().addAll(arrTime, arrDate);

        VBox arrLocBox = new VBox(2);
        Label arrCode = new Label(flight.getDestination().getAirportCode() + " " + flight.getDestination().getLocation());
        arrCode.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label arrName = new Label(flight.getDestination().getName());
        arrName.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        arrLocBox.getChildren().addAll(arrCode, arrName);

        arrRow.getChildren().addAll(arrTimeBox, createTimelineDot(), arrLocBox);

        card.getChildren().addAll(depRow, connectorRow, arrRow);
        return card;
    }

    private Label createTimelineDot() {
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: #0073bc; -fx-font-size: 10px;");
        return dot;
    }

    private VBox buildPassengersCard() {
        VBox container = new VBox(12);
        Label title = new Label("Passengers");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        container.getChildren().add(title);

        for (OrderTicket ticket : tickets) {
            HBox row = new HBox(16);
            row.setPadding(new Insets(16));
            row.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px;");
            row.setAlignment(Pos.CENTER_LEFT);

            VBox nameBox = new VBox(4);
            Label name = new Label("👤 " + ticket.getName() + "  (Age " + ticket.getAge() + ")");
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
            Label details = new Label("Passport: " + ticket.getPassport() + " | Seat: " + ticket.getSeat().getSeatNumber() + " (" + ticket.getSeat().getSeatClass() + ")");
            details.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
            nameBox.getChildren().addAll(name, details);

            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

            VBox bagBox = new VBox(4);
            bagBox.setAlignment(Pos.CENTER_RIGHT);
            Label carryOn = new Label("👜 1x personal item (10kg)");
            carryOn.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
            bagBox.getChildren().add(carryOn);
            
            if (extraBagCount > 0) {
                Label checkedBag = new Label("🧳 " + extraBagCount + "x checked bag (23kg)");
                checkedBag.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px; -fx-font-weight: bold;");
                bagBox.getChildren().add(checkedBag);
            }

            row.getChildren().addAll(nameBox, spacer, bagBox);
            container.getChildren().add(row);
        }
        return container;
    }

    private VBox buildInfoCards() {
        VBox container = new VBox(12);
        Label title = new Label("Additional Information");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        container.getChildren().add(title);

        container.getChildren().addAll(
            infoCard("ℹ Fare conditions", "Rebooking is not allowed on Saver Tickets. If you want to change your trip, you will have to make a new booking and pay full price."),
            infoCard("🍲 Meals & Drinks", "Selected Meal: " + mealType.getDisplayName() + ". Selected Beverage: " + drinkSelection + ". Note: Liquids must comply with 100ml aviation limits."),
            infoCard(hasPet ? "🐾 Pet Carrier Approved" : "🐾 No Pets Authorized", hasPet ? "You have purchased an in-cabin pet allowance. Ensure your carrier fits under the seat." : "No pets are registered for this manifest.")
        );
        return container;
    }

    private VBox infoCard(String title, String text) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        Label tLbl = new Label(title);
        tLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f2b5c;");
        Label textLbl = new Label(text);
        textLbl.setWrapText(true);
        textLbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px; -fx-line-spacing: 4px;");
        card.getChildren().addAll(tLbl, textLbl);
        return card;
    }

    private HBox buildConfirmRow() {
        HBox row = new HBox(24);
        row.setPadding(new Insets(24));
        row.setStyle("-fx-background-color: #fffbeb; -fx-border-color: #fcd34d; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        row.setAlignment(Pos.CENTER_LEFT);

        double ticketsTotal = tickets.stream().mapToDouble(OrderTicket::getPrice).sum();
        double orderTotal = ticketsTotal + extrasCost;

        VBox totalsBox = new VBox(4);
        Label totalLbl = new Label("Total to Pay (15-Day Office Remittance):");
        totalLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #92400e;");
        Label totalVal = new Label(String.format("$%.2f", orderTotal));
        totalVal.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #b45309;");
        totalsBox.getChildren().addAll(totalLbl, totalVal);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button confirmBtn = new Button("Confirm & Generate Booking Ticket");
        confirmBtn.setDefaultButton(true);
        confirmBtn.setStyle("-fx-background-color: #0f2b5c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 16px 32px; -fx-background-radius: 6px; -fx-cursor: hand;");
        confirmBtn.setOnAction(e -> processConfirmation(orderTotal));

        row.getChildren().addAll(totalsBox, spacer, confirmBtn);
        return row;
    }

    private void processConfirmation(double orderTotal) {
        try {
            int passId = Integer.parseInt(passenger.getId());
            boolean applyExtrasToFirstTicket = true;
            
            for (OrderTicket ticket : tickets) {
                double ticketPrice = ticket.getPrice();
                if (applyExtrasToFirstTicket) {
                    ticketPrice += extrasCost;
                    applyExtrasToFirstTicket = false;
                }
                bookingService.createBooking(passId, flight.getId(), ticket.getSeat().getId(), ticket.getSeat().getSeatClass(), ticketPrice);
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Booking Confirmed");
            alert.setHeaderText("Your itinerary has been successfully secured!");
            alert.setContentText(String.format("Please remember to visit a Fleur Airlines office within 15 days to finalize your payment of $%.2f.", orderTotal));
            alert.showAndWait();
            
            SceneTransitionUtil.standardFade(stage, new MyBookingsScreen(stage, passenger).createScene(stage.getWidth(), stage.getHeight()));

        } catch (DatabaseException ex) {
            new Alert(Alert.AlertType.ERROR, "System Error: " + ex.getMessage()).showAndWait();
        }
    }

    public static final class OrderTicket {
        private final Seat seat;
        private final String name;
        private final int age;
        private final String passport;
        private final double price;

        public OrderTicket(Seat seat, String name, int age, String passport, double price) {
            this.seat = seat;
            this.name = name;
            this.age = age;
            this.passport = passport;
            this.price = price;
        }

        public Seat getSeat() { return seat; }
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getPassport() { return passport; }
        public double getPrice() { return price; }
    }
}