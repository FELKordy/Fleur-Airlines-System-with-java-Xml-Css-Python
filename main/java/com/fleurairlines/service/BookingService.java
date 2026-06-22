package com.fleurairlines.service;

import java.util.List;

import com.fleurairlines.database.DatabaseService;
import com.fleurairlines.model.Passenger;
import com.fleurairlines.model.Reservation;
import com.fleurairlines.model.Seat;
import com.fleurairlines.pattern.SessionManager;
import com.fleurairlines.util.DatabaseException;

public class BookingService {

    private final DatabaseService db;
    private final SessionManager  session;

    public BookingService() throws DatabaseException {
        this.db      = DatabaseService.getInstance();
        this.session = SessionManager.getInstance();
    }

    // ── Create Booking ────────────────────────────────────────────────────────
    public Reservation createBooking(int passengerId, int flightId, int seatId,
                                     Seat.SeatClass seatClass, double price)
            throws DatabaseException {

        requirePassenger();

        if (passengerId <= 0) throw new DatabaseException("Invalid passenger ID.");
        if (seatClass   == null) throw new DatabaseException("Seat class cannot be null.");
        if (price       <= 0) throw new DatabaseException("Price must be positive.");

        Reservation reservation = new Reservation();
        reservation.setPassengerId(passengerId);
        reservation.setFlightId(flightId);
        reservation.setSeatId(seatId);
        reservation.setSeatClass(seatClass);
        reservation.setTotalPrice(price);
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);

        db.saveReservation(reservation);

        if (session.getCurrentUser() instanceof Passenger passenger
                && passenger.getId().equals(String.valueOf(passengerId))) {
            passenger.addLoyaltyPoints((int) price);
            db.savePassenger(passenger);
        }

        return reservation;
    }

    // ── Cancel Booking ────────────────────────────────────────────────────────
    public void cancelBooking(String bookingCode) throws DatabaseException {
        if (bookingCode == null || bookingCode.isBlank())
            throw new DatabaseException("Booking code cannot be empty.");

        Reservation existing = db.getReservationByBookingCode(bookingCode.trim());
        if (existing == null) throw new DatabaseException("Booking not found.");
        db.cancelReservation(bookingCode.trim());

        if (session.getCurrentUser() instanceof Passenger passenger
                && passenger.getId().equals(String.valueOf(existing.getPassengerId()))) {
            int pointsToRemove = (int) Math.round(existing.getTotalPrice());
            int remaining = passenger.getLoyaltyPoints() - pointsToRemove;
            passenger.setLoyaltyPoints(Math.max(0, remaining));
            db.savePassenger(passenger);
        }
    }

    // ── My Bookings ───────────────────────────────────────────────────────────
    // FIX: removed requirePassenger() guard here — the passenger is already
    // identified by their ID passed as parameter. requirePassenger() was
    // blocking valid calls when the session object reference changed after login.
    public List<Reservation> getMyBookings(String passengerId) throws DatabaseException {
        if (passengerId == null || passengerId.isBlank())
            throw new DatabaseException("Passenger ID cannot be empty.");
        return db.getReservationsByPassenger(passengerId);
    }

    // ── All Bookings (admin only) ─────────────────────────────────────────────
    public List<Reservation> getAllBookings() throws DatabaseException {
        requireAdmin();
        return db.getAllReservations();
    }

    // ── Access guards ─────────────────────────────────────────────────────────
    private void requirePassenger() throws DatabaseException {
        if (!session.isLoggedIn() || !session.isPassenger())
            throw new DatabaseException("Passenger access required.");
    }

    private void requireAdmin() throws DatabaseException {
        if (!session.isLoggedIn() || !session.isAdmin())
            throw new DatabaseException("Admin access required.");
    }
}