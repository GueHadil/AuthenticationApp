package com.example;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class SmtpMailer implements Mailer {

    public static class Config {
        public String host;
        public int port;
        public boolean auth;
        public boolean starttls;
        public boolean ssl;
        public boolean debug;
        public String username;
        public String password;
        public String from;

        // Timeouts (ms)
        public int connectionTimeoutMs = 10000; // 10s
        public int timeoutMs = 15000;           // 15s
        public int writeTimeoutMs = 10000;      // 10s

        // Optional SOCKS proxy
        public String socksHost = null;
        public Integer socksPort = null;
    }

    private final Config cfg;
    private final Session session;

    public SmtpMailer(Config cfg) {
        this.cfg = cfg;

        Properties p = new Properties();
        p.put("mail.transport.protocol", "smtp");
        p.put("mail.smtp.host", cfg.host);
        p.put("mail.smtp.port", String.valueOf(cfg.port));
        p.put("mail.smtp.auth", String.valueOf(cfg.auth));
        p.put("mail.smtp.starttls.enable", String.valueOf(cfg.starttls));
        p.put("mail.smtp.ssl.enable", String.valueOf(cfg.ssl));

        // Timeouts
        p.put("mail.smtp.connectiontimeout", String.valueOf(cfg.connectionTimeoutMs));
        p.put("mail.smtp.timeout", String.valueOf(cfg.timeoutMs));
        p.put("mail.smtp.writetimeout", String.valueOf(cfg.writeTimeoutMs));

        // Optional SOCKS proxy
        if (cfg.socksHost != null && cfg.socksPort != null) {
            p.put("mail.smtp.socks.host", cfg.socksHost);
            p.put("mail.smtp.socks.port", String.valueOf(cfg.socksPort));
        }

        this.session = Session.getInstance(p);
        this.session.setDebug(cfg.debug);
    }

    @Override
    public boolean send(String to, String subject, String body) {
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(cfg.from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            msg.setSubject(subject, "UTF-8");
            msg.setText(body, "UTF-8");

            Transport t = session.getTransport("smtp");
            // Use explicit username/password to avoid null creds
            t.connect(cfg.host, cfg.port, cfg.username, cfg.password);
            t.sendMessage(msg, msg.getAllRecipients());
            t.close();

            return true;
        } catch (Exception e) {
            // Log full stack to diagnose (timeouts/creds/etc.)
            e.printStackTrace();
            return false;
        }
    }
}