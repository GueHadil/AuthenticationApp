package com.example;

import javax.swing.*;

/**
 * Launcher class to choose between Console and GUI version
 */
public class Launcher {
    public static void main(String[] args) {
        String[] options = {"GUI Version", "Console Version"};

        int choice = JOptionPane.showOptionDialog(
                null,
                "Choose how you want to run the User Management System:",
                "User Management System - Launcher",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            // Launch GUI version
            SwingUtilities.invokeLater(() -> {
                UserManagementGUI gui = new UserManagementGUI();
                gui.setVisible(true);
            });
        } else if (choice == 1) {
            // Launch Console version
            Main.main(new String[0]);
        } else {
            System.exit(0);
        }
    }
}

