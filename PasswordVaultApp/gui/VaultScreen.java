package gui;

import model.VaultItem;
import service.VaultService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class VaultScreen extends JFrame {

    private int userId;
    private String username;
    private VaultService vaultService;
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField siteField;
    private JTextField siteUserField;
    private JPasswordField sitePassField;
    private JProgressBar strengthBar;
    private JCheckBox showPasswordCheck;
    private Timer clipboardTimer;

    // Dark theme colors
    private final Color mainBg = new Color(28, 28, 28);
    private final Color panelBg = new Color(40, 40, 40);
    private final Color inputBg = new Color(60, 60, 60);
    private final Color btnBg = new Color(75, 75, 75);
    private final Color btnHover = new Color(95, 95, 95);
    private final Color tableBg = new Color(45, 45, 45);
    private final Color tableHeaderBg = new Color(60, 60, 60);
    private final Font defaultFont = new Font("Segoe UI", Font.PLAIN, 13);

    public VaultScreen(int userId, String username) {
        this.userId = userId;
        this.username = username;
        this.vaultService = new VaultService();

        setTitle("Vault - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 500);
        setLocationRelativeTo(null);
        getContentPane().setBackground(mainBg);

        initUI();
        loadVault();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // --- TOP PANEL ---
        JPanel topPanel = makePanel(new BorderLayout(), panelBg);
        JLabel userLabel = new JLabel("Logged in as: " + username);
        userLabel.setFont(defaultFont.deriveFont(Font.BOLD));
        userLabel.setForeground(Color.WHITE);

        JPanel userOptionsPanel = makePanel(new FlowLayout(FlowLayout.RIGHT), panelBg);
        JButton helpBtn = new JButton("Help");
        JButton switchUserBtn = new JButton("Switch User");
        JButton logoutBtn = new JButton("Logout");

        for (JButton btn : new JButton[]{helpBtn, switchUserBtn, logoutBtn}) styleButton(btn);
        userOptionsPanel.add(helpBtn);
        userOptionsPanel.add(switchUserBtn);
        userOptionsPanel.add(logoutBtn);

        topPanel.add(userLabel, BorderLayout.WEST);
        topPanel.add(userOptionsPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- INPUT PANEL ---
        JPanel inputPanel = makePanel(new GridBagLayout(), mainBg);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel siteLabel = new JLabel("Site:");
        JLabel siteUserLabel = new JLabel("Site Username:");
        JLabel sitePassLabel = new JLabel("Site Password:");
        JLabel strengthLabel = new JLabel("Password Strength:");
        for (JLabel lbl : new JLabel[]{siteLabel, siteUserLabel, sitePassLabel, strengthLabel})
            lbl.setForeground(Color.WHITE);

        siteField = makeTextField(15);
        siteUserField = makeTextField(15);
        sitePassField = makePasswordField(15);

        strengthBar = new JProgressBar(0, 100);
        strengthBar.setStringPainted(true);
        strengthBar.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        strengthBar.setBackground(new Color(50, 50, 50));

        showPasswordCheck = new JCheckBox("Show Password");
        showPasswordCheck.setBackground(mainBg);
        showPasswordCheck.setForeground(Color.WHITE);

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");
        JButton copyBtn = new JButton("Copy Password");
        JButton generateBtn = new JButton("Generate Password");

        for (JButton btn : new JButton[]{addBtn, updateBtn, deleteBtn, refreshBtn, copyBtn, generateBtn})
            styleButton(btn);

        // Layout input panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(siteLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(siteField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(siteUserLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(siteUserField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(sitePassLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(sitePassField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(strengthLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(strengthBar, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        inputPanel.add(showPasswordCheck, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 5;
        inputPanel.add(addBtn, gbc);
        gbc.gridx = 1;
        inputPanel.add(updateBtn, gbc);
        gbc.gridx = 0;
        gbc.gridy = 6;
        inputPanel.add(deleteBtn, gbc);
        gbc.gridx = 1;
        inputPanel.add(refreshBtn, gbc);
        gbc.gridx = 0;
        gbc.gridy = 7;
        inputPanel.add(copyBtn, gbc);
        gbc.gridx = 1;
        inputPanel.add(generateBtn, gbc);

        add(inputPanel, BorderLayout.WEST);

        // --- TABLE PANEL ---
        tableModel = new DefaultTableModel(new String[]{"ID", "Site", "Username", "Password"}, 0);
        table = new JTable(tableModel);
        table.setBackground(tableBg);
        table.setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(70, 70, 70));
        table.setRowHeight(28);
        table.setGridColor(new Color(70, 70, 70));
        table.getTableHeader().setFont(defaultFont.deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(tableHeaderBg);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(table.getWidth(), 30));

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.removeColumn(columnModel.getColumn(0));

        // mask password column
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value != null ? "********" : "");
                setForeground(Color.WHITE);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(panelBg);
        add(scrollPane, BorderLayout.CENTER);

        // --- ACTIONS ---
        addBtn.addActionListener(e -> addVaultItem());
        updateBtn.addActionListener(e -> updateVaultItemSmooth());
        deleteBtn.addActionListener(e -> deleteVaultItem());
        refreshBtn.addActionListener(e -> loadVault());
        copyBtn.addActionListener(e -> copyPassword());
        generateBtn.addActionListener(e -> generatePassword());
        showPasswordCheck.addActionListener(e -> togglePasswordVisibility());
        helpBtn.addActionListener(e -> showHelp());
        switchUserBtn.addActionListener(e -> {
            dispose();
            new AuthScreen().setVisible(true);
        });
        logoutBtn.addActionListener(e -> {
            int confirm = showDarkConfirm("Are you sure you want to logout?", "Logout");
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new AuthScreen().setVisible(true);
            }
        });

        sitePassField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateStrength();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateStrength();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateStrength();
            }
        });
    }

    // --- VAULT LOGIC ---
    private void loadVault() {
        try {
            List<VaultItem> items = vaultService.getAllItems(userId);
            tableModel.setRowCount(0);
            for (VaultItem v : items)
                tableModel.addRow(new Object[]{v.getId(), v.getSite(), v.getSiteUsername(), v.getSitePassword()});
        } catch (SQLException e) {
            showDarkPopup("Error loading vault: " + e.getMessage(), "Error");
        }
    }

    private void addVaultItem() {
        String site = siteField.getText();
        String siteUser = siteUserField.getText();
        String sitePass = new String(sitePassField.getPassword());
        if (site.isEmpty() || siteUser.isEmpty() || sitePass.isEmpty()) {
            showDarkPopup("Please fill in all fields!", "Warning");
            return;
        }
        try {
            VaultItem newItem = new VaultItem(site, siteUser, sitePass);
            vaultService.addItem(userId, newItem);
            loadVault();
            clearFields();
            showDarkPopup("Password added successfully!", "Success");
        } catch (SQLException e) {
            showDarkPopup("Error adding item: " + e.getMessage(), "Error");
        }
    }

    private void updateVaultItemSmooth() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showDarkPopup("Select an item to update.", "Warning");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String oldSite = (String) tableModel.getValueAt(modelRow, 1);
        String oldUser = (String) tableModel.getValueAt(modelRow, 2);
        String oldPass = (String) tableModel.getValueAt(modelRow, 3);

        String[] options = {"Site", "Username", "Password"};
        String choice = (String) JOptionPane.showInputDialog(this, "Choose field to update:", "Update Vault Item",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == null) return;

        JTextField inputField = new JTextField();
        inputField.setBackground(inputBg);
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setText(choice.equals("Site") ? oldSite : choice.equals("Username") ? oldUser : oldPass);

        int confirm = JOptionPane.showConfirmDialog(this, inputField, "Enter new " + choice,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) return;

        String newValue = inputField.getText();
        if (newValue.isEmpty()) {
            showDarkPopup("Value cannot be empty!", "Error");
            return;
        }

        try {
            VaultItem updated = new VaultItem(id,
                    choice.equals("Site") ? newValue : oldSite,
                    choice.equals("Username") ? newValue : oldUser,
                    choice.equals("Password") ? newValue : oldPass);
            vaultService.updateItem(updated);
            loadVault();
            clearFields();
            showDarkPopup("Item updated successfully!", "Success");
        } catch (SQLException e) {
            showDarkPopup("Error updating: " + e.getMessage(), "Error");
        }
    }

    private void deleteVaultItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showDarkPopup("Select an item to delete.", "Warning");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        int confirm = showDarkConfirm("Are you sure you want to delete this password?", "Delete");
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            vaultService.deleteItem(id);
            loadVault();
            showDarkPopup("Password deleted successfully.", "Deleted");
        } catch (SQLException e) {
            showDarkPopup("Error deleting: " + e.getMessage(), "Error");
        }
    }

    private void copyPassword() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showDarkPopup("Select an item to copy.", "Warning");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String password = (String) tableModel.getValueAt(modelRow, 3);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(password), null);
        showDarkPopup("Password copied to clipboard for 10 seconds!", "Copied");
        if (clipboardTimer != null) clipboardTimer.cancel();
        clipboardTimer = new Timer();
        clipboardTimer.schedule(new TimerTask() {
            public void run() {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
            }
        }, 10000);
    }

    private void generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(chars.charAt((int) (Math.random() * chars.length())));
        sitePassField.setText(sb.toString());
        updateStrength();
    }

    private void togglePasswordVisibility() {
        sitePassField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '*');
    }

    private void updateStrength() {
        String pwd = new String(sitePassField.getPassword());
        int score = passwordStrength(pwd);
        strengthBar.setValue(score);
        if (score < 40) {
            strengthBar.setForeground(Color.RED);
            strengthBar.setString("Weak");
        } else if (score < 70) {
            strengthBar.setForeground(Color.ORANGE);
            strengthBar.setString("Medium");
        } else {
            strengthBar.setForeground(Color.GREEN);
            strengthBar.setString("Strong");
        }
    }

    private int passwordStrength(String pwd) {
        if (pwd.isEmpty()) return 0;
        int score = 0;
        if (pwd.length() >= 8) score += 30;
        if (Pattern.compile("[A-Z]").matcher(pwd).find()) score += 20;
        if (Pattern.compile("[a-z]").matcher(pwd).find()) score += 20;
        if (Pattern.compile("[0-9]").matcher(pwd).find()) score += 15;
        if (Pattern.compile("[!@#$%^&*()\\-_=+]").matcher(pwd).find()) score += 15;
        return Math.min(score, 100);
    }

    private void clearFields() {
        siteField.setText("");
        siteUserField.setText("");
        sitePassField.setText("");
        strengthBar.setValue(0);
        strengthBar.setString("");
        showPasswordCheck.setSelected(false);
        sitePassField.setEchoChar('*');
    }

    private void showHelp() {
    String helpText = "<html><body style='width:580px; color:white; font-family:Segoe UI; font-size:13px;'>"
            + "<h2 style='color:#00BFFF;'>Password Vault Help</h2>"
            + "<ul style='margin-left:20px; line-height:1.6;'>"
            + "<li>Add, Update, or Delete your saved site credentials easily.</li>"
            + "<li>Click <b>'Copy Password'</b> to copy it; it auto-clears from clipboard after 10 seconds.</li>"
            + "<li>Use <b>'Generate Password'</b> to create a strong random password instantly.</li>"
            + "<li>The <b>Password Strength</b> bar indicates Weak, Medium, or Strong levels.</li>"
            + "<li>Check <b>'Show Password'</b> to temporarily reveal hidden passwords.</li>"
            + "<li>Use <b>'Switch User'</b> or <b>'Logout'</b> to change or exit accounts securely.</li>"
            + "<li>Click <b>'Refresh'</b> to update your vault with the latest saved data.</li>"
            + "</ul>"
            + "<p style='margin-top:10px; color:#B0B0B0;'><i>Stay secure. Stay organized. Stay smart.</i></p>"
            + "</body></html>";

    // Dark themed dialog with wider width
    JDialog dialog = new JDialog(this, "Help", true);
    dialog.setSize(700, 430); // increased width
    dialog.setResizable(false);
    dialog.setLocationRelativeTo(this);
    dialog.getContentPane().setBackground(new Color(40, 40, 40));
    dialog.setLayout(new BorderLayout());

    JLabel msgLabel = new JLabel(helpText);
    msgLabel.setHorizontalAlignment(SwingConstants.LEFT);
    msgLabel.setVerticalAlignment(SwingConstants.TOP);
    msgLabel.setBorder(BorderFactory.createEmptyBorder(25, 25, 15, 25));

    JButton okBtn = new JButton("OK");
    okBtn.setBackground(new Color(75, 75, 75));
    okBtn.setForeground(Color.WHITE);
    okBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    okBtn.setFocusPainted(false);
    okBtn.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));

    // smooth hover
    okBtn.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) { okBtn.setBackground(new Color(95, 95, 95)); }
        public void mouseExited(java.awt.event.MouseEvent evt) { okBtn.setBackground(new Color(75, 75, 75)); }
    });
    okBtn.addActionListener(e -> dialog.dispose());

    JPanel btnPanel = new JPanel();
    btnPanel.setBackground(new Color(40, 40, 40));
    btnPanel.add(okBtn);

    dialog.add(msgLabel, BorderLayout.CENTER);
    dialog.add(btnPanel, BorderLayout.SOUTH);
    dialog.setVisible(true);
}





    // --- DARK POPUP UTILITIES ---
    private void showDarkPopup(String message, String title) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(panelBg);
        dialog.setLayout(new BorderLayout());

        JLabel msgLabel = new JLabel("<html><body style='width: 350px;'>" + message + "</body></html>");
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setFont(defaultFont);
        msgLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton okBtn = new JButton("OK");
        styleButton(okBtn);
        okBtn.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(panelBg);
        btnPanel.add(okBtn);

        dialog.add(msgLabel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private int showDarkConfirm(String message, String title) {
        UIManager.put("OptionPane.background", panelBg);
        UIManager.put("Panel.background", panelBg);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("Button.background", btnBg);
        UIManager.put("Button.foreground", Color.WHITE);
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    // --- UTILITY METHODS ---
    private void styleButton(JButton btn) {
        btn.setBackground(btnBg);
        btn.setForeground(Color.WHITE);
        btn.setFont(defaultFont);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(btnHover);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(btnBg);
            }
        });
    }

    private JPanel makePanel(LayoutManager layout, Color bg) {
        JPanel p = new JPanel(layout);
        p.setBackground(bg);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return p;
    }

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
}
