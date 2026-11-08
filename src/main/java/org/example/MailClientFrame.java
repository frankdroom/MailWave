package org.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class MailClientFrame extends JFrame {

    // ── Credentials (edit here or via Settings dialog) ──
    private String email    = "frankdroompc@gmail.com";
    private String password = "xxxx xxxx xxxx xxxx";

    private EmailReceiver receiver;
    private EmailSender   sender;

    private EmailListPanel   listPanel;
    private EmailDetailPanel detailPanel;
    private JLabel           statusBar;
    private JSpinner         fromSpinner;
    private JSpinner         toSpinner;
    private JButton          refreshBtn;
    private JLabel           loadingLabel;
    private List<Email>      allEmails = new ArrayList<>();
    private JTextField       searchField;

    public MailClientFrame() {
        super("MailWave");
        initServices();
        buildUI();
        applyGlobalStyle();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    private void initServices() {
        receiver = new EmailReceiver(email, password);
        sender   = new EmailSender(email, password);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UI Construction
    // ════════════════════════════════════════════════════════════════════════

    private void buildUI() {
        getContentPane().setBackground(Theme.BG_DARKEST);
        setLayout(new BorderLayout());

        add(buildSidebar(),    BorderLayout.WEST);
        add(buildMainArea(),   BorderLayout.CENTER);
        add(buildStatusBar(),  BorderLayout.SOUTH);
    }

    // ── Sidebar ─────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.BG_DARKEST);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDER));

        // Logo / app title
        JPanel logo = new JPanel(new BorderLayout());
        logo.setBackground(Theme.BG_DARKEST);
        logo.setBorder(new EmptyBorder(22, 20, 18, 20));

        JLabel appName = new JLabel("MailWave");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appName.setForeground(Theme.TEXT_PRIMARY);

        JLabel tagline = new JLabel("  ✉  Gmail Client");
        tagline.setFont(Theme.FONT_SMALL);
        tagline.setForeground(Theme.TEXT_MUTED);

        logo.add(appName, BorderLayout.NORTH);
        logo.add(tagline, BorderLayout.SOUTH);
        sidebar.add(logo, BorderLayout.NORTH);

        // Nav links
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Theme.BG_DARKEST);
        nav.setBorder(new EmptyBorder(8, 0, 8, 0));

        nav.add(navItem("📥", "Inbox", true));
        //nav.add(navItem("⭐", "Starred", false));
        //nav.add(navItem("📤", "Sent",   false));
        //nav.add(navItem("🗑", "Trash",  false));

        sidebar.add(nav, BorderLayout.CENTER);

        // Bottom: settings + account
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Theme.BG_DARKEST);
        bottom.setBorder(new EmptyBorder(12, 16, 18, 16));

        JButton settingsBtn = sidebarButton("⚙  Settings");
        settingsBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 13));
        settingsBtn.addActionListener(e -> openSettings());
        bottom.add(settingsBtn, BorderLayout.NORTH);

        JPanel account = new JPanel(new BorderLayout(8, 0));
        account.setBackground(Theme.BG_DARKEST);
        account.setBorder(new EmptyBorder(14, 0, 0, 0));

        JLabel avatar = new JLabel(getInitial(email));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setForeground(Color.WHITE);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(Theme.ACCENT);
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel accountLabel = new JLabel(email.length() > 22 ? email.substring(0, 20) + "…" : email);
        accountLabel.setFont(Theme.FONT_SMALL);
        accountLabel.setForeground(Theme.TEXT_MUTED);

        account.add(avatar, BorderLayout.WEST);
        account.add(accountLabel, BorderLayout.CENTER);

        bottom.add(account, BorderLayout.SOUTH);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel navItem(String icon, String label, boolean active) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(active ? Theme.BG_HOVER : Theme.BG_DARKEST);
        p.setBorder(new EmptyBorder(11, 20, 11, 20));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        if (active) {
            JPanel accent = new JPanel();
            accent.setBackground(Theme.ACCENT);
            accent.setPreferredSize(new Dimension(3, 42));
            p.add(accent, BorderLayout.WEST);
        }

        JLabel ic = new JLabel(icon);
        ic.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_BODY);
        lbl.setForeground(active ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);

        p.add(ic,  BorderLayout.WEST);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private JButton sidebarButton(String text) {
        JButton b = new JButton(text);
        b.setFont(Theme.FONT_SMALL);
        b.setBackground(Theme.BG_CARD);
        b.setForeground(Theme.TEXT_SECONDARY);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(7, 12, 7, 12)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return b;
    }

    // ── Main area ────────────────────────────────────────────────────────────
    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG_DARK);

        main.add(buildToolbar(), BorderLayout.NORTH);

        // Split: email list  |  detail
        listPanel   = new EmailListPanel();
        detailPanel = new EmailDetailPanel();

        listPanel.getList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Email selected = listPanel.getList().getSelectedValue();
                if (selected != null) {
                    selected.setRead(true);
                    detailPanel.showEmail(selected);
                    listPanel.getList().repaint();
                } else {
                    detailPanel.showEmpty();
                }
            }
        });

        detailPanel.setOnReply(() -> {
            Email sel = listPanel.getList().getSelectedValue();
            if (sel != null) openReply(sel);
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, detailPanel);
        split.setDividerLocation(360);
        split.setDividerSize(1);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(Theme.BORDER);

        main.add(split, BorderLayout.CENTER);
        return main;
    }

    // ── Toolbar ──────────────────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(Theme.BG_DARK);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(10, 20, 10, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(Theme.BG_DARK);

        JButton composeBtn = new JButton("New Message");
        composeBtn.setFont(Theme.FONT_BODY);
        composeBtn.setBackground(Theme.ACCENT);
        composeBtn.setForeground(Color.BLACK);
        composeBtn.setFocusPainted(false);
        composeBtn.setBorder(new EmptyBorder(8, 18, 8, 18));
        composeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        composeBtn.addActionListener(e -> new ComposeDialog(this, sender).setVisible(true));
        composeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { composeBtn.setBackground(Theme.ACCENT_GLOW); }
            public void mouseExited(MouseEvent e)  { composeBtn.setBackground(Theme.ACCENT); }
        });

        refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(Theme.FONT_BODY);
        refreshBtn.setBackground(Theme.BG_CARD);
        refreshBtn.setForeground(Theme.TEXT_SECONDARY);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(7, 14, 7, 14)));
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadEmails());
        refreshBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { refreshBtn.setBackground(Theme.BG_HOVER); }
            public void mouseExited(MouseEvent e)  { refreshBtn.setBackground(Theme.BG_CARD); }
        });

        loadingLabel = new JLabel("");
        loadingLabel.setFont(Theme.FONT_SMALL);
        loadingLabel.setForeground(Theme.TEXT_MUTED);

        left.add(composeBtn);
        left.add(refreshBtn);
        left.add(loadingLabel);
        bar.add(left, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerPanel.setBackground(Theme.BG_DARK);

        searchField = new JTextField(24);
        searchField.setFont(Theme.FONT_BODY);
        searchField.setBackground(Theme.BG_CARD);
        searchField.setForeground(Theme.TEXT_PRIMARY);
        searchField.setCaretColor(Theme.ACCENT);
        searchField.setToolTipText("Search by subject or sender");
        searchField.putClientProperty("JTextField.placeholderText", "Search messages...");
        searchField.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(7, 10, 7, 10)));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterEmails(searchField.getText()); }
        });
        centerPanel.add(searchField);
        bar.add(centerPanel, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setBackground(Theme.BG_DARK);

        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date weekAgo = cal.getTime();

        SpinnerDateModel fromModel = new SpinnerDateModel(weekAgo, null, today, Calendar.DAY_OF_MONTH);
        SpinnerDateModel toModel   = new SpinnerDateModel(today,   null, null, Calendar.DAY_OF_MONTH);

        fromSpinner = createDateSpinner(fromModel);
        toSpinner   = createDateSpinner(toModel);

        JLabel fromLbl = miniLabel("From");
        JLabel toLbl   = miniLabel("To");

        right.add(fromLbl);
        right.add(fromSpinner);
        right.add(toLbl);
        right.add(toSpinner);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JSpinner createDateSpinner(SpinnerDateModel model) {
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd MMM yyyy");
        spinner.setEditor(editor);
        spinner.setFont(Theme.FONT_SMALL);
        editor.getTextField().setBackground(Theme.BG_CARD);
        editor.getTextField().setForeground(Theme.TEXT_PRIMARY);
        editor.getTextField().setFont(Theme.FONT_SMALL);
        editor.getTextField().setBorder(new EmptyBorder(4, 6, 4, 6));
        spinner.setBorder(new LineBorder(Theme.BORDER));
        spinner.setBackground(Theme.BG_CARD);
        spinner.setPreferredSize(new Dimension(120, 30));
        return spinner;
    }

    private JLabel miniLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_MUTED);
        return l;
    }

    // ── Status bar ───────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_DARKEST);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(5, 20, 5, 20)));

        statusBar = new JLabel("Ready");
        statusBar.setFont(Theme.FONT_SMALL);
        statusBar.setForeground(Theme.TEXT_MUTED);
        bar.add(statusBar, BorderLayout.WEST);

        JLabel versionLabel = new JLabel("MailWave 1.0  •  Gmail IMAP");
        versionLabel.setFont(Theme.FONT_SMALL);
        versionLabel.setForeground(Theme.TEXT_MUTED);
        bar.add(versionLabel, BorderLayout.EAST);
        return bar;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Actions
    // ════════════════════════════════════════════════════════════════════════

    private void loadEmails() {
        Date from = (Date) fromSpinner.getValue();
        Date to   = (Date) toSpinner.getValue();

        // Make "to" end of day
        Calendar cal = Calendar.getInstance();
        cal.setTime(to);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        to = cal.getTime();
        final Date finalTo = to;

        refreshBtn.setEnabled(false);
        loadingLabel.setText("Loading…");
        statusBar.setText("Connecting to Gmail…");
        listPanel.clear();
        detailPanel.showEmpty();

        SwingWorker<List<Email>, Void> worker = new SwingWorker<>() {
            protected List<Email> doInBackground() {
                return receiver.readEmails(from, finalTo);
            }
            protected void done() {
                try {
                    allEmails = get();
                    allEmails.sort(Comparator.comparing(Email::getReceivedOn,
                            Comparator.nullsLast(Comparator.reverseOrder())));
                    listPanel.setEmails(allEmails);
                    int n = allEmails.size();
                    statusBar.setText("Loaded " + n + " message" + (n != 1 ? "s" : "") + ".");
                } catch (Exception ex) {
                    statusBar.setText("Error: " + ex.getCause().getMessage());
                    JOptionPane.showMessageDialog(MailClientFrame.this,
                            "Failed to load emails:\n" + ex.getCause().getMessage(),
                            "Connection Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    loadingLabel.setText("");
                    refreshBtn.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void filterEmails(String query) {
        if (query == null || query.isBlank()) {
            listPanel.setEmails(allEmails);
            return;
        }
        String q = query.toLowerCase();
        List<Email> filtered = new ArrayList<>();
        for (Email e : allEmails) {
            String subj = e.getSubject() != null ? e.getSubject().toLowerCase() : "";
            String from = e.getFrom() != null ? e.getFrom().toLowerCase() : "";
            String text = e.getText().toLowerCase();
            if (subj.contains(q) || from.contains(q) || text.contains(q)) {
                filtered.add(e);
            }
        }
        listPanel.setEmails(filtered);
        statusBar.setText("Found " + filtered.size() + " matching message(s).");
    }

    private void openReply(Email original) {
        String to      = original.getFrom();
        String subject = "Re: " + (original.getSubject() != null ? original.getSubject() : "");
        new ComposeDialog(this, sender, to, subject, original.getText()).setVisible(true);
    }

    private void openSettings() {
        JDialog dlg = new JDialog(this, "Account Settings", true);
        dlg.setSize(420, 220);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Theme.BG_DARK);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_DARK);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 0, 6, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailField = settingsField(email);
        JPasswordField passField = new JPasswordField(password);
        stylePasswordField(passField);

        gc.gridx=0; gc.gridy=0; gc.weightx=0;
        form.add(settingsLabel("Email:"), gc);
        gc.gridx=1; gc.weightx=1;
        form.add(emailField, gc);
        gc.gridx=0; gc.gridy=1; gc.weightx=0;
        form.add(settingsLabel("App Password:"), gc);
        gc.gridx=1; gc.weightx=1;
        form.add(passField, gc);

        dlg.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setBackground(Theme.BG_DARKEST);

        JButton save = new JButton("Save");
        save.setBackground(Theme.ACCENT);
        save.setForeground(Color.WHITE);
        save.setFont(Theme.FONT_BODY);
        save.setFocusPainted(false);
        save.setBorder(new EmptyBorder(7, 18, 7, 18));
        save.addActionListener(e -> {
            email    = emailField.getText().trim();
            password = new String(passField.getPassword());
            initServices();
            statusBar.setText("Account updated. Click Refresh to load emails.");
            dlg.dispose();
        });

        JButton cancel = new JButton("Cancel");
        cancel.setBackground(Theme.BG_CARD);
        cancel.setForeground(Theme.TEXT_SECONDARY);
        cancel.setFont(Theme.FONT_SMALL);
        cancel.setFocusPainted(false);
        cancel.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(7, 14, 7, 14)));
        cancel.addActionListener(e -> dlg.dispose());

        btns.add(cancel);
        btns.add(save);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JTextField settingsField(String val) {
        JTextField f = new JTextField(val);
        f.setFont(Theme.FONT_BODY);
        f.setBackground(Theme.BG_CARD);
        f.setForeground(Theme.TEXT_PRIMARY);
        f.setCaretColor(Theme.ACCENT);
        f.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private void stylePasswordField(JPasswordField f) {
        f.setFont(Theme.FONT_BODY);
        f.setBackground(Theme.BG_CARD);
        f.setForeground(Theme.TEXT_PRIMARY);
        f.setCaretColor(Theme.ACCENT);
        f.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(6, 8, 6, 8)));
    }

    private JLabel settingsLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════════════

    private void applyGlobalStyle() {
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("SplitPane.background", Theme.BORDER);
        UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
        UIManager.put("OptionPane.background", Theme.BG_DARK);
        UIManager.put("Panel.background", Theme.BG_DARK);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT_PRIMARY);
    }

    private String getInitial(String email) {
        if (email == null || email.isEmpty()) return "?";
        return String.valueOf(Character.toUpperCase(email.charAt(0)));
    }
}