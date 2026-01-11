package com.example;

import java.util.List;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Console User Management (MailHog) ===");

        boolean running = true;
        while (running) {
            System.out.println("\nMenu:");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. List All Users");
            System.out.println("4. List Pending Users");
            System.out.println("5. Approve User (ADMIN)");
            System.out.println("6. Request Password Reset");
            System.out.println("7. Apply Password Reset");
            System.out.println("8. Delete User (ADMIN)");
            System.out.println("9. Exit");
            System.out.print("Choice: ");

            int choice;
            try { choice = Integer.parseInt(scanner.nextLine().trim()); }
            catch (Exception e) { System.out.println("Invalid."); continue; }

            switch (choice) {
                case 1 -> register(scanner, userService);
                case 2 -> login(scanner, userService);
                case 3 -> listAll(userService);
                case 4 -> listPending(userService);
                case 5 -> approve(scanner, userService);
                case 6 -> requestReset(scanner, userService);
                case 7 -> applyReset(scanner, userService);
                case 8 -> delete(scanner, userService);
                case 9 -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
        scanner.close();
    }

    private static void register(Scanner sc, UserService svc) {
        System.out.print("Full name: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Password: ");
        String pwd = sc.nextLine();

        List<String> roles = svc.getRegistrationRoles();
        System.out.println("Roles disponibles (hors ADMIN):");
        for (int i = 0; i < roles.size(); i++) System.out.println((i+1) + ". " + roles.get(i));
        System.out.print("Choisir rôle numéro: ");
        String rRaw = sc.nextLine();
        int idx;
        try { idx = Integer.parseInt(rRaw.trim()) - 1; } catch (Exception e) { idx = -1; }
        if (idx < 0 || idx >= roles.size()) {
            System.out.println("Rôle invalide.");
            return;
        }

        boolean ok = svc.registerUser(name, email, pwd, roles.get(idx));
        System.out.println(ok ? "Inscription OK (PENDING)." : "Echec inscription.");
    }

    private static void login(Scanner sc, UserService svc) {
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Password: ");
        String pwd = sc.nextLine();
        User u = svc.loginUser(email, pwd);
        System.out.println(u != null ? ("Login OK -> " + u) : "Login failed.");
    }

    private static void listAll(UserService svc) {
        List<User> users = svc.listAllUsers();
        users.forEach(System.out::println);
    }

    private static void listPending(UserService svc) {
        List<User> pending = svc.listPendingUsers();
        pending.forEach(System.out::println);
    }

    private static void approve(Scanner sc, UserService svc) {
        System.out.print("User ID à approuver: ");
        int id = Integer.parseInt(sc.nextLine().trim());
        boolean ok = svc.approveUser(id);
        System.out.println(ok ? "Approuvé + email (MailHog)." : "Echec.");
    }

    private static void requestReset(Scanner sc, UserService svc) {
        System.out.print("Email pour reset: ");
        String email = sc.nextLine();
        boolean ok = svc.requestPasswordReset(email);
        System.out.println(ok ? "Token envoyé (MailHog)." : "Echec.");
    }

    private static void applyReset(Scanner sc, UserService svc) {
        System.out.print("Token: ");
        String token = sc.nextLine();
        System.out.print("Nouveau mot de passe: ");
        String pwd = sc.nextLine();
        boolean ok = svc.resetPassword(token, pwd);
        System.out.println(ok ? "Mot de passe mis à jour." : "Echec reset.");
    }

    private static void delete(Scanner sc, UserService svc) {
        System.out.print("User ID à supprimer: ");
        int id = Integer.parseInt(sc.nextLine().trim());
        boolean ok = svc.deleteUserAccount(id);
        System.out.println(ok ? "Supprimé." : "Echec suppression.");
    }
}