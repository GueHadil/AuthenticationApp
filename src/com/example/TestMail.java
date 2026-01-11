package com.example;

public class TestMail {
    public static void main(String[] args) {
        // Construit SmtpMailer avec variables d’environnement
        SmtpMailer.Config cfg = new SmtpMailer.Config();
        cfg.host = getEnv("MAIL_HOST", "smtp.gmail.com");
        cfg.port = Integer.parseInt(getEnv("MAIL_PORT", "587"));
        cfg.auth = true;
        cfg.starttls = Boolean.parseBoolean(getEnv("MAIL_STARTTLS", "true"));
        cfg.ssl = Boolean.parseBoolean(getEnv("MAIL_SSL", "false"));
        cfg.debug = Boolean.parseBoolean(getEnv("MAIL_DEBUG", "true"));
        cfg.username = System.getenv("MAIL_USERNAME");
        cfg.password = System.getenv("MAIL_PASSWORD");
        cfg.from = System.getenv("MAIL_FROM");

        SmtpMailer mailer = new SmtpMailer(cfg);
        boolean ok = mailer.send(
                "guellazhadil20@gmail.com",
                "Test SMTP Jakarta Mail",
                "Ceci est un test d'envoi SMTP via Jakarta Mail."
        );
        System.out.println("Send result: " + ok);
    }

    private static String getEnv(String k, String def) {
        String v = System.getenv(k);
        return (v != null && !v.trim().isEmpty()) ? v.trim() : def;
    }
}