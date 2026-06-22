package com.fleurairlines.ui;

import java.util.List;

import com.fleurairlines.model.Passenger;
import com.fleurairlines.model.Reservation;
import com.fleurairlines.service.BookingService;
import com.fleurairlines.util.DatabaseException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MyBookingsScreen {

    private final Stage stage;
    private final Passenger passenger;
    private final BookingService bookingService;

    public MyBookingsScreen(Stage stage, Passenger passenger) {
        this.stage = stage;
        this.passenger = passenger;
        try {
            this.bookingService = new BookingService();
        } catch (DatabaseException e) {
            throw new IllegalStateException(e);
        }
    }

    public Scene createScene(double width, double height) {
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
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("← Dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-radius: 4px; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            try {
                SceneTransitionUtil.standardFade(stage, new PassengerDashboard(stage, passenger).createScene(width, height));
            } catch (DatabaseException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        topNav.getChildren().addAll(logo, spacer, backBtn);

        // ── Content Area ─────────────────────────────────────────────────────
        VBox content = new VBox(24);
        content.setPadding(new Insets(40));

        Label header = new Label("Your Upcoming Trips");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f2b5c;");

        TableView<ReservationRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        TableColumn<ReservationRow, String> codeCol = new TableColumn<>("Booking Reference");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("bookingCode"));
        codeCol.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f2b5c;");

        TableColumn<ReservationRow, String> seatCol = new TableColumn<>("Seat / Class");
        seatCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getSeat() + " (" + c.getValue().getSeatClass() + ")"
        ));

        TableColumn<ReservationRow, Double> priceCol = new TableColumn<>("Total Amount");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(String.format("$%.2f", item));
                setStyle("-fx-text-fill: #334155;");
            }
        });

        TableColumn<ReservationRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "CONFIRMED"  -> setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    case "CANCELLED"  -> setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    case "CHECKED_IN" -> setStyle("-fx-text-fill: #0073bc; -fx-font-weight: bold;");
                    default           -> setStyle("-fx-text-fill: #64748b;");
                }
            }
        });

        TableColumn<ReservationRow, Void> actionCol = new TableColumn<>("Manage");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel Trip");
            {
                cancelBtn.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #f87171; -fx-text-fill: #dc2626; -fx-border-radius: 4px; -fx-cursor: hand; -fx-font-size: 11px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                ReservationRow row = getTableView().getItems().get(getIndex());
                
                if ("CANCELLED".equals(row.getStatus()) || "COMPLETED".equals(row.getStatus())) {
                    setGraphic(null);
                    return;
                }
                cancelBtn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancel booking " + row.getBookingCode() + "? Cancellation fees may apply.");
                    confirm.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            try {
                                bookingService.cancelBooking(row.getBookingCode());
                                refresh(table);
                            } catch (DatabaseException ex) {
                                new Alert(Alert.AlertType.ERROR, "Cancellation failed: " + ex.getMessage()).showAndWait();
                            }
                        }
                    });
                });
                setGraphic(cancelBtn);
            }
        });

        table.getColumns().add(codeCol);
        table.getColumns().add(seatCol);
        table.getColumns().add(priceCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(actionCol);
        VBox.setVgrow(table, Priority.ALWAYS);
        refresh(table);

        content.getChildren().addAll(header, table);
        
        root.setTop(topNav);
        root.setCenter(content);
        root.setBottom(new FleurFooter());

        return new Scene(root, width, height);
    }

    private void refresh(TableView<ReservationRow> table) {
        table.getItems().clear();
        try {
            List<Reservation> list = bookingService.getMyBookings(passenger.getId());
            for (Reservation r : list) {
                table.getItems().add(new ReservationRow(
                        r.getBookingCode(), "Seat " + r.getSeatId(), r.getSeatClass().name(),
                        r.getTotalPrice(), r.getStatus().name()
                ));
            }
        } catch (DatabaseException e) {
            new Alert(Alert.AlertType.ERROR, "Unable to load bookings: " + e.getMessage()).showAndWait();
        }
    }

    public static final class ReservationRow {
        private final String bookingCode;
        private final String seat;
        private final String seatClass;
        private final double price;
        private final String status;

        public ReservationRow(String bookingCode, String seat, String seatClass, double price, String status) {
            this.bookingCode = bookingCode;
            this.seat = seat;
            this.seatClass = seatClass;
            this.price = price;
            this.status = status;
        }

        public String getBookingCode() { return bookingCode; }
        public String getSeat()        { return seat; }
        public String getSeatClass()   { return seatClass; }
        public double getPrice()       { return price; }
        public String getStatus()      { return status; }
    }
}