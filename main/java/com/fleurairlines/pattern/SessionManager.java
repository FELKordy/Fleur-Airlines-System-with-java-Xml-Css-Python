package com.fleurairlines.pattern;

import com.fleurairlines.model.Admin;
import com.fleurairlines.model.Passenger;
import com.fleurairlines.model.Person;

public final class SessionManager {

    private static volatile SessionManager instance;
    private Person currentUser;

    private SessionManager() {
        this.currentUser = null;
    }

    public static SessionManager getInstance() {
        SessionManager result = instance;
        if (result == null) {
            synchronized (SessionManager.class) {
                result = instance;
                if (result == null)
                    instance = result = new SessionManager();
            }
        }
        return result;
    }

    public synchronized void login(Person user)    { this.currentUser = user; }
    public synchronized void loginPassenger(Passenger passenger) { this.currentUser = passenger; }
    public synchronized void loginAdmin(Admin admin) { this.currentUser = admin; }
    public synchronized void logout()              { this.currentUser = null; }
    public synchronized Person getCurrentUser()    { return currentUser; }
    public synchronized boolean isLoggedIn()       { return currentUser != null; }
    public synchronized boolean isAdmin()          { return currentUser instanceof Admin; }
    public synchronized boolean isPassenger()      { return currentUser instanceof Passenger; }
}