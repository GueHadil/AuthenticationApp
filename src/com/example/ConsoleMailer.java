package com.example;

public class ConsoleMailer implements Mailer {
    @Override
    public boolean send(String to, String subject, String body) {
        System.out.println("=== SIMULATED EMAIL ===");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body:\n" + body);
        System.out.println("=======================");
        return true;
    }
}