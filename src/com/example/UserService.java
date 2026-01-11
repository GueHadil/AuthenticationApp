package com.example;

import java.util.List;

public class UserService {

    private final UserDAO userDAO;
    private final Mailer mailer;

    public UserService() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        this.userDAO = new UserDAO();
        this.mailer = buildMailer();
    }

    private Mailer buildMailer() {
        SmtpMailer.Config cfg = new SmtpMailer.Config();
        cfg.host = "smtp.gmail.com";
        cfg.port = 587;
        cfg.auth = true;
        cfg.starttls = true;
        cfg.ssl = false;
        cfg.debug = true;

        cfg.username = "email@gmail.com";
        cfg.password = "auth_app_password";
        cfg.from = "email0@gmail.com";

        return new SmtpMailer(cfg);
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String e = email.trim().toLowerCase();
        return e.contains("@") && e.indexOf('@') > 0 && e.lastIndexOf('.') > e.indexOf('@') + 1;
    }

    private boolean isValidPassword(String pwd) {
        return pwd != null && pwd.length() >= 6;
    }

    // ===== REGISTRATION =====

    public List<String> getRegistrationRoles() {
        return userDAO.getRegistrationRoleNames();
    }

    public boolean registerUser(String username, String email, String password, String roleName) {
        if (username == null || username.trim().isEmpty()) return false;
        if (!isValidEmail(email)) return false;
        if (!isValidPassword(password)) return false;
        if ("ADMIN".equalsIgnoreCase(roleName)) return false;
        if (userDAO.emailExists(email)) return false;

        int roleId = userDAO.getRoleIdByName(roleName);
        if (roleId <= 0) return false;

        User u = new User();
        u.setUsername(username.trim());
        u.setEmail(email.trim().toLowerCase());
        u.setPassword(password);
        u.setRoleId(roleId);
        u.setStatus("PENDING");
        u.setEnabled(true);

        return userDAO.createUser(u);
    }

    // ===== LOGIN =====

    public User loginUser(String email, String password) {
        if (!isValidEmail(email) || password == null) return null;
        return userDAO.authenticate(email.trim().toLowerCase(), password);
    }

    // ===== ADMIN =====

    public List<User> listAllUsers() {
        return userDAO.getAllUsers();
    }

    public List<User> listPendingUsers() {
        return userDAO.getPendingUsers();
    }

    public boolean approveUser(int userId) {
        User u = userDAO.getUserById(userId);
        boolean ok = userDAO.approveUser(userId);
        if (ok && u != null) {
            mailer.send(u.getEmail(),
                    EmailTemplates.approvalSubject(),
                    EmailTemplates.approvalBody(u));
        }
        return ok;
    }
    // Reject: update status + send rejection email
    public boolean rejectUser(int userId) {
        User u = userDAO.getUserById(userId);
        boolean ok = userDAO.rejectUser(userId);
        if (ok && u != null && isValidEmail(u.getEmail())) {
            mailer.send(
                    u.getEmail(),
                    EmailTemplates.rejectionSubject(),
                    EmailTemplates.rejectionBody(u)
            );
        }
        return ok;
    }



    public boolean deleteUserAccount(int userId) {
        return userDAO.deleteUser(userId);
    }

    // ===== PASSWORD RESET (FIXED) =====

    public boolean requestPasswordReset(String email) {
        if (!isValidEmail(email)) return false;

        String normalized = email.trim().toLowerCase();
        String token = userDAO.createResetToken(normalized, 15);

        if (token == null) return false;

        return mailer.send(
                normalized,
                EmailTemplates.resetSubject(),
                EmailTemplates.resetBody(token)
        );
    }

    public boolean resetPassword(String token, String newPassword) {
        if (!isValidPassword(newPassword)) return false;
        if (token == null) return false;

        return userDAO.resetPasswordByToken(token.trim(), newPassword);
    }
}
