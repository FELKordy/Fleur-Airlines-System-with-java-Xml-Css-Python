package com.fleurairlines.service;

import com.fleurairlines.database.DatabaseService;
import com.fleurairlines.model.Flight;
import com.fleurairlines.pattern.FlightEventBus;
import com.fleurairlines.pattern.SessionManager;
import com.fleurairlines.util.DatabaseException;
import com.fleurairlines.util.InputSanitizer;

import java.util.List;

public class FlightService {

    private final DatabaseService db;
    private final SessionManager  session;
    private final FlightEventBus  eventBus;

    public FlightService() throws DatabaseException {
        this.db       = DatabaseService.getInstance();
        this.session  = SessionManager.getInstance();
        this.eventBus = FlightEventBus.getInstance();
    }

    // ── Get All Flights ───────────────────────────────────────────────────────
    public List<Flight> getAllFlights() throws DatabaseException {
        return db.getAllFlights();
    }

    // ── Search Flights ────────────────────────────────────────────────────────
    public List<Flight> searchFlights(String origin, String destination, String date)
            throws DatabaseException {
        String o  = InputSanitizer.sanitizeText(origin).trim();
        String d  = InputSanitizer.sanitizeText(destination).trim();
        String dt = InputSanitizer.sanitizeText(date).trim();

        // Use SQL-backed search with a reasonable default limit to avoid OOM/slow streaming
        int DEFAULT_LIMIT = 50;
        return db.searchFlights(o, d, dt, DEFAULT_LIMIT);
    }

    // ── Update Flight Status (admin only) ─────────────────────────────────────
    public Flight updateFlightStatus(String flightNumber, String newStatus)
            throws DatabaseException {
        requireAdmin();

        if (flightNumber == null || flightNumber.isBlank())
            throw new DatabaseException("Flight number cannot be empty.");
        if (newStatus == null || newStatus.isBlank())
            throw new DatabaseException("Status cannot be empty.");

        Flight flight = db.getFlightByNumber(flightNumber.trim());
        if (flight == null)
            throw new DatabaseException("Flight not found: " + flightNumber);

        flight.setStatus(newStatus.trim());
        db.saveFlight(flight);
        eventBus.publish(flightNumber.trim(), newStatus.trim(), "Flight status updated to " + newStatus.trim());
        return flight;
    }

    // ── Add Flight (admin only) ───────────────────────────────────────────────
    public void addFlight(Flight flight) throws DatabaseException {
        requireAdmin();
        if (flight == null)
            throw new DatabaseException("Flight cannot be null.");
        db.saveFlight(flight);
    }

    // ── Get Flight By Number ──────────────────────────────────────────────────
    public Flight getFlightByNumber(String flightNumber) throws DatabaseException {
        if (flightNumber == null || flightNumber.isBlank())
            throw new DatabaseException("Flight number cannot be empty.");
        return db.getFlightByNumber(flightNumber.trim());
    }

    // ── Access guard ──────────────────────────────────────────────────────────
    private void requireAdmin() throws DatabaseException {
        if (!session.isLoggedIn() || !session.isAdmin())
            throw new DatabaseException("Admin access required.");
    }
}