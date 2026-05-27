package org.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ComposeDialog extends JDialog {

    private final EmailSender sender;
    private JTextField toField;
    private JTextField subjectField;
    private JTextArea bodyArea;
    private final List<File> attachedFiles = new ArrayList<>();
    private JPanel attachmentPanel;
    private JLabel statusLabel;

    public ComposeDialog(Frame parent, EmailSender sender) {
        super(parent, "New Message", true);
        this.sender = sender;
        buildUI();
        setSize(680, 560);
        setLocationRelativeTo(parent);
    }

    public ComposeDialog(Frame parent, EmailSender sender, String to, String subject, String quotedBody) {
        super(parent, "Reply", true);
        this.sender = sender;
        buildUI();
        toField.setText(to);
        subjectField.setText(subject);
        bodyArea.setText("\n\n------- Original Message -------\n" + quotedBody);
        bodyArea.setCaretPosition(0);
        setSize(680, 560);
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        // Header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARKEST);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Compose Message");
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.TEXT_MUTED);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_DARK);
        form.setBorder(new EmptyBorder(16, 20, 8, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        toField = createField();
        subjectField = createField();

        addFormRow(form, gbc, "To:", toField, 0);
        addFormRow(form, gbc, "Subject:", subjectField, 1);

        // Body
        bodyArea = new JTextArea();
        bodyArea.setFont(Theme.FONT_BODY);
        bodyArea.setBackground(Theme.BG_CARD);
        bodyArea.setForeground(Theme.TEXT_PRIMARY);
        bodyArea.setCaretColor(Theme.ACCENT);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyScroll.setBorder(new LineBorder(Theme.BORDER));
        bodyScroll.getViewport().setBackground(Theme.BG_CARD);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 0, 0, 0);
        form.add(bodyScroll, gbc);

        add(form, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(Theme.BG_DARKEST);
        bottom.setBorder(new EmptyBorder(10, 20, 14, 20));

        // Attachments row
        attachmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        attachmentPanel.setBackground(Theme.BG_DARKEST);

        JButton attachBtn = new JButton("Attach File");
        attachBtn.setFont(Theme.FONT_SMALL);
        attachBtn.setBackground(Theme.BG_CARD);
        attachBtn.setForeground(Theme.TEXT_SECONDARY);
        attachBtn.setFocusPainted(false);
        attachBtn.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(6, 12, 6, 12)));
        attachBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        attachBtn.addActionListener(e -> chooseAttachment());
        attachmentPanel.add(attachBtn);

        statusLabel = new JLabel("");
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);

        JPanel leftBottom = new JPanel(new BorderLayout(0, 6));
        leftBottom.setBackground(Theme.BG_DARKEST);
        leftBottom.add(attachmentPanel, BorderLayout.NORTH);
        leftBottom.add(statusLabel, BorderLayout.SOUTH);

        bottom.add(leftBottom, BorderLayout.CENTER);

        JButton sendBtn = new JButton("Send");
        sendBtn.setFont(Theme.FONT_HEADING);

        sendBtn.setContentAreaFilled(false); // Отключаем стандартный белый градиент ОС
        sendBtn.setOpaque(true);
        sendBtn.setBackground(Theme.ACCENT);
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.setBorder(new EmptyBorder(10, 24, 10, 24));
        sendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendBtn.addActionListener(e -> sendEmail());
        sendBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { sendBtn.setBackground(Theme.ACCENT_GLOW); }
            public void mouseExited(MouseEvent e) { sendBtn.setBackground(Theme.ACCENT); }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(Theme.FONT_SMALL);
        cancelBtn.setBackground(Theme.BG_CARD);
        cancelBtn.setForeground(Theme.TEXT_SECONDARY);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(6, 12, 6, 12)));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setBackground(Theme.BG_DARKEST);
        rightButtons.add(cancelBtn);
        rightButtons.add(sendBtn);
        bottom.add(rightButtons, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, String label, JComponent field, int row) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(60, 20));

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, gbc);
    }

    private JTextField createField() {
        JTextField f = new JTextField();
        f.setFont(Theme.FONT_BODY);
        f.setBackground(Theme.BG_CARD);
        f.setForeground(Theme.TEXT_PRIMARY);
        f.setCaretColor(Theme.ACCENT);
        f.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private JButton createIconButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_SMALL);
        btn.setBackground(bg);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(6, 12, 6, 12)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void chooseAttachment() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            for (File f : chooser.getSelectedFiles()) {
                attachedFiles.add(f);
                addAttachmentChip(f.getName());
            }
        }
    }

    private void addAttachmentChip(String name) {
        JLabel chip = new JLabel("[FILE] " + name + "  [X]");
        chip.setFont(Theme.FONT_SMALL);
        chip.setForeground(Theme.TEXT_ACCENT);
        chip.setBackground(Theme.ACCENT_DIM);
        chip.setOpaque(true);
        chip.setBorder(new EmptyBorder(3, 8, 3, 8));
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chip.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                attachmentPanel.remove(chip);
                attachedFiles.removeIf(f -> f.getName().equals(name));
                attachmentPanel.revalidate();
                attachmentPanel.repaint();
            }
        });
        attachmentPanel.add(chip);
        attachmentPanel.revalidate();
    }

    private void sendEmail() {
        String to = toField.getText().trim();
        String subject = subjectField.getText().trim();
        String body = bodyArea.getText();

        if (to.isEmpty() || subject.isEmpty()) {
            statusLabel.setForeground(Theme.ERROR);
            statusLabel.setText("Please fill in To and Subject fields.");
            return;
        }

        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        statusLabel.setText("Sending...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            protected Void doInBackground() throws Exception {
                sender.sendEmail(to, subject, body, attachedFiles.isEmpty() ? null : attachedFiles);
                return null;
            }
            protected void done() {
                try {
                    get();
                    statusLabel.setForeground(Theme.SUCCESS);
                    statusLabel.setText("Message sent successfully!");
                    Timer t = new Timer(1500, ev -> dispose());
                    t.setRepeats(false);
                    t.start();
                } catch (Exception ex) {
                    statusLabel.setForeground(Theme.ERROR);
                    statusLabel.setText("Error: " + ex.getCause().getMessage());
                }
            }
        };
        worker.execute();
    }
}