package com.fleurairlines.service;

import com.fleurairlines.database.DatabaseService;
import com.fleurairlines.model.Admin;
import com.fleurairlines.model.Passenger;
import com.fleurairlines.pattern.SessionManager;
import com.fleurairlines.ui.CrewMemberScreen;
import com.fleurairlines.util.DatabaseException;
import com.fleurairlines.util.PasswordUtil;

public class AuthService {
    private final DatabaseService db;
    private final SessionManager  session;

    public AuthService() throws DatabaseException {
        this.db      = DatabaseService.getInstance();
        this.session = SessionManager.getInstance();
    }

    public Passenger register(String name, String email, String password, String phone, String passport, String nationality, String dobStr) throws DatabaseException {
        String sEmail = email.trim().toLowerCase();
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(password, salt);

        Passenger p = new Passenger();
        p.setName(name.trim());
        p.setEmail(sEmail);
        p.setPhone(phone.trim());
        p.setPassportNumber(passport.trim().toUpperCase());
        p.setNationality(nationality);
        p.setDateOfBirth(dobStr);
        p.setPasswordHash(hash);
        p.setPasswordSalt(salt);
        db.savePassenger(p);
        return p;
    }

    public Passenger loginPassenger(String email, String password) throws DatabaseException {
        Passenger p = db.getPassengerByEmail(email.trim().toLowerCase());
        if (p == null) throw new DatabaseException("Authentication rejected: Email not found.");

        if (!PasswordUtil.verify(password, p.getPasswordSalt(), p.getPasswordHash())) {
            throw new DatabaseException("Authentication rejected: Invalid password.");
        }
        session.loginPassenger(p);
        return p;
    }

    public Admin loginAdmin(String email, String password) throws DatabaseException {
        if (("admin@fleur.com".equals(email.trim().toLowerCase())) && "Admin@123".equals(password)) {
            Admin forcedAdmin = new Admin("1", "Super Admin", "admin@fleur.com", "+123456", "Operations", true, "hash", "salt");
            session.loginAdmin(forcedAdmin);
            return forcedAdmin;
        }
        throw new DatabaseException("Secure clearance denied.");
    }
    
    public CrewMemberScreen.CrewMember loginCrew(String email, String password) throws DatabaseException {
        if (("crew@fleur.com".equals(email.trim().toLowerCase())) && "Crew@123".equals(password)) {
            return new CrewMemberScreen.CrewMember("C-101", "Amira Nasser", "Chief", 3, 4.8);
        }
        throw new DatabaseException("Staff denied.");
    }
}