package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ComposeDialog extends JDialog {

    @FunctionalInterface
    public interface SendEmailCallback {
        void send(String to, String subject, String body, List<String> attachmentPaths) throws Exception;
    }

    private final JTextField toField = new JTextField();
    private final JTextField subjectField = new JTextField();
    private final JTextArea bodyArea = new JTextArea();
    private final DefaultListModel<String> attachmentsModel = new DefaultListModel<>();
    private final List<String> attachmentPaths = new ArrayList<>();
    private final SendEmailCallback callback;

    public ComposeDialog(Frame parent, SendEmailCallback callback) {
        super(parent, "Новое письмо", true);
        this.callback = callback;
        initUI();
    }

    private void initUI() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        fieldsPanel.add(new JLabel("Кому:"));
        fieldsPanel.add(toField);
        fieldsPanel.add(new JLabel("Тема:"));
        fieldsPanel.add(subjectField);

        JList<String> attachmentsList = new JList<>(attachmentsModel);
        JButton addAttachmentBtn = new JButton("Прикрепить файл");
        addAttachmentBtn.addActionListener(e -> selectAttachment());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton sendBtn = new JButton("Отправить");
        sendBtn.addActionListener(e -> onSendClicked());
        bottomPanel.add(addAttachmentBtn);
        bottomPanel.add(sendBtn);

        add(fieldsPanel, BorderLayout.NORTH);
        add(new JScrollPane(bodyArea), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void selectAttachment() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            attachmentPaths.add(selectedFile.getAbsolutePath());
            attachmentsModel.addElement(selectedFile.getName());
        }
    }

    private void onSendClicked() {
        if (callback != null) {
            try {
                callback.send(
                        toField.getText().trim(),
                        subjectField.getText().trim(),
                        bodyArea.getText(),
                        attachmentPaths
                );
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Ошибка отправки: " + ex.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}