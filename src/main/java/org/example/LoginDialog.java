package org.example;

import org.example.service.EmailSearchService;
import org.example.service.MailService;
import org.example.service.MimeParserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginDialog extends JDialog {

    private final JTextField emailField = new JTextField("user@example.com");
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextField imapHostField = new JTextField("imap.example.com");
    private final JTextField imapPortField = new JTextField("993");
    private final JTextField smtpHostField = new JTextField("smtp.example.com");
    private final JTextField smtpPortField = new JTextField("465");

    private final JButton loginButton = new JButton("Войти");
    private final JButton cancelButton = new JButton("Отмена");
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);

    private MailService mailService;

    public LoginDialog(Frame parent) {
        super(parent, "Авторизация — MailWave", true);
        initUI();
    }

    private void initUI() {
        setSize(440, 480);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BG_PANEL);

        // Заголовок
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BG_DARKEST);
        headerPanel.setBorder(new EmptyBorder(20, 24, 16, 24));

        JLabel titleLabel = new JLabel("MailWave");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Введите данные вашей почты для входа");
        subtitleLabel.setFont(Theme.FONT_SMALL);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Theme.BG_PANEL);
        formPanel.setBorder(new EmptyBorder(16, 24, 8, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        int row = 0;

        addFormField(formPanel, gbc, row++, "Email / Логин:", emailField);
        addFormField(formPanel, gbc, row++, "Пароль / App Password:", passwordField);

        // Настройки серверов
        JLabel serverSettingsLabel = new JLabel("Настройки серверов");
        serverSettingsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        serverSettingsLabel.setForeground(Theme.TEXT_ACCENT);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 4, 4, 4);
        formPanel.add(serverSettingsLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 4, 4, 4);
        addFormField(formPanel, gbc, row++, "IMAP Сервер:", imapHostField);
        addFormField(formPanel, gbc, row++, "IMAP Порт:", imapPortField);
        addFormField(formPanel, gbc, row++, "SMTP Сервер:", smtpHostField);
        addFormField(formPanel, gbc, row++, "SMTP Порт:", smtpPortField);

        // Панель статуса и кнопок
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Theme.BG_PANEL);
        bottomPanel.setBorder(new EmptyBorder(8, 24, 20, 24));

        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.TEXT_MUTED);
        statusLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Theme.BG_PANEL);

        styleButton(cancelButton, Theme.BG_CARD, Theme.TEXT_SECONDARY);
        styleButton(loginButton, Theme.ACCENT_DIM, Theme.TEXT_ACCENT);

        cancelButton.addActionListener(e -> dispose());
        loginButton.addActionListener(e -> performLogin());

        buttonPanel.add(cancelButton);
        buttonPanel.add(loginButton);

        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_BODY);
        label.setForeground(Theme.TEXT_SECONDARY);

        field.setBackground(Theme.BG_CARD);
        field.setForeground(Theme.TEXT_PRIMARY);
        field.setCaretColor(Theme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, Theme.BORDER),
                new EmptyBorder(5, 8, 5, 8)
        ));

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(label, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(Theme.FONT_BODY);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, Theme.BORDER),
                new EmptyBorder(6, 16, 6, 16)
        ));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            @Override
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
    }

    private void performLogin() {
        String username = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String imapHost = imapHostField.getText().trim();
        String imapPort = imapPortField.getText().trim();
        String smtpHost = smtpHostField.getText().trim();
        String smtpPort = smtpPortField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || imapHost.isEmpty() || smtpHost.isEmpty()) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Заполните все обязательные поля!");
            return;
        }

        loginButton.setEnabled(false);
        cancelButton.setEnabled(false);
        statusLabel.setForeground(Theme.TEXT_ACCENT);
        statusLabel.setText("Проверка подключения к " + imapHost + "...");

        SwingWorker<MailService, Void> worker = new SwingWorker<>() {
            @Override
            protected MailService doInBackground() throws Exception {
                MimeParserService parserService = new MimeParserService();
                EmailSearchService searchService = new EmailSearchService();
                EmailReceiver receiver = new EmailReceiver(imapHost, imapPort, username, password);
                EmailSender sender = new EmailSender(smtpHost, smtpPort, username, password);

                MailService service = new MailService(receiver, sender, parserService, searchService);

                service.testConnection();
                return service;
            }

            @Override
            protected void done() {
                try {
                    mailService = get();
                    dispose();
                } catch (Exception ex) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Ошибка авторизации: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
                    loginButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }


    public MailService getAuthenticatedMailService() {
        return mailService;
    }
}