package org.example;

import org.example.service.MailService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class MailClientFrame extends JFrame {

    private final MailService mailService;
    private final EmailListPanel emailListPanel;
    private final EmailDetailPanel emailDetailPanel;
    private final JTextField searchField;
    private final JButton refreshButton;
    private final JButton composeButton;

    public MailClientFrame(MailService mailService) {
        this.mailService = mailService;
        this.emailListPanel = new EmailListPanel();
        this.emailDetailPanel = new EmailDetailPanel();
        this.searchField = new JTextField(20);
        this.refreshButton = new JButton("Обновить");
        this.composeButton = new JButton("Написать");

        initUI();
        setupListeners();
    }

    private void initUI() {
        setTitle("MailWave Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Поиск:"));
        topPanel.add(searchField);
        topPanel.add(refreshButton);
        topPanel.add(composeButton);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                emailListPanel,
                emailDetailPanel
        );
        splitPane.setDividerLocation(350);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
    }

    private void setupListeners() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { performSearch(); }
            @Override
            public void removeUpdate(DocumentEvent e) { performSearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { performSearch(); }
        });

        emailListPanel.setOnEmailSelectedListener(emailDetailPanel::displayEmail);

        refreshButton.addActionListener(e -> loadEmailsAsync());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                loadEmailsAsync();
            }
        });

        composeButton.addActionListener(e -> openComposeDialog());
    }

    private void performSearch() {
        String query = searchField.getText();
        List<Email> filtered = mailService.filterEmails(query);
        emailListPanel.updateList(filtered);
    }

    private void loadEmailsAsync() {
        refreshButton.setEnabled(false);
        refreshButton.setText("Загрузка...");

        SwingWorker<List<Email>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Email> doInBackground() throws Exception {
                return mailService.fetchEmails();
            }

            @Override
            protected void done() {
                try {
                    get();
                    emailListPanel.updateList(mailService.filterEmails(searchField.getText()));
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(
                            MailClientFrame.this,
                            "Ошибка при получении писем: " + cause.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    refreshButton.setText("Обновить");
                    refreshButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void openComposeDialog() {
        ComposeDialog composeDialog = new ComposeDialog(this, (to, subject, body, attachments) -> {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    mailService.sendEmail(to, subject, body, attachments);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(MailClientFrame.this, "Письмо успешно отправлено!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                MailClientFrame.this,
                                "Ошибка отправки: " + ex.getMessage(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            worker.execute();
        });
        composeDialog.setVisible(true);
    }
}