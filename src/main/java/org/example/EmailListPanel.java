package org.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EmailListPanel extends JPanel {

    private static final SimpleDateFormat SDF_TODAY = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat SDF_OLD   = new SimpleDateFormat("MMM d");

    public enum Filter { ALL, UNREAD, STARRED, ATTACHMENTS }

    private final DefaultListModel<Email> model = new DefaultListModel<>();
    private final JList<Email> list;
    private final JLabel countLabel = new JLabel();
    private Filter activeFilter = Filter.ALL;
    private List<Email> sourceEmails = new ArrayList<>();

    private Consumer<Email> onStarToggled;
    private Consumer<Email> onEmailSelectedListener;

    public EmailListPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PANEL);

        list = buildList();

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.BG_PANEL);
        styleScrollBar(scroll.getVerticalScrollBar());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.BG_DARKEST);
        top.add(buildHeader(),    BorderLayout.NORTH);
        top.add(buildFilterBar(), BorderLayout.SOUTH);

        add(top,    BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public JList<Email> getList() { return list; }

    public void setOnStarToggled(Consumer<Email> c) { this.onStarToggled = c; }

    public void setOnEmailSelectedListener(Consumer<Email> listener) {
        this.onEmailSelectedListener = listener;
    }

    public void updateList(List<Email> emails) {
        setEmails(emails);
    }

    public void setEmails(List<Email> emails) {
        sourceEmails = emails != null ? new ArrayList<>(emails) : new ArrayList<>();
        applyFilter();
    }

    public void clear() {
        sourceEmails.clear();
        model.clear();
        countLabel.setText("");
    }

    private void applyFilter() {
        model.clear();
        List<Email> filtered = filter(sourceEmails, activeFilter);
        for (Email e : filtered) model.addElement(e);
        int n = filtered.size();
        countLabel.setText(n + " message" + (n != 1 ? "s" : ""));
    }

    static List<Email> filter(List<Email> emails, Filter filter) {
        List<Email> filtered = new ArrayList<>();
        for (Email e : emails) {
            switch (filter) {
                case UNREAD      -> { if (!e.isRead())                      filtered.add(e); }
                case STARRED     -> { if (e.isStarred())                    filtered.add(e); }
                case ATTACHMENTS -> { if (!e.getAttachments().isEmpty())    filtered.add(e); }
                default          -> filtered.add(e);
            }
        }
        return filtered;
    }

    void setFilter(Filter filter) {
        this.activeFilter = filter;
        applyFilter();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARKEST);
        header.setBorder(new EmptyBorder(12, 16, 8, 16));

        JLabel title = new JLabel("INBOX");
        title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        title.setForeground(Theme.TEXT_MUTED);
        header.add(title, BorderLayout.WEST);

        countLabel.setFont(Theme.FONT_BADGE);
        countLabel.setForeground(Theme.TEXT_ACCENT);
        header.add(countLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        bar.setBackground(Theme.BG_DARKEST);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        String[][] tabs = {{"All", "ALL"}, {"Unread", "UNREAD"}, {"Starred", "STARRED"}, {"Attachments", "ATTACHMENTS"}};
        ButtonGroup group = new ButtonGroup();
        for (String[] tab : tabs) {
            JToggleButton btn = new JToggleButton(tab[0]);
            btn.setFont(Theme.FONT_SMALL);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(new EmptyBorder(5, 10, 5, 10));
            Filter f = Filter.valueOf(tab[1]);
            styleFilterBtn(btn, false);
            if (f == Filter.ALL) { styleFilterBtn(btn, true); btn.setSelected(true); }
            btn.addActionListener(e -> {
                activeFilter = f;
                for (Component c : bar.getComponents())
                    if (c instanceof JToggleButton tb) styleFilterBtn(tb, tb == btn);
                applyFilter();
            });
            group.add(btn);
            bar.add(btn);
        }
        return bar;
    }

    private void styleFilterBtn(JToggleButton btn, boolean active) {
        btn.setBackground(active ? Theme.ACCENT_DIM : Theme.BG_DARKEST);
        btn.setForeground(active ? Theme.TEXT_ACCENT : Theme.TEXT_MUTED);
    }

    private JList<Email> buildList() {
        JList<Email> lst = new JList<>(model);
        lst.setCellRenderer(new EmailCellRenderer());
        lst.setBackground(Theme.BG_PANEL);
        lst.setSelectionBackground(Theme.BG_SELECTED);
        lst.setSelectionForeground(Theme.TEXT_PRIMARY);
        lst.setFixedCellHeight(76);
        lst.setBorder(BorderFactory.createEmptyBorder());

        lst.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Email selected = lst.getSelectedValue();
                if (selected != null && onEmailSelectedListener != null) {
                    onEmailSelectedListener.accept(selected);
                }
            }
        });

        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Theme.BG_CARD);
        popup.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JMenuItem toggleRead    = menuItem("Mark as unread");
        JMenuItem toggleStarred = menuItem("Star");
        JMenuItem copyFrom      = menuItem("Copy sender address");

        toggleRead.addActionListener(e -> {
            Email sel = lst.getSelectedValue();
            if (sel != null) {
                sel.setRead(!sel.isRead());
                toggleRead.setText(sel.isRead() ? "Mark as unread" : "Mark as read");
                lst.repaint();
                applyFilter();
            }
        });

        toggleStarred.addActionListener(e -> {
            Email sel = lst.getSelectedValue();
            if (sel != null) {
                sel.setStarred(!sel.isStarred());
                toggleStarred.setText(sel.isStarred() ? "Unstar" : "Star");
                lst.repaint();
                applyFilter();
                if (onStarToggled != null) onStarToggled.accept(sel);
            }
        });

        copyFrom.addActionListener(e -> {
            Email sel = lst.getSelectedValue();
            if (sel != null && sel.getSender() != null) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new java.awt.datatransfer.StringSelection(sel.getSender()), null);
            }
        });

        popup.add(toggleRead);
        popup.add(toggleStarred);
        popup.addSeparator();
        popup.add(copyFrom);

        lst.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e)  { maybePopup(e); }
            @Override
            public void mouseReleased(MouseEvent e) { maybePopup(e); }
            private void maybePopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int idx = lst.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        lst.setSelectedIndex(idx);
                        Email sel = lst.getModel().getElementAt(idx);
                        toggleRead.setText(sel.isRead() ? "Mark as unread" : "Mark as read");
                        toggleStarred.setText(sel.isStarred() ? "Unstar" : "Star");
                    }
                    popup.show(lst, e.getX(), e.getY());
                }
            }
        });

        return lst;
    }

    private JMenuItem menuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setBackground(Theme.BG_CARD);
        item.setForeground(Theme.TEXT_PRIMARY); // Исправлен цвет текста
        item.setFont(Theme.FONT_BODY);
        item.setBorder(new EmptyBorder(6, 14, 6, 14));
        return item;
    }

    private class EmailCellRenderer extends JPanel implements ListCellRenderer<Email> {

        private final JLabel unreadDot    = new JLabel("●");
        private final JLabel starLabel    = new JLabel();
        private final JLabel fromLabel    = new JLabel();
        private final JLabel subjectLabel = new JLabel();
        private final JLabel dateLabel    = new JLabel();
        private final JLabel snippetLabel = new JLabel();
        private final JLabel attachIcon   = new JLabel("[ATTACH]");

        EmailCellRenderer() {
            setLayout(new BorderLayout(6, 0));
            setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

            JPanel leftCol = new JPanel();
            leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
            leftCol.setOpaque(false);
            leftCol.setBorder(new EmptyBorder(12, 14, 12, 4));
            leftCol.setPreferredSize(new Dimension(28, 70));

            unreadDot.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 8));
            unreadDot.setForeground(Theme.ACCENT);
            unreadDot.setAlignmentX(Component.CENTER_ALIGNMENT);

            starLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 13));
            starLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            leftCol.add(Box.createVerticalGlue());
            leftCol.add(unreadDot);
            leftCol.add(Box.createVerticalStrut(6));
            leftCol.add(starLabel);
            leftCol.add(Box.createVerticalGlue());
            add(leftCol, BorderLayout.WEST);

            JPanel center = new JPanel(new GridBagLayout());
            center.setOpaque(false);
            center.setBorder(new EmptyBorder(12, 0, 12, 0));
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
            gc.anchor = GridBagConstraints.WEST;

            fromLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 13));
            subjectLabel.setFont(Theme.FONT_BODY);
            snippetLabel.setFont(Theme.FONT_SMALL);
            snippetLabel.setForeground(Theme.TEXT_MUTED);

            gc.gridy = 0; center.add(fromLabel, gc);
            gc.gridy = 1; center.add(subjectLabel, gc);
            gc.gridy = 2; center.add(snippetLabel, gc);
            add(center, BorderLayout.CENTER);

            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setOpaque(false);
            right.setBorder(new EmptyBorder(14, 8, 14, 16));
            dateLabel.setFont(Theme.FONT_SMALL);
            dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            attachIcon.setFont(Theme.FONT_SMALL);
            attachIcon.setAlignmentX(Component.RIGHT_ALIGNMENT);
            attachIcon.setForeground(Theme.TEXT_MUTED);
            right.add(dateLabel);
            right.add(Box.createVerticalStrut(6));
            right.add(attachIcon);
            add(right, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Email> list, Email email,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            Color bg = isSelected ? Theme.BG_SELECTED : (index % 2 == 0 ? Theme.BG_PANEL : Theme.BG_CARD);
            setBackground(bg);

            String from = email.getSender() != null ? email.getSender() : "Unknown";
            if (from.length() > 30) from = from.substring(0, 28) + "…";
            fromLabel.setText(from);
            fromLabel.setForeground(isSelected ? Theme.TEXT_PRIMARY :
                    (email.isRead() ? Theme.TEXT_SECONDARY : Theme.TEXT_PRIMARY));

            String subj = email.getSubject() != null ? email.getSubject() : "(No Subject)";
            if (subj.length() > 45) subj = subj.substring(0, 43) + "…";
            subjectLabel.setText(subj);
            subjectLabel.setForeground(isSelected ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);

            String snippet = email.getBody() != null ? email.getBody().trim().replace("\n", " ") : "";
            if (snippet.length() > 55) snippet = snippet.substring(0, 53) + "…";
            snippetLabel.setText(snippet.isEmpty() ? " " : snippet);

            if (email.getSentDate() != null) {
                long age = System.currentTimeMillis() - email.getSentDate().getTime();
                dateLabel.setText(age < 86400000L
                        ? SDF_TODAY.format(email.getSentDate())
                        : SDF_OLD.format(email.getSentDate()));
            } else {
                dateLabel.setText("");
            }
            dateLabel.setForeground(isSelected ? Theme.TEXT_SECONDARY : Theme.TEXT_MUTED);

            unreadDot.setVisible(!email.isRead());
            starLabel.setText(email.isStarred() ? "★" : "");
            starLabel.setForeground(new Color(0xFF, 0xC0, 0x40));
            attachIcon.setVisible(email.getAttachments() != null && !email.getAttachments().isEmpty());

            return this;
        }
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = Theme.BG_HOVER;
                trackColor = Theme.BG_PANEL;
            }
            @Override
            protected JButton createDecreaseButton(int o) { return tiny(); }
            @Override
            protected JButton createIncreaseButton(int o) { return tiny(); }
            private JButton tiny() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }
}