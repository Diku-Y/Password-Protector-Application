package gui;

import service.AuthService;

import javax.swing.*;
import java.awt.*;

public class AuthScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private AuthService authService;

    private int attempts = 0;
    private static final int MAX_ATTEMPTS = 3;

    // Dark theme colors
    private final Color mainBg = new Color(28, 28, 28);
    private final Color panelBg = new Color(40, 40, 40);
    private final Color inputBg = new Color(60, 60, 60);
    private final Color btnBg = new Color(75, 75, 75);
    private final Color btnHover = new Color(95, 95, 95);
    private final Font defaultFont = new Font("Segoe UI", Font.PLAIN, 14);

    public AuthScreen() {
        this.authService = new AuthService();

        setTitle("Password Vault - Login/Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        getContentPane().setBackground(mainBg);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(mainBg);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        usernameLabel.setForeground(Color.WHITE);
        passwordLabel.setForeground(Color.WHITE);

        usernameField = makeTextField(15);
        passwordField = makePasswordField(15);

        JButton loginBtn = makeButton("Login");
        JButton registerBtn = makeButton("Register");

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; panel.add(usernameLabel, gbc);
        gbc.gridx = 1; panel.add(usernameField, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1; panel.add(passwordLabel, gbc);
        gbc.gridx = 1; panel.add(passwordField, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2; panel.add(loginBtn, gbc);
        gbc.gridx = 1; panel.add(registerBtn, gbc);

        add(panel);

        // Button actions
        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> register());
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (attempts >= MAX_ATTEMPTS) {
            showDarkMessage("Too many failed login attempts. Please restart the app.", "Login Blocked", false);
            return;
        }

        try {
            int userId = authService.login(username, password);

            if (userId != -1) {
                showDarkMessage("Login successful!", "Success", true);
                dispose();
                new VaultScreen(userId, username).setVisible(true);
            } else {
                attempts++;
                showDarkMessage("Invalid username or password. Attempts left: " + (MAX_ATTEMPTS - attempts), "Login Failed", false);
            }
        } catch (Exception ex) {
            showDarkMessage("Unexpected error: " + ex.getMessage(), "Error", false);
            ex.printStackTrace();
        }
    }

    private void register() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (authService.register(username, password)) {
            showDarkMessage("Registration successful! You can login now.", "Success", true);
        } else {
            showDarkMessage("Username already exists or registration failed.", "Error", false);
        }
    }

    // Dark themed message dialog
    private void showDarkMessage(String message, String title, boolean success) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(350, 160);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(panelBg);
        dialog.setLayout(new BorderLayout());

        JLabel msgLabel = new JLabel("<html><body style='width: 300px;'>" + message + "</body></html>");
        msgLabel.setForeground(success ? new Color(100, 255, 100) : new Color(255, 100, 100));
        msgLabel.setFont(defaultFont);
        msgLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton okBtn = makeButton("OK");
        okBtn.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(panelBg);
        btnPanel.add(okBtn);

        dialog.add(msgLabel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // --- Utility methods ---
    private JTextField makeTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(inputBg);
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        tf.setFont(defaultFont);
        return tf;
    }

    private JPasswordField makePasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setBackground(inputBg);
        pf.setForeground(Color.WHITE);
        pf.setCaretColor(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        pf.setFont(defaultFont);
        return pf;
    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(btnBg);
        btn.setForeground(Color.WHITE);
        btn.setFont(defaultFont);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(btnHover); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(btnBg); }
        });
        return btn;
    }
}
