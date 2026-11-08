package org.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;

public class EmailDetailPanel extends JPanel {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("EEEE, MMMM d, yyyy  HH:mm");

    private JLabel   subjectLabel;
    private JLabel   fromLabel;
    private JLabel   dateLabel;
    private JTextArea bodyArea;
    private JPanel   attachmentsPanel;
    private JPanel   contentPanel;
    private JButton  starBtn;
    private Email    currentEmail;

    private Runnable         onReply;
    private Consumer<Email>  onStarToggled;

    public EmailDetailPanel() {
        setLayout(new CardLayout());
        setBackground(Theme.BG_PANEL);
        buildEmptyState();
        buildContentPanel();
    }

    public void setOnReply(Runnable r)             { this.onReply = r; }
    public void setOnStarToggled(Consumer<Email> c){ this.onStarToggled = c; }

    private void buildEmptyState() {
        JPanel emptyState = new JPanel(new GridBagLayout());
        emptyState.setBackground(Theme.BG_PANEL);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Theme.BG_PANEL);

        JLabel icon = new JLabel("✉");
        icon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 52));
        icon.setForeground(Theme.TEXT_MUTED);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("Select a message to read");
        msg.setFont(Theme.FONT_BODY);
        msg.setForeground(Theme.TEXT_MUTED);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(icon);
        center.add(Box.createVerticalStrut(12));
        center.add(msg);
        emptyState.add(center);
        add(emptyState, "empty");
    }

    private void buildContentPanel() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Theme.BG_PANEL);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(Theme.BG_DARKEST);
        topHeader.setBorder(new EmptyBorder(20, 24, 16, 24));

        subjectLabel = new JLabel("Subject");
        subjectLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
        subjectLabel.setForeground(Theme.TEXT_PRIMARY);

        JPanel headerMeta = new JPanel();
        headerMeta.setLayout(new BoxLayout(headerMeta, BoxLayout.Y_AXIS));
        headerMeta.setBackground(Theme.BG_DARKEST);
        headerMeta.setBorder(new EmptyBorder(10, 0, 0, 0));

        fromLabel = new JLabel();
        fromLabel.setFont(Theme.FONT_BODY);
        fromLabel.setForeground(Theme.TEXT_SECONDARY);

        dateLabel = new JLabel();
        dateLabel.setFont(Theme.FONT_SMALL);
        dateLabel.setForeground(Theme.TEXT_MUTED);

        headerMeta.add(fromLabel);
        headerMeta.add(Box.createVerticalStrut(4));
        headerMeta.add(dateLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setBackground(Theme.BG_DARKEST);

        starBtn = new JButton("☆");
        starBtn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));

        // Сбрасываем системный менеджер Look and Feel, чтобы цвет фона работал корректно
        starBtn.setContentAreaFilled(false);
        starBtn.setOpaque(true);

        starBtn.setBackground(Theme.BG_CARD);
        starBtn.setForeground(new Color(0xFF, 0xC0, 0x40));
        starBtn.setFocusPainted(false);
        starBtn.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(5, 10, 5, 10)));
        starBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        starBtn.setToolTipText("Star this message");
        starBtn.addActionListener(e -> toggleStar());
        starBtn.addMouseListener(hoverEffect(starBtn, Theme.BG_CARD, Theme.BG_HOVER));

        JButton replyBtn = actionButton("Reply", Theme.ACCENT_DIM, Theme.TEXT_ACCENT);
        replyBtn.addActionListener(e -> { if (onReply != null) onReply.run(); });

        actions.add(starBtn);
        actions.add(replyBtn);

        JPanel rightHeader = new JPanel(new BorderLayout());
        rightHeader.setBackground(Theme.BG_DARKEST);
        rightHeader.add(actions, BorderLayout.NORTH);

        topHeader.add(subjectLabel, BorderLayout.NORTH);
        topHeader.add(headerMeta,  BorderLayout.CENTER);
        topHeader.add(rightHeader, BorderLayout.EAST);

        bodyArea = new JTextArea();
        bodyArea.setEditable(false);
        bodyArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bodyArea.setBackground(Theme.BG_PANEL);
        bodyArea.setForeground(Theme.TEXT_PRIMARY);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setBorder(new EmptyBorder(20, 24, 20, 24));

        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyScroll.setBorder(BorderFactory.createEmptyBorder());
        bodyScroll.getViewport().setBackground(Theme.BG_PANEL);
        styleScrollBar(bodyScroll.getVerticalScrollBar());

        attachmentsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        attachmentsPanel.setBackground(Theme.BG_DARKEST);
        attachmentsPanel.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));
        attachmentsPanel.setVisible(false);

        contentPanel.add(topHeader,       BorderLayout.NORTH);
        contentPanel.add(bodyScroll,      BorderLayout.CENTER);
        contentPanel.add(attachmentsPanel, BorderLayout.SOUTH);

        add(contentPanel, "content");
        ((CardLayout) getLayout()).show(this, "empty");
    }

    public void showEmail(Email email) {
        currentEmail = email;

        String subject = email.getSubject() != null ? email.getSubject() : "(No Subject)";
        subjectLabel.setText(subject);
        fromLabel.setText("From:  " + (email.getFrom() != null ? email.getFrom() : "Unknown"));
        dateLabel.setText(email.getReceivedOn() != null ? SDF.format(email.getReceivedOn()) : "");
        bodyArea.setText(email.getText().trim());
        bodyArea.setCaretPosition(0);

        updateStarButton(email.isStarred());

        attachmentsPanel.removeAll();
        if (!email.getAttachments().isEmpty()) {
            JLabel attLabel = new JLabel("Attachments:");
            attLabel.setFont(Theme.FONT_SMALL);
            attLabel.setForeground(Theme.TEXT_MUTED);
            attLabel.setBorder(new EmptyBorder(0, 4, 0, 4));
            attachmentsPanel.add(attLabel);

            for (FileByteData att : email.getAttachments()) {
                attachmentsPanel.add(buildAttachmentChip(att));
            }

            if (email.getAttachments().size() > 1) {
                JButton saveAll = actionButton("Save All", Theme.BG_CARD, Theme.TEXT_SECONDARY);
                saveAll.addActionListener(e -> saveAllAttachments(email));
                attachmentsPanel.add(saveAll);
            }

            attachmentsPanel.setVisible(true);
        } else {
            attachmentsPanel.setVisible(false);
        }
        attachmentsPanel.revalidate();

        ((CardLayout) getLayout()).show(this, "content");
    }

    public void showEmpty() {
        currentEmail = null;
        ((CardLayout) getLayout()).show(this, "empty");
    }

    private void toggleStar() {
        if (currentEmail == null) return;
        currentEmail.setStarred(!currentEmail.isStarred());
        updateStarButton(currentEmail.isStarred());
        if (onStarToggled != null) onStarToggled.accept(currentEmail);
    }

    private void updateStarButton(boolean starred) {
        starBtn.setText(starred ? "★" : "☆");
        starBtn.setToolTipText(starred ? "Remove star" : "Star this message");
    }

    private JButton buildAttachmentChip(FileByteData att) {
        long kb = Math.max(1, att.getData().length / 1024);
        JButton btn = new JButton(att.getFilename() + "  (" + kb + " KB)");
        btn.setFont(Theme.FONT_SMALL);
        btn.setBackground(Theme.BG_CARD);
        btn.setForeground(Theme.TEXT_ACCENT);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(5, 10, 5, 10)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Save: " + att.getFilename());
        btn.addMouseListener(hoverEffect(btn, Theme.BG_CARD, Theme.BG_HOVER));
        btn.addActionListener(e -> saveSingleAttachment(att));
        return btn;
    }

    private void saveSingleAttachment(FileByteData att) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(att.getFilename()));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.write(chooser.getSelectedFile().toPath(), att.getData());
                showBanner("Saved: " + chooser.getSelectedFile().getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveAllAttachments(Email email) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose folder to save all attachments");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            int saved = 0;
            for (FileByteData att : email.getAttachments()) {
                try {
                    Files.write(new File(dir, att.getFilename()).toPath(), att.getData());
                    saved++;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to save " + att.getFilename() + ":\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            showBanner("Saved " + saved + " file(s) to " + dir.getName());
        }
    }

    private JButton actionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_SMALL);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(bg.darker()), new EmptyBorder(6, 14, 6, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(hoverEffect(btn, bg, bg.brighter()));
        return btn;
    }

    private void showBanner(String msg) {
        JWindow banner = new JWindow(SwingUtilities.getWindowAncestor(this));
        JLabel lbl = new JLabel("  " + msg + "  ");
        lbl.setFont(Theme.FONT_BODY);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        lbl.setBackground(Theme.SUCCESS);
        lbl.setOpaque(true);
        lbl.setBorder(new EmptyBorder(8, 16, 8, 16));
        banner.add(lbl);
        banner.pack();

        Point loc = getLocationOnScreen();
        int x = loc.x + (getWidth() - banner.getWidth()) / 2;
        int y = loc.y + getHeight() - banner.getHeight() - 20;
        banner.setLocation(x, y);
        banner.setVisible(true);

        Timer t = new Timer(2200, e -> banner.dispose());
        t.setRepeats(false);
        t.start();
    }

    private MouseAdapter hoverEffect(JButton btn, Color normal, Color hover) {
        return new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        };
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                thumbColor = Theme.BG_HOVER;
                trackColor = Theme.BG_PANEL;
            }
            protected JButton createDecreaseButton(int o) { return tiny(); }
            protected JButton createIncreaseButton(int o) { return tiny(); }
            private JButton tiny() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }
}