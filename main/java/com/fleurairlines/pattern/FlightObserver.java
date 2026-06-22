package com.fleurairlines.pattern;

public interface FlightObserver {
    void onFlightStatusChanged(String flightNumber, String newStatus, String message);
}

