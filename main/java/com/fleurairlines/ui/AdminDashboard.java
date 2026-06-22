package com.fleurairlines.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fleurairlines.model.Admin;
import com.fleurairlines.model.Aircraft;
import com.fleurairlines.model.Airport;
import com.fleurairlines.model.Flight;
import com.fleurairlines.model.Passenger;
import com.fleurairlines.model.Reservation;
import com.fleurairlines.pattern.SessionManager;
import com.fleurairlines.service.BookingService;
import com.fleurairlines.service.FlightService;
import com.fleurairlines.util.DatabaseException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class AdminDashboard {

    private final Stage          stage;
    private final Admin          admin;
    private final FlightService  flightService;
    private final BookingService bookingService;

    private static final String NAV_BLUE   = "#1a2744";
    private static final String GOLD       = "#c9a84c";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String BTN_STYLE  =
            "-fx-background-color: " + NAV_BLUE + "; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 8 16;";
    private static final String FIELD_STYLE =
            "-fx-border-color: " + NAV_BLUE + "; -fx-border-radius: 4px; " +
            "-fx-background-radius: 4px; -fx-padding: 6px;";

    public AdminDashboard(Stage stage, Admin admin) {
        this.stage = stage;
        this.admin = admin;
        try {
            this.flightService  = new FlightService();
            this.bookingService = new BookingService();
        } catch (DatabaseException e) {
            throw new IllegalStateException(e);
        }
    }

    public Scene createScene(double width, double height) throws DatabaseException {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #e8ecf5;");

        HBox topNav = new HBox(16);
        topNav.setPadding(new Insets(14));
        topNav.setAlignment(Pos.CENTER_LEFT);
        topNav.setStyle("-fx-background-color: " + NAV_BLUE + ";");

        Label logo = new Label("✈  Fleur Airlines  |  Admin Panel");
        logo.setTextFill(Color.web(GOLD));
        logo.setFont(Font.font(18));
        logo.setStyle("-fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label adminLbl = new Label(admin.getName() + "  ·  " + admin.getDepartment()
                + "  ·  " + (admin.isSuperAdmin() ? "Super Admin" : "Standard Admin"));
        adminLbl.setTextFill(Color.WHITE);

        Button logout = new Button("Logout");
        logout.setStyle("-fx-background-color: " + GOLD + "; -fx-text-fill: " + NAV_BLUE +
                "; -fx-font-weight: bold; -fx-background-radius: 6px;");
        logout.setOnAction(e -> {
            SessionManager.getInstance().logout();
            SceneTransitionUtil.standardFade(stage, new LoginScreen(stage).createScene(width, height));
        });

        topNav.getChildren().addAll(logo, spacer, adminLbl, logout);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-tab-min-width: 120px;");

        Tab flightsTab    = new Tab("✈  Flights");
        Tab bookingsTab   = new Tab("📋  Bookings");
        Tab passengersTab = new Tab("👥  Passengers");

        flightsTab.setContent(buildFlightsTab(width, height));
        bookingsTab.setContent(buildBookingsTab());
        passengersTab.setContent(buildPassengersTab());

        tabs.getTabs().addAll(flightsTab, bookingsTab, passengersTab);

        root.setTop(topNav);
        root.setCenter(tabs);
        root.setBottom(new FleurFooter());

        return new Scene(root, width, height);
    }

    private VBox buildFlightsTab(double width, double height) {
        TableView<Flight> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No flights found"));

        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        TableColumn<Flight, String> noCol = new TableColumn<>("Flight No");
        noCol.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));

        TableColumn<Flight, String> origCol = new TableColumn<>("Origin");
        origCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getOrigin() != null ? c.getValue().getOrigin().getAirportCode() : ""));

        TableColumn<Flight, String> destCol = new TableColumn<>("Destination");
        destCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDestination() != null ? c.getValue().getDestination().getAirportCode() : ""));

        TableColumn<Flight, String> depCol = new TableColumn<>("Departure");
        depCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDepartureTime() != null ? c.getValue().getDepartureTime().format(dateTimeFmt) : ""));

        TableColumn<Flight, String> arrCol = new TableColumn<>("Arrival");
        arrCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getArrivalTime() != null ? c.getValue().getArrivalTime().format(dateTimeFmt) : ""));

        TableColumn<Flight, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "SCHEDULED" -> setStyle("-fx-text-fill: #1e7e34; -fx-font-weight: bold;");
                    case "CANCELLED" -> setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
                    case "DELAYED"   -> setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    case "DEPARTED"  -> setStyle("-fx-text-fill: #0056b3; -fx-font-weight: bold;");
                    default          -> setStyle("");
                }
            }
        });

        TableColumn<Flight, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button changeBtn = new Button("Change Status");
            private final Button detailsBtn = new Button("Details");
            private final HBox btnContainer = new HBox(8);
            {
                changeBtn.setStyle(BTN_STYLE + " -fx-font-size: 11px;");
                detailsBtn.setStyle(BTN_STYLE + " -fx-font-size: 11px;");
                btnContainer.setAlignment(Pos.CENTER_LEFT);
                btnContainer.getChildren().addAll(changeBtn, detailsBtn);

                changeBtn.setOnAction(e -> {
                    Flight f = getTableView().getItems().get(getIndex());
                    if (f == null) return;

                    if ("ARRIVED".equals(f.getStatus()) || "CANCELLED".equals(f.getStatus())) {
                        new Alert(Alert.AlertType.WARNING,
                                "Flight " + f.getFlightNumber() + " is in a terminal state (" +
                                f.getStatus() + ") and its condition cannot be changed.").showAndWait();
                        return;
                    }
                    ChoiceDialog<String> dialog = new ChoiceDialog<>(f.getStatus(),
                            "SCHEDULED", "BOARDING", "DEPARTED", "ARRIVED", "CANCELLED", "DELAYED");
                    dialog.setTitle("Change Flight Status");
                    dialog.setHeaderText("Flight: " + f.getFlightNumber());
                    dialog.setContentText("Select new status:");
                    dialog.showAndWait().ifPresent(val -> {
                        try {
                            flightService.updateFlightStatus(f.getFlightNumber(), val);
                            refreshFlightTable(table);
                        } catch (DatabaseException ex) {
                            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
                        }
                    });
                });

                detailsBtn.setOnAction(e -> {
                    Flight f = getTableView().getItems().get(getIndex());
                    if (f == null) return;

                    java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    Dialog<Void> details = new Dialog<>();
                    details.setTitle("Flight Details");
                    details.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

                    VBox content = new VBox(8);
                    content.setPadding(new Insets(12));
                    content.getChildren().addAll(
                        new Label("Flight: " + f.getFlightNumber()),
                        new Label("Route: " + (f.getOrigin() != null ? f.getOrigin().getAirportCode() : "") + " → " + (f.getDestination() != null ? f.getDestination().getAirportCode() : "")),
                        new Label("Departure: " + (f.getDepartureTime() != null ? f.getDepartureTime().format(fmt) : "N/A")),
                        new Label("Arrival: " + (f.getArrivalTime() != null ? f.getArrivalTime().format(fmt) : "N/A"))
                    );

                    details.getDialogPane().setContent(content);
                    details.showAndWait();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                setGraphic(btnContainer);
            }
        });

        @SuppressWarnings("unchecked")
        TableColumn<Flight, ?>[] flightCols = new TableColumn[]{noCol, origCol, destCol, depCol, arrCol, statusCol, actionCol};
        table.getColumns().addAll(flightCols);
        refreshFlightTable(table);

        Button addBtn = new Button("+ Add Flight");
        addBtn.setStyle(BTN_STYLE);
        addBtn.setOnAction(e -> showAddFlightDialog(table));

        HBox toolbar = new HBox(12, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(0, 0, 8, 0));

        VBox box = new VBox(12, toolbar, table);
        box.setPadding(new Insets(18));
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private void showAddFlightDialog(TableView<Flight> table) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add New Flight");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Add Flight");
        okButton.setStyle(BTN_STYLE);

        TextField flightNumField = styledField("e.g. CA007");

        // FIX: The Admin panel MUST format origins/destinations with the EXACT 3-letter codes used in FlightSearchScreen
        String[] airportList = {
            "CAI (Cairo)", "CDG (Paris)", "DXB (Dubai)", "LHR (London)", 
            "JFK (New York)", "ATH (Athens)", "FCO (Rome)"
        };

        ComboBox<String> originBox = new ComboBox<>();
        originBox.getItems().addAll(airportList);
        originBox.setPromptText("Select Origin");
        originBox.setMaxWidth(300);

        ComboBox<String> destBox = new ComboBox<>();
        destBox.getItems().addAll(airportList);
        destBox.setPromptText("Select Destination");
        destBox.setMaxWidth(300);

        String[] aircraftList = {
                "Aircraft-001 (Boeing 737, 180 seats)",
                "Aircraft-002 (Airbus A320, 180 seats)",
                "Aircraft-003 (Boeing 777, 300 seats)"
        };
        ComboBox<String> aircraftBox = new ComboBox<>();
        aircraftBox.getItems().addAll(aircraftList);
        aircraftBox.setPromptText("Select Aircraft");
        aircraftBox.setMaxWidth(300);

        HBox depDateRow = buildDateTimeRow("Departure");
        ComboBox<Integer>  depYear   = getYearBox(depDateRow);
        ComboBox<String>   depMonth  = getMonthBox(depDateRow);
        ComboBox<Integer>  depDay    = getDayBox(depDateRow);
        ComboBox<String>   depTime   = getTimeBox(depDateRow);

        HBox arrDateRow = buildDateTimeRow("Arrival");
        ComboBox<Integer>  arrYear   = getYearBox(arrDateRow);
        ComboBox<String>   arrMonth  = getMonthBox(arrDateRow);
        ComboBox<Integer>  arrDay    = getDayBox(arrDateRow);
        ComboBox<String>   arrTime   = getTimeBox(arrDateRow);

        Label errorLbl = new Label();
        errorLbl.setTextFill(Color.RED);
        errorLbl.setWrapText(true);
        errorLbl.setMaxWidth(380);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setMinWidth(420);
        form.getChildren().addAll(
                formRow("Flight Number:", flightNumField),
                formRow("Origin:",        originBox),
                formRow("Destination:",   destBox),
                formRow("Aircraft:",      aircraftBox),
                label("Departure Date & Time:"), depDateRow,
                label("Arrival Date & Time:"),   arrDateRow,
                errorLbl
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(420);
        dialog.getDialogPane().setContent(scroll);

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            errorLbl.setText("");

            String flightNum  = flightNumField.getText().trim().toUpperCase();
            String originFull = originBox.getValue();
            String destFull   = destBox.getValue();
            String aircraftSel = aircraftBox.getValue();

            if (flightNum.isEmpty() || originFull == null || destFull == null || aircraftSel == null ||
                depYear.getValue() == null || depMonth.getValue() == null ||
                depDay.getValue()  == null || depTime.getValue()  == null ||
                arrYear.getValue() == null || arrMonth.getValue() == null ||
                arrDay.getValue()  == null || arrTime.getValue()  == null) {
                errorLbl.setText("Please fill in all fields.");
                event.consume();
                return;
            }
            if (originFull.equals(destFull)) {
                errorLbl.setText("Origin and destination cannot be the same.");
                event.consume();
                return;
            }

            LocalDateTime depDateTime = buildDateTime(depYear, depMonth, depDay, depTime);
            LocalDateTime arrDateTime = buildDateTime(arrYear, arrMonth, arrDay, arrTime);

            if (!arrDateTime.isAfter(depDateTime)) {
                errorLbl.setText("Arrival must be after departure.");
                event.consume();
                return;
            }
            if (depDateTime.isBefore(LocalDateTime.now().minusMinutes(30))) {
                errorLbl.setText("Departure date/time cannot be in the past. Please select a future date.");
                event.consume();
                return;
            }

            String aircraftId = aircraftSel.split(" ")[0]; 
            String model      = aircraftSel.split("\\(")[1].split(",")[0].trim(); 
            int    capacity   = Integer.parseInt(aircraftSel.split(",")[1].trim().split(" ")[0]); 

            // FIX: Extracts specifically "CAI" instead of "Cairo" so it syncs perfectly with User Search queries
            String originCode = originFull.split(" ")[0];
            String destCode = destFull.split(" ")[0];

            try {
                Airport  originAirport = new Airport(originCode, originFull, originFull);
                Airport  destAirport   = new Airport(destCode, destFull, destFull);
                Aircraft aircraft      = new Aircraft(aircraftId, model, capacity);

                Flight newFlight = new Flight(flightNum, originAirport, destAirport,
                        depDateTime, arrDateTime, aircraft, "SCHEDULED");

                flightService.addFlight(newFlight);
                refreshFlightTable(table);

                new Alert(Alert.AlertType.INFORMATION,
                        "Flight " + flightNum + " added successfully!").showAndWait();
            } catch (DatabaseException ex) {
                errorLbl.setText("Error: " + ex.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private HBox buildDateTimeRow(String label) {
        ComboBox<Integer> yearBox = new ComboBox<>();
        int currentYear = java.time.LocalDate.now().getYear();
        for (int y = currentYear; y <= currentYear + 5; y++) yearBox.getItems().add(y);
        yearBox.setPromptText("Year");
        yearBox.setPrefWidth(90);

        String[] months = {"Jan","Feb","Mar","Apr","May","Jun", "Jul","Aug","Sep","Oct","Nov","Dec"};
        ComboBox<String> monthBox = new ComboBox<>();
        monthBox.getItems().addAll(months);
        monthBox.setPromptText("Month");
        monthBox.setPrefWidth(80);

        ComboBox<Integer> dayBox = new ComboBox<>();
        dayBox.setPromptText("Day");
        dayBox.setPrefWidth(70);

        Runnable updateDays = () -> {
            Integer y = yearBox.getValue();
            String  m = monthBox.getValue();
            if (y == null || m == null) return;
            int mIdx = java.util.Arrays.asList(months).indexOf(m) + 1;
            int days = java.time.YearMonth.of(y, mIdx).lengthOfMonth();
            Integer prev = dayBox.getValue();
            dayBox.getItems().clear();
            for (int d = 1; d <= days; d++) dayBox.getItems().add(d);
            if (prev != null && prev <= days) dayBox.setValue(prev);
        };
        yearBox.setOnAction(e -> updateDays.run());
        monthBox.setOnAction(e -> updateDays.run());

        ComboBox<String> timeBox = new ComboBox<>();
        for (int h = 0; h < 24; h++)
            for (int m = 0; m < 60; m += 30)
                timeBox.getItems().add(String.format("%02d:%02d", h, m));
        timeBox.setPromptText("Time");
        timeBox.setPrefWidth(90);

        HBox row = new HBox(8, yearBox, monthBox, dayBox, timeBox);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setUserData(label);
        return row;
    }

    @SuppressWarnings("unchecked")
    private ComboBox<Integer> getYearBox(HBox row)  { return (ComboBox<Integer>) row.getChildren().get(0); }
    @SuppressWarnings("unchecked")
    private ComboBox<String>  getMonthBox(HBox row) { return (ComboBox<String>)  row.getChildren().get(1); }
    @SuppressWarnings("unchecked")
    private ComboBox<Integer> getDayBox(HBox row)   { return (ComboBox<Integer>) row.getChildren().get(2); }
    @SuppressWarnings("unchecked")
    private ComboBox<String>  getTimeBox(HBox row)  { return (ComboBox<String>)  row.getChildren().get(3); }

    private LocalDateTime buildDateTime(ComboBox<Integer> year, ComboBox<String> month,
                                        ComboBox<Integer> day,  ComboBox<String> time) {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun", "Jul","Aug","Sep","Oct","Nov","Dec"};
        int mIdx  = java.util.Arrays.asList(months).indexOf(month.getValue()) + 1;
        String[] parts = time.getValue().split(":");
        return LocalDateTime.of(year.getValue(), mIdx, day.getValue(),
                Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    private VBox buildBookingsTab() {
        TableView<Reservation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No bookings found"));

        TableColumn<Reservation, String>  codeCol      = new TableColumn<>("Booking Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("bookingCode"));

        TableColumn<Reservation, Integer> passengerCol = new TableColumn<>("Passenger ID");
        passengerCol.setCellValueFactory(new PropertyValueFactory<>("passengerId"));

        TableColumn<Reservation, Integer> flightCol    = new TableColumn<>("Flight ID");
        flightCol.setCellValueFactory(new PropertyValueFactory<>("flightId"));

        TableColumn<Reservation, Double>  priceCol     = new TableColumn<>("Price ($)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        TableColumn<Reservation, String>  statusCol    = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStatus().name()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "CONFIRMED"  -> setStyle("-fx-text-fill: #1e7e34; -fx-font-weight: bold;");
                    case "CANCELLED"  -> setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
                    case "CHECKED_IN" -> setStyle("-fx-text-fill: #0056b3; -fx-font-weight: bold;");
                    default           -> setStyle("");
                }
            }
        });

        @SuppressWarnings("unchecked")
        TableColumn<Reservation, ?>[] reservationCols =
                new TableColumn[]{codeCol, passengerCol, flightCol, priceCol, statusCol};
        table.getColumns().addAll(reservationCols);

        try {
            table.getItems().setAll(bookingService.getAllBookings());
        } catch (DatabaseException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }

        VBox box = new VBox(table);
        box.setPadding(new Insets(18));
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox buildPassengersTab() {
        TableView<Passenger> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No passengers found"));

        TableColumn<Passenger, String>  idCol       = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Passenger, String>  nameCol     = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Passenger, String>  emailCol    = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Passenger, String>  passportCol = new TableColumn<>("Passport");
        passportCol.setCellValueFactory(new PropertyValueFactory<>("passportNumber"));

        TableColumn<Passenger, Integer> loyaltyCol  = new TableColumn<>("Loyalty Pts");
        loyaltyCol.setCellValueFactory(new PropertyValueFactory<>("loyaltyPoints"));
        
        TableColumn<Passenger, Void> actionCol = new TableColumn<>("Security Clearance");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button blockBtn = new Button("Suspend Account");
            {
                blockBtn.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #f87171; -fx-text-fill: #dc2626; -fx-border-radius: 4px; -fx-cursor: hand; -fx-font-size: 11px; -fx-font-weight: bold;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Passenger passenger = getTableView().getItems().get(getIndex());

                if (passenger.getPasswordHash() != null && passenger.getPasswordHash().startsWith("LOCKED_")) {
                    blockBtn.setText("Account Suspended");
                    blockBtn.setDisable(true);
                    blockBtn.setStyle("-fx-background-color: #cbd5e1; -fx-text-fill: #64748b; -fx-font-size: 11px;");
                }

                blockBtn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to suspend " + passenger.getName() + "? They will no longer be able to log in.");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == javafx.scene.control.ButtonType.OK) {
                            try {
                                passenger.setPasswordHash("LOCKED_" + java.util.UUID.randomUUID().toString());
                                com.fleurairlines.database.DatabaseService.getInstance().savePassenger(passenger);
                                table.refresh();
                                
                                new Alert(Alert.AlertType.INFORMATION, passenger.getName() + "'s account has been successfully suspended.").showAndWait();
                                
                            } catch (DatabaseException ex) {
                                new Alert(Alert.AlertType.ERROR, "Failed to apply security lock.").showAndWait();
                            }
                        }
                    });
                });
                setGraphic(blockBtn);
            }
        });

        @SuppressWarnings("unchecked")
        TableColumn<Passenger, ?>[] passengerCols =
                new TableColumn[]{idCol, nameCol, emailCol, passportCol, loyaltyCol, actionCol};
        table.getColumns().addAll(passengerCols);

        try {
            table.getItems().setAll(
                    com.fleurairlines.database.DatabaseService.getInstance().getAllPassengers());
        } catch (DatabaseException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }

        VBox box = new VBox(table);
        box.setPadding(new Insets(18));
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private void refreshFlightTable(TableView<Flight> table) {
        table.getItems().clear();
        try {
            table.getItems().setAll(flightService.getAllFlights());
        } catch (DatabaseException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    private HBox formRow(String labelText, javafx.scene.Node field) {
        Label lbl = new Label(labelText);
        lbl.setMinWidth(130);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + NAV_BLUE + ";");
        HBox row = new HBox(12, lbl, field);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + NAV_BLUE + "; -fx-font-size: 12px;");
        return l;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setMaxWidth(300);
        tf.setStyle(FIELD_STYLE);
        return tf;
    }

    private void handleStatusUpdate(Flight flight, TableView<Flight> table) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(flight.getStatus(), "SCHEDULED", "DELAYED", "DEPARTED", "CANCELLED");
        dialog.setTitle("Operational Override");
        dialog.setHeaderText("Update status for " + flight.getFlightNumber());
        dialog.setContentText("Select new status:");

        dialog.showAndWait().ifPresent(newStatus -> {
            try {
                if (newStatus.equals("DELAYED")) {
                    // FIX: Force the Admin to specify the exact new time
                    TextInputDialog timeDialog = new TextInputDialog(flight.getDepartureTime().format(FMT));
                    timeDialog.setTitle("Delay Configuration");
                    timeDialog.setHeaderText("Enter exact new departure time for " + flight.getFlightNumber());
                    timeDialog.setContentText("Format (yyyy-MM-dd HH:mm):");
                    
                    timeDialog.showAndWait().ifPresent(newTimeStr -> {
                        try {
                            // Validates and updates the flight time
                            LocalDateTime newTime = LocalDateTime.parse(newTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                            flight.setDepartureTime(newTime);
                            flight.setStatus("DELAYED");
                            
                            // Save to DB
                            com.fleurairlines.database.DatabaseService.getInstance().saveFlight(flight);
                            refreshFlightTable(table);
                            
                            new Alert(Alert.AlertType.INFORMATION, "Flight delayed to: " + newTimeStr).showAndWait();
                        } catch (Exception ex) {
                            new Alert(Alert.AlertType.ERROR, "Invalid time format. Please use yyyy-MM-dd HH:mm").showAndWait();
                        }
                    });
                } else {
                    flight.setStatus(newStatus);
                    com.fleurairlines.database.DatabaseService.getInstance().saveFlight(flight);
                    refreshFlightTable(table);
                    new Alert(Alert.AlertType.INFORMATION, "Status updated to " + newStatus).showAndWait();
                }
            } catch (DatabaseException ex) {
                new Alert(Alert.AlertType.ERROR, "System Error: " + ex.getMessage()).showAndWait();
            }
        });
    }


}