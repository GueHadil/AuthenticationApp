package com.example;

public interface Mailer {
    boolean send(String to, String subject, String body);
}