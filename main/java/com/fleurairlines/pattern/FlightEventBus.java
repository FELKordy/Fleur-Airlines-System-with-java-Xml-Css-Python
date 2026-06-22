package com.fleurairlines.pattern;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FlightEventBus {

    private static volatile FlightEventBus instance;
    private final Set<FlightObserver> observers;
    private final ExecutorService executorService;

    private FlightEventBus() {
        this.observers = ConcurrentHashMap.newKeySet();
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "flight-event-bus");
            t.setDaemon(true);
            return t;
        });
    }

    public static FlightEventBus getInstance() {
        FlightEventBus result = instance;
        if (result == null) {
            synchronized (FlightEventBus.class) {
                result = instance;
                if (result == null)
                    instance = result = new FlightEventBus();
            }
        }
        return result;
    }

    public void subscribe(FlightObserver observer) {
        if (observer == null) return;
        observers.add(observer);
    }

    public void unsubscribe(FlightObserver observer) {
        if (observer == null) return;
        observers.remove(observer);
    }

    public void publish(String flightNumber, String newStatus, String message) {
        if (flightNumber == null || newStatus == null) return;
        for (FlightObserver observer : observers)
            executorService.execute(() -> observer.onFlightStatusChanged(flightNumber, newStatus, message));
    }
}