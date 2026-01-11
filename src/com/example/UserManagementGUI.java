package com.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Application: "CloudDesk User Manager"
 * - Envoi d'email (acceptation/rejet + mot de passe oublié) via Mailer (SMTP/Console)
 * - Réinitialisation SANS HTTP: envoi du CODE (token) par email et saisie dans un formulaire Swing
 */
public class UserManagementGUI extends JFrame {
    private static final String APP_NAME = "CloudDesk User Manager";

    private final UserService userService = new UserService();
    private User currentUser;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JLabel totalUsersLabel;
    private JLabel acceptedUsersLabel;
    private JLabel pendingUsersLabel;

    // Palette
    private static final Color SIDEBAR_BG = new Color(20, 22, 30);
    private static final Color SIDEBAR_HOVER = new Color(40, 44, 52);
    private static final Color SIDEBAR_SELECTED = new Color(84, 110, 255);
    private static final Color CONTENT_BG = new Color(245, 247, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(84, 110, 255);
    private static final Color PRIMARY_DARK = new Color(65, 86, 220);
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color BORDER_COLOR = new Color(222, 226, 230);

    // Icônes (fallback)
    private static final Icon ICON_DASH = UIManager.getIcon("FileView.computerIcon");
    private static final Icon ICON_PENDING = UIManager.getIcon("OptionPane.warningIcon");
    private static final Icon ICON_USERS = UIManager.getIcon("FileView.directoryIcon");
    private static final Icon ICON_PROFILE = UIManager.getIcon("FileChooser.homeFolderIcon");

    public UserManagementGUI() {
        setLookAndFeel();
        setTitle(APP_NAME);
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        showLoginScreen();
    }

    private void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("Table.showGrid", false);
        } catch (Exception ignored) {}
    }

    private void showLoginScreen() {
        Container root = getContentPane();
        Component center = ((BorderLayout) root.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (center != null) root.remove(center);

        JPanel loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBackground(CONTENT_BG);
        loginPanel.add(createBrandHeader(), BorderLayout.NORTH);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setBackground(CONTENT_BG);

        JPanel card = new RoundedPanel(16);
        card.setLayout(BoxLayoutFactory(card));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        card.setPreferredSize(new Dimension(520, 480));

        JLabel title = new JLabel("Bienvenue");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField emailField = createFormField();
        JPasswordField passwordField = new JPasswordField();
        stylePasswordField(passwordField);

        JButton loginBtn = primaryButton("Se connecter");
        JButton regBtn = outlineButton("Créer un compte");
        JButton resetBtn = subtleButton("Mot de passe oublié ?");
        JButton haveCodeBtn = subtleButton("J’ai un code"); // Nouveau

        loginBtn.addActionListener(e -> {
            String email = emailField.getText();
            String pwd = new String(passwordField.getPassword());

            if (email == null || email.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email requis.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pwd == null || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mot de passe requis.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User u = userService.loginUser(email.trim(), pwd);
            if (u == null) {
                JOptionPane.showMessageDialog(this, "Email ou mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!u.isEnabled()) {
                JOptionPane.showMessageDialog(this, "Compte désactivé. Contactez l'administrateur.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!"ACCEPTED".equalsIgnoreCase(u.getStatus())) {
                JOptionPane.showMessageDialog(this, "Demande non encore acceptée. Veuillez patienter.", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            currentUser = u;
            initializeApp();
        });

        regBtn.addActionListener(e -> SwingUtilities.invokeLater(this::showRegistrationDialog));
        resetBtn.addActionListener(e -> showResetRequestDialog());
        haveCodeBtn.addActionListener(e -> showResetApplyDialog());

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        addFormRow(card, "Email", emailField);
        addFormRow(card, "Mot de passe", passwordField);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        btns.setOpaque(false);
        btns.add(loginBtn);
        btns.add(regBtn);
        card.add(btns);

        JPanel aux = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        aux.setOpaque(false);
        aux.add(resetBtn);
        aux.add(haveCodeBtn);
        card.add(aux);

        centerWrap.add(card);
        loginPanel.add(centerWrap, BorderLayout.CENTER);
        root.add(loginPanel, BorderLayout.CENTER);

        root.revalidate();
        root.repaint();
    }

    private JComponent createBrandHeader() {
        JPanel header = new GradientPanel(PRIMARY_COLOR, PRIMARY_DARK);
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JButton brandBtn = headerButton(APP_NAME);
        brandBtn.setForeground(Color.WHITE);
        brandBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        brandBtn.setOpaque(false);
        brandBtn.setContentAreaFilled(false);
        brandBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                APP_NAME + "\nGestion des utilisateurs avec thème moderne bleu.",
                "À propos", JOptionPane.INFORMATION_MESSAGE));

        JLabel tagline = new JLabel("Gérez vos utilisateurs avec fluidité et style");
        tagline.setForeground(new Color(225, 235, 245));
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(BoxLayoutFactory(text));
        text.add(brandBtn);
        text.add(tagline);

        header.add(text, BorderLayout.WEST);
        return header;
    }

    private void initializeApp() {
        Container root = getContentPane();
        root.removeAll();
        root.setLayout(new BorderLayout());
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMainContent(), BorderLayout.CENTER);
        root.revalidate();
        root.repaint();
        loadUserData();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(BoxLayoutFactory(sidebar));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel profile = new RoundedPanel(12);
        profile.setLayout(new BorderLayout());
        profile.setBackground(new Color(35, 38, 48));
        profile.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel name = new JLabel(currentUser != null ? currentUser.getUsername() : "Invité");
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel role = new JLabel(resolveRoleName(currentUser));
        role.setForeground(new Color(180, 185, 195));
        role.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        profile.add(name, BorderLayout.NORTH);
        profile.add(role, BorderLayout.SOUTH);
        sidebar.add(profile);

        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        addNavItem(sidebar, "Dashboard", ICON_DASH, true);
        if (isAdmin()) {
            addNavItem(sidebar, "Pending Requests", ICON_PENDING, false);
            addNavItem(sidebar, "Users", ICON_USERS, false);
        } else {
            addNavItem(sidebar, "My Profile", ICON_PROFILE, false);
        }

        sidebar.add(Box.createVerticalGlue());
        JButton logout = outlineButton("Déconnexion");
        logout.addActionListener(e -> {
            currentUser = null;
            getContentPane().removeAll();
            getContentPane().setLayout(new BorderLayout());
            showLoginScreen();
        });
        JPanel logoutWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        logoutWrap.setOpaque(false);
        logoutWrap.add(logout);
        sidebar.add(logoutWrap);

        return sidebar;
    }

    private void addNavItem(JPanel sidebar, String text, Icon icon, boolean selected) {
        JPanel item = new RoundedPanel(10);
        item.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        item.setBackground(selected ? SIDEBAR_SELECTED : SIDEBAR_BG);
        item.setMaximumSize(new Dimension(240, 44));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton labelBtn = new JButton(text);
        labelBtn.setForeground(Color.WHITE);
        labelBtn.setOpaque(false);
        labelBtn.setContentAreaFilled(false);
        labelBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        labelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        labelBtn.addActionListener(e -> switchTab(text));
        item.add(new JLabel(icon));
        item.add(labelBtn);

        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!selected) item.setBackground(SIDEBAR_HOVER); }
            public void mouseExited(MouseEvent e) { if (!selected) item.setBackground(SIDEBAR_BG); }
            public void mouseClicked(MouseEvent e) { switchTab(text); }
        });

        sidebar.add(item);
    }

    private void switchTab(String tab) {
        Container root = getContentPane();
        Component center = ((BorderLayout) root.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (center != null) root.remove(center);

        if (isAdmin()) {
            if ("Pending Requests".equals(tab)) root.add(createPendingRequestsContent(), BorderLayout.CENTER);
            else if ("Users".equals(tab)) root.add(createUsersContent(), BorderLayout.CENTER);
            else root.add(createMainContent(), BorderLayout.CENTER);
        } else {
            if ("My Profile".equals(tab)) root.add(createNonAdminProfileContent(), BorderLayout.CENTER);
            else root.add(createMainContent(), BorderLayout.CENTER);
        }
        root.revalidate();
        root.repaint();
        loadUserData();
    }

    private JPanel createMainContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel(isAdmin() ? "Tableau de bord (Admin)" : "Tableau de bord");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        if (isAdmin()) {
            JButton invite = primaryButton("Ajouter un utilisateur");
            invite.addActionListener(e -> SwingUtilities.invokeLater(this::showRegistrationDialog));
            right.add(invite);
        } else {
            JButton profileBtn = primaryButton("Mon profil");
            profileBtn.addActionListener(e -> switchTab("My Profile"));
            right.add(profileBtn);
        }

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(BoxLayoutFactory(body));
        body.setOpaque(false);
        body.add(createStatsPanel());
        body.add(Box.createRigidArea(new Dimension(0, 16)));

        if (isAdmin()) {
            body.add(createUsersTableCard());
        } else {
            body.add(createRoleContentCard());
            body.add(Box.createRigidArea(new Dimension(0, 16)));
            body.add(createNonAdminInfoCard());
        }

        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createNonAdminProfileContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Mon profil");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(BoxLayoutFactory(body));
        body.setOpaque(false);
        body.add(createRoleContentCard());
        body.add(Box.createRigidArea(new Dimension(0, 16)));
        body.add(createNonAdminInfoCard());
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRoleContentCard() {
        String role = normalizeRole(resolveRoleName(currentUser));
        JPanel card = new RoundedPanel(14);
        card.setLayout(BoxLayoutFactory(card));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Actions " + role);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(TEXT_PRIMARY);
        card.add(header);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel actions = new JPanel(new GridLayout(0, 2, 10, 10));
        actions.setOpaque(false);

        if ("ETUDIANT".equals(role)) {
            actions.add(roleActionButton("Voir mes cours"));
            actions.add(roleActionButton("Voir mes notes"));
            actions.add(roleActionButton("Annonces"));
            actions.add(roleActionButton("Documents pédagogiques"));
            actions.add(roleActionButton("Contacter un enseignant"));
        } else if ("ENSEIGNANT".equals(role)) {
            actions.add(roleActionButton("Gérer mes classes"));
            actions.add(roleActionButton("Publier une annonce"));
            actions.add(roleActionButton("Déposer un support"));
            actions.add(roleActionButton("Saisir/valider les notes"));
            actions.add(roleActionButton("Messagerie étudiants"));
        } else if ("GESTIONNAIRE".equals(role)) {
            actions.add(roleActionButton("Superviser inscriptions"));
            actions.add(roleActionButton("Gérer comptes utilisateurs"));
            actions.add(roleActionButton("Voir statistiques"));
            actions.add(roleActionButton("Administrer rôles"));
            actions.add(roleActionButton("Paramètres système"));
        } else {
            actions.add(roleActionButton("Voir mon profil"));
            actions.add(roleActionButton("Mettre à jour mes infos"));
            actions.add(roleActionButton("Ressources de mon rôle"));
        }

        card.add(actions);
        return card;
    }

    private JButton roleActionButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(PRIMARY_COLOR);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Action: " + text, "Info", JOptionPane.INFORMATION_MESSAGE));
        return b;
    }

    private String resolveRoleName(User u) {
        if (u == null) return "Utilisateur";
        if (u.getRoleName() != null && !u.getRoleName().trim().isEmpty()) return u.getRoleName().trim();
        int id = u.getRoleId();
        if (id == 1) return "ADMIN";
        return "Utilisateur";
    }

    private String normalizeRole(String roleName) {
        if (roleName == null) return "UTILISATEUR";
        String r = roleName.trim().toUpperCase();
        if (r.contains("ETUD") || r.contains("STUDENT")) return "ETUDIANT";
        if (r.contains("ENSEIGN") || r.contains("TEACH")) return "ENSEIGNANT";
        if (r.contains("GESTION") || r.contains("MANAGER")) return "GESTIONNAIRE";
        if (r.equals("ADMIN")) return "ADMIN";
        return r;
    }

    private JPanel createNonAdminInfoCard() {
        JPanel card = new RoundedPanel(14);
        card.setLayout(BoxLayoutFactory(card));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Informations personnelles");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(TEXT_PRIMARY);
        card.add(header);

        if (currentUser != null) {
            card.add(labelLine("Nom complet: ", currentUser.getUsername()));
            card.add(labelLine("Email: ", currentUser.getEmail()));
            card.add(labelLine("Rôle: ", currentUser.getRoleName() != null ? currentUser.getRoleName() : String.valueOf(currentUser.getRoleId())));
        } else {
            card.add(labelLine("Nom complet: ", "-"));
            card.add(labelLine("Email: ", "-"));
            card.add(labelLine("Rôle: ", "-"));
        }
        return card;
    }

    private JPanel labelLine(String label, String value) {
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        line.setOpaque(false);
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel v = new JLabel(value); v.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_PRIMARY);
        v.setForeground(TEXT_SECONDARY);
        line.add(l); line.add(v);
        return line;
    }

    private JPanel createPendingRequestsContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Demandes en attente");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"ID", "Nom", "Email", "Rôle", "Créé", "Actions"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = makeTable(model);

        List<User> pending = userService.listPendingUsers();
        for (User u : pending) {
            model.addRow(new Object[]{
                    Integer.valueOf(u.getUserId()), u.getUsername(), u.getEmail(),
                    u.getRoleName() != null ? u.getRoleName() : u.getRoleId(),
                    u.getCreatedAt(), "Double-clic"
            });
        }

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        Object val = model.getValueAt(row, 0);
                        int id = toInt(val);
                        if (id >= 0) {
                            showPendingActionDialog(id);
                        } else {
                            JOptionPane.showMessageDialog(UserManagementGUI.this,
                                    "ID invalide sélectionné.", "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void showPendingActionDialog(int userId) {
        Object[] options = {"✅ Accept", "❌ Reject", "🗑️ Delete"};
        int choice = JOptionPane.showOptionDialog(this,
                "Action for user ID: " + userId, "Pending Request",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        boolean result = false;
        String msg = "";
        if (choice == 0) {
            result = userService.approveUser(userId);
            msg = result ? "User accepted." : "Approval failed.";
        } else if (choice == 1) {
            result = userService.rejectUser(userId);
            msg = result ? "User rejected." : "Rejection failed.";
        } else if (choice == 2) {
            result = userService.deleteUserAccount(userId);
            msg = result ? "User deleted." : "Deletion failed.";
        }

        if (choice >= 0) {
            JOptionPane.showMessageDialog(this, msg,
                    result ? "Success" : "Error",
                    result ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        }
        switchTab("Pending Requests");
    }

    private JPanel createUsersContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Tous les utilisateurs");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        panel.add(createUsersTableCard(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel stats = new JPanel(new GridLayout(1, isAdmin() ? 3 : 2, 16, 0));
        stats.setOpaque(false);

        totalUsersLabel = new JLabel("0");
        acceptedUsersLabel = new JLabel("0");
        pendingUsersLabel = new JLabel("0");

        if (isAdmin()) {
            stats.add(createStatCard("Utilisateurs", totalUsersLabel));
            stats.add(createStatCard("Acceptés", acceptedUsersLabel));
            stats.add(createStatCard("En attente", pendingUsersLabel));
        } else {
            stats.add(createStatCard("Mon statut", new JLabel(currentUser != null ? currentUser.getStatus() : "-")));
            stats.add(createStatCard("Mon rôle", new JLabel(resolveRoleName(currentUser))));
        }
        return stats;
    }

    private JPanel createStatCard(String title, JLabel valueLabel) {
        JPanel card = new RoundedPanel(14);
        card.setLayout(BoxLayoutFactory(card));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JLabel t = new JLabel(title);
        t.setForeground(TEXT_SECONDARY);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(TEXT_PRIMARY);
        card.add(t);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(valueLabel);
        return card;
    }

    private JPanel createUsersTableCard() {
        JPanel card = new RoundedPanel(14);
        card.setLayout(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] columns = {"Utilisateur", "Rôle", "Statut", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } };

        userTable = makeTable(tableModel);
        userTable.setRowHeight(30);
        userTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!isAdmin()) return;
                if (e.getClickCount() == 2) {
                    int row = userTable.getSelectedRow();
                    if (row >= 0) showUserRowActions(row);
                }
            }
        });

        card.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return card;
    }

    private JTable makeTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(30);
        t.setShowHorizontalLines(false);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        return t;
    }

    private void showUserRowActions(int row) {
        if (!isAdmin()) return;
        List<User> users = userService.listAllUsers();
        if (row >= users.size()) return;
        User user = users.get(row);

        Object[] options = {"👁️ Voir", "🗑️ Supprimer"};
        int choice = JOptionPane.showOptionDialog(this,
                "Sélectionner une action pour: " + user.getUsername(), "Actions utilisateur",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            showUserDetails(user);
        } else if (choice == 1) {
            deleteUser(user);
        }
    }

    private void showUserDetails(User user) {
        JOptionPane.showMessageDialog(this,
                "Détails utilisateur:\n" +
                        "ID: " + user.getUserId() + "\n" +
                        "Nom: " + user.getUsername() + "\n" +
                        "Email: " + user.getEmail() + "\n" +
                        "Rôle: " + (user.getRoleName() != null ? user.getRoleName() : user.getRoleId()) + "\n" +
                        "Statut: " + user.getStatus() + "\n" +
                        "Activé: " + user.isEnabled(),
                "Détails", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteUser(User user) {
        if (!isAdmin()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer l'utilisateur " + user.getUsername() + " ?", "Confirmer", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = userService.deleteUserAccount(user.getUserId());
            JOptionPane.showMessageDialog(this, ok ? "Supprimé." : "Échec de suppression.",
                    ok ? "Succès" : "Erreur",
                    ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (ok) loadUserData();
        }
    }

    private void loadUserData() {
        List<User> users = userService.listAllUsers();
        int total = users.size();
        int accepted = 0;
        int pending = 0;

        for (User u : users) {
            if ("ACCEPTED".equalsIgnoreCase(u.getStatus())) accepted++;
            if ("PENDING".equalsIgnoreCase(u.getStatus())) pending++;
        }

        if (isAdmin()) {
            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(total));
            if (acceptedUsersLabel != null) acceptedUsersLabel.setText(String.valueOf(accepted));
            if (pendingUsersLabel != null) pendingUsersLabel.setText(String.valueOf(pending));
            if (tableModel != null) {
                tableModel.setRowCount(0);
                for (User u : users) {
                    tableModel.addRow(new Object[]{
                            u.getUsername() + " <" + u.getEmail() + ">",
                            u.getRoleName() != null ? u.getRoleName() : u.getRoleId(),
                            u.getStatus(),
                            "Actions"
                    });
                }
            }
        }
    }

    private boolean isAdmin() {
        if (currentUser == null) return false;
        if (currentUser.getRoleName() != null)
            return "ADMIN".equalsIgnoreCase(currentUser.getRoleName());
        return currentUser.getRoleId() == 1;
    }

    private JTextField createFormField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        tf.setPreferredSize(new Dimension(0, 40));
        return tf;
    }

    private void stylePasswordField(JPasswordField pf) {
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        pf.setPreferredSize(new Dimension(0, 40));
    }

    private JButton primaryButton(String text) {
        return new RoundedButton(text, PRIMARY_COLOR, Color.WHITE);
    }

    private JButton outlineButton(String text) {
        JButton b = new RoundedButton(text, Color.WHITE, TEXT_PRIMARY);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        return b;
    }

    private JButton subtleButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        b.setContentAreaFilled(false);
        b.setForeground(PRIMARY_COLOR);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton headerButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setForeground(PRIMARY_COLOR);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void addFormRow(JPanel parent, String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        parent.add(l);
        parent.add(Box.createRigidArea(new Dimension(0, 6)));
        parent.add(field);
        parent.add(Box.createRigidArea(new Dimension(0, 12)));
    }

    private int toInt(Object value) {
        if (value == null) return -1;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) {
            long l = (Long) value;
            return (l > Integer.MAX_VALUE) ? -1 : (int) l;
        }
        if (value instanceof String) {
            try { return Integer.parseInt(((String) value).trim()); }
            catch (NumberFormatException e) { return -1; }
        }
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(String.valueOf(value).trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    // 1) Envoi du code par email
    private void showResetRequestDialog() {
        String email = JOptionPane.showInputDialog(this, "Entrez votre email pour recevoir un code:",
                "Réinitialisation du mot de passe", JOptionPane.QUESTION_MESSAGE);
        if (email != null && !email.trim().isEmpty()) {
            boolean ok = userService.requestPasswordReset(email.trim());
            JOptionPane.showMessageDialog(this,
                    ok ? "Un email vous a été envoyé avec le CODE de réinitialisation." : "Email introuvable ou erreur.",
                    ok ? "Succès" : "Erreur",
                    ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (ok) {
                int openForm = JOptionPane.showConfirmDialog(this,
                        "Souhaitez-vous saisir le code maintenant ?", "Réinitialisation",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (openForm == JOptionPane.YES_OPTION) {
                    showResetApplyDialog();
                }
            }
        }
    }

    // 2) Formulaire pour saisir le code + nouveau mot de passe
    private void showResetApplyDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(BoxLayoutFactory(panel));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField tokenField = createFormField();
        tokenField.setToolTipText("Collez ici le code reçu par email");
        JPasswordField newPwdField = new JPasswordField();
        stylePasswordField(newPwdField);

        addFormRow(panel, "Code (token)", tokenField);
        addFormRow(panel, "Nouveau mot de passe", newPwdField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Appliquer la réinitialisation",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String token = tokenField.getText() != null ? tokenField.getText().replaceAll("\\s+", "").trim() : "";
            String newPwd = new String(newPwdField.getPassword());
            if (token.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le code est requis.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newPwd == null || newPwd.length() < 6) {
                JOptionPane.showMessageDialog(this, "Mot de passe trop court (min 6).", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean ok = userService.resetPassword(token, newPwd);
            JOptionPane.showMessageDialog(this,
                    ok ? "Mot de passe réinitialisé. Vous pouvez vous connecter." : "Token invalide/expiré ou erreur.",
                    ok ? "Succès" : "Erreur",
                    ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRegistrationDialog() {
        JDialog dialog = new JDialog(this, "Créer un compte", true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(this);

        JPanel content = new RoundedPanel(14);
        content.setLayout(BoxLayoutFactory(content));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JTextField usernameField = createFormField();
        JTextField emailField = createFormField();
        JPasswordField passwordField = new JPasswordField();
        stylePasswordField(passwordField);

        List<String> roles = userService.getRegistrationRoles();
        JComboBox<String> roleBox = new JComboBox<>(roles.toArray(new String[0]));
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        addFormRow(content, "Nom complet", usernameField);
        addFormRow(content, "Email", emailField);
        addFormRow(content, "Mot de passe", passwordField);

        JLabel roleLabel = new JLabel("Rôle");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roleLabel.setForeground(TEXT_PRIMARY);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(roleLabel);
        content.add(Box.createRigidArea(new Dimension(0, 6)));
        content.add(roleBox);
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton cancel = outlineButton("Annuler");
        JButton create = primaryButton("Créer");
        cancel.addActionListener(e -> dialog.dispose());
        create.addActionListener(e -> {
            boolean ok = userService.registerUser(
                    usernameField.getText(),
                    emailField.getText(),
                    new String(passwordField.getPassword()),
                    (String) roleBox.getSelectedItem()
            );
            JOptionPane.showMessageDialog(dialog, ok ? "Compte créé (PENDING)." : "Échec de création.",
                    ok ? "Succès" : "Erreur", ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (ok) { dialog.dispose(); loadUserData(); }
        });

        buttons.add(cancel);
        buttons.add(create);
        content.add(buttons);

        dialog.add(content);
        dialog.setVisible(true);
    }

    private BoxLayout BoxLayoutFactory(Container target) {
        return new BoxLayout(target, BoxLayout.Y_AXIS);
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        RoundedPanel(int radius) { this.radius = radius; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundedButton extends JButton {
        private final Color bg;
        private final Color fg;
        RoundedButton(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg; this.fg = fg;
            setFocusPainted(false);
            setForeground(fg);
            setBackground(bg);
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getModel().isPressed() ? bg.darker() : (getModel().isRollover() ? bg.brighter() : bg);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class GradientPanel extends JPanel {
        private final Color c1, c2;
        GradientPanel(Color c1, Color c2) { this.c1 = c1; this.c2 = c2; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserManagementGUI().setVisible(true));
    }
}