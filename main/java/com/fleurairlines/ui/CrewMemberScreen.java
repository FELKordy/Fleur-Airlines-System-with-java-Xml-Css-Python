package com.fleurairlines.ui;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fleurairlines.model.Flight;
import com.fleurairlines.pattern.SessionManager;
import com.fleurairlines.service.FlightService;
import com.fleurairlines.util.DatabaseException;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
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
import javafx.util.Duration;

public class CrewMemberScreen {

    public static final class CrewMember {
        private final String id, name, role;
        private int daysOffLeft;
        private final double overallRating;

        public CrewMember(String id, String name, String role, int daysOffLeft, double overallRating) {
            this.id = id; this.name = name; this.role = role;
            this.daysOffLeft = daysOffLeft; this.overallRating = overallRating;
        }
        public String getId() { return id; }
        public String getName() { return name; }
        public String getRole() { return role; }
        public int getDaysOffLeft() { return daysOffLeft; }
        public double getOverallRating() { return overallRating; }
        public void setDaysOffLeft(int d) { this.daysOffLeft = d; }
    }

    private static final class CrewFlightRow {
        final String flightNumber, route, departure, arrival, status;
        final String gateNumber, airportFullName, temperatureCelsius, uniform;

        CrewFlightRow(String fn, String rt, String dep, String arr, String st, String gate, String apt, String temp, String uni) {
            flightNumber = fn; route = rt; departure = dep; arrival = arr; status = st;
            gateNumber = gate; airportFullName = apt; temperatureCelsius = temp; uniform = uni;
        }
    }

    private final Stage stage;
    private final CrewMember crewMember;
    private final FlightService flightService;
    
    // FIX: Track this label specifically so we can update it on swipe
    private Label daysOffLabel;

    public CrewMemberScreen(Stage stage, CrewMember crewMember) {
        this.stage = stage;
        this.crewMember = crewMember;
        FlightService fs = null;
        try { fs = new FlightService(); } catch (DatabaseException ignored) {}
        this.flightService = fs;
    }

    public Scene createScene(double width, double height) throws DatabaseException {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");

        // ── Top Navigation ───────────────────────────────────────────────────
        HBox topNav = new HBox(16);
        topNav.setPadding(new Insets(20, 32, 20, 32));
        topNav.setAlignment(Pos.CENTER_LEFT);
        topNav.setStyle("-fx-background-color: #0f2b5c;");

        Label logo = new Label("✈ FLEUR AIRLINES | STAFF PORTAL");
        logo.setTextFill(Color.WHITE);
        logo.setFont(Font.font("System", FontWeight.BOLD, 16));
        logo.setStyle("-fx-letter-spacing: 1px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label roleLbl = new Label(crewMember.getRole() + " • " + crewMember.getName());
        roleLbl.setTextFill(Color.web("#cbd5e1"));
        roleLbl.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-radius: 4px; -fx-text-fill: white; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            SessionManager.getInstance().logout();
            SceneTransitionUtil.standardFade(stage, new LoginScreen(stage).createScene(width, height));
        });

        topNav.getChildren().addAll(logo, spacer, roleLbl, logoutBtn);

        // ── Content ──────────────────────────────────────────────────────────
        VBox contentBox = new VBox(32);
        contentBox.setPadding(new Insets(40));
        
        contentBox.getChildren().addAll(
            buildSummaryRow(),
            buildNextFlightBox(),
            buildFlightsList()
        );

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setTop(topNav);
        root.setCenter(scroll);
        root.setBottom(new FleurFooter());

        return new Scene(root, width, height);
    }

    private HBox buildSummaryRow() {
        HBox row = new HBox(24);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox daysCard = new VBox(8);
        daysCard.setPadding(new Insets(24));
        daysCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        daysCard.setPrefWidth(260);
        Label daysTitle = new Label("Remaining Days Off");
        daysTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");
        
        // FIX: Assign to our class-level variable
        daysOffLabel = new Label(String.valueOf(crewMember.getDaysOffLeft()));
        daysOffLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #0073bc;");
        daysCard.getChildren().addAll(daysTitle, daysOffLabel);

        VBox ratingCard = new VBox(8);
        ratingCard.setPadding(new Insets(24));
        ratingCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        ratingCard.setPrefWidth(260);
        Label ratingTitle = new Label("Performance Rating");
        ratingTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");
        Label ratingNum = new Label(String.format("%.1f", crewMember.getOverallRating()) + " / 5.0");
        ratingNum.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #0f2b5c;");
        ratingCard.getChildren().addAll(ratingTitle, ratingNum);

        row.getChildren().addAll(daysCard, ratingCard);
        return row;
    }

    private VBox buildNextFlightBox() {
        CrewFlightRow nextFlight = getNextFlightData();
        VBox box = new VBox(16);
        box.setStyle("-fx-background-color: #0f2b5c; -fx-background-radius: 8px; -fx-padding: 32px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 8);");

        Label header = new Label("✈ Your Upcoming Assignment");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        if (nextFlight == null) {
            Label noNext = new Label("No pending rosters at this time.");
            noNext.setStyle("-fx-text-fill: #cbd5e1;");
            box.getChildren().addAll(header, noNext);
            return box;
        }

        HBox detailsRow = new HBox(40);
        detailsRow.getChildren().addAll(
            nextFlightDetail("Flight", nextFlight.flightNumber),
            nextFlightDetail("Sector", nextFlight.route),
            nextFlightDetail("Departure", nextFlight.departure),
            nextFlightDetail("Status", nextFlight.status)
        );

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #1e3a8a; -fx-border-width: 0;");

        HBox extraRow = new HBox(40);
        extraRow.getChildren().addAll(
            nextFlightDetail("Base Airport", nextFlight.airportFullName),
            nextFlightDetail("Assigned Gate", nextFlight.gateNumber),
            nextFlightDetail("Arrival", nextFlight.arrival),
            nextFlightDetail("Cabin Temp", nextFlight.temperatureCelsius),
            nextFlightDetail("Uniform Spec", nextFlight.uniform)
        );

        box.getChildren().addAll(header, detailsRow, sep, extraRow);
        return box;
    }

    private VBox nextFlightDetail(String label, String value) {
        VBox v = new VBox(4);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold;");
        v.getChildren().addAll(lbl, val);
        return v;
    }

    private VBox buildFlightsList() {
        VBox list = new VBox(12);
        Label header = new Label("Roster Schedule");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f2b5c;");
        list.getChildren().add(header);

        List<CrewFlightRow> flights = getAssignedFlights();
        for (CrewFlightRow flight : flights) {
            list.getChildren().add(buildSwipeableFlightRow(flight, list));
        }
        return list;
    }

    private StackPane buildSwipeableFlightRow(CrewFlightRow flight, VBox parentList) {
        HBox cancelBg = new HBox();
        cancelBg.setAlignment(Pos.CENTER_RIGHT);
        cancelBg.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 8px; -fx-padding: 0 24 0 0;");
        Label cancelLbl = new Label("Drop Shift");
        cancelLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        cancelBg.getChildren().add(cancelLbl);

        HBox card = new HBox(24);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        Label fnLbl = new Label(flight.flightNumber);
        fnLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f2b5c; -fx-font-size: 16px; -fx-min-width: 80px;");
        
        Label routeLbl = new Label(flight.route);
        routeLbl.setStyle("-fx-text-fill: #475569; -fx-min-width: 120px; -fx-font-weight: bold;");

        Label depLbl = new Label(flight.departure);
        depLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLbl = new Label(flight.status);
        statusLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (flight.status.equals("SCHEDULED") ? "#0073bc" : "#64748b") + ";");

        Label swipeHint = new Label("← Swipe to drop");
        swipeHint.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");

        card.getChildren().addAll(fnLbl, routeLbl, depLbl, spacer, statusLbl, swipeHint);

        StackPane stack = new StackPane(cancelBg, card);
        stack.setAlignment(Pos.CENTER_LEFT);

        final double[] startX = {0};
        final boolean[] swiped = {false};

        card.setOnMousePressed(e -> startX[0] = e.getSceneX());
        card.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - startX[0];
            if (dx < 0) card.setTranslateX(Math.max(dx, -160));
        });
        card.setOnMouseReleased(e -> {
            double dx = e.getSceneX() - startX[0];
            TranslateTransition anim = new TranslateTransition(Duration.millis(200), card);
            if (dx < -100) {
                anim.setToX(-160);
                swiped[0] = true;
            } else {
                anim.setToX(0);
                swiped[0] = false;
            }
            anim.play();
        });

        cancelBg.setOnMouseClicked(e -> {
            if (swiped[0]) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Dropping this shift costs 1 day off. You have " + crewMember.getDaysOffLeft() + " left. Proceed?");
                confirm.showAndWait().ifPresent(res -> {
                    if (res == javafx.scene.control.ButtonType.OK) {
                        if (crewMember.getDaysOffLeft() > 0) {
                            
                            // FIX: Deduct the day off and explicitly update the UI Label text
                            crewMember.setDaysOffLeft(crewMember.getDaysOffLeft() - 1);
                            daysOffLabel.setText(String.valueOf(crewMember.getDaysOffLeft()));
                            
                            parentList.getChildren().remove(stack);
                        } else {
                            new Alert(Alert.AlertType.WARNING, "Insufficient days off balance.").showAndWait();
                        }
                    }
                });
                TranslateTransition anim = new TranslateTransition(Duration.millis(200), card);
                anim.setToX(0); anim.play(); swiped[0] = false;
            }
        });

        return stack;
    }

    private List<CrewFlightRow> getAssignedFlights() {
        List<CrewFlightRow> rows = new ArrayList<>();
        if (flightService != null) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                for (Flight f : flightService.getAllFlights()) {
                    rows.add(new CrewFlightRow(
                        f.getFlightNumber(),
                        f.getOrigin().getAirportCode() + " → " + f.getDestination().getAirportCode(),
                        f.getDepartureTime().format(fmt), f.getArrivalTime().format(fmt),
                        f.getStatus(), "Gate 12", f.getDestination().getName(), "24", "Standard Corporate"
                    ));
                }
            } catch (DatabaseException ignored) {}
        }
        return limitFlightsPerDay(rows);
    }

    private List<CrewFlightRow> limitFlightsPerDay(List<CrewFlightRow> rows) {
        Map<String, Integer> dailyCount = new LinkedHashMap<>();
        List<CrewFlightRow> filtered = new ArrayList<>();
        for (CrewFlightRow row : rows) {
            String dateKey = row.departure.split(" ")[0];
            int count = dailyCount.getOrDefault(dateKey, 0);
            if (count < 3) { filtered.add(row); dailyCount.put(dateKey, count + 1); }
        }
        return filtered;
    }

    private CrewFlightRow getNextFlightData() {
        for (CrewFlightRow r : getAssignedFlights()) {
            if ("SCHEDULED".equals(r.status)) return r;
        }
        return getAssignedFlights().isEmpty() ? null : getAssignedFlights().get(0);
    }
}