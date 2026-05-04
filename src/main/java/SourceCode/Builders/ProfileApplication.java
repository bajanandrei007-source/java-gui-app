package SourceCode.Builders;

import SourceCode.UserInstances.PlayerSession;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * ProfileApplication — view/edit the current player's profile.
 *
 * Displays:
 *  - Username (editable via "Edit" button → inline text field → "Save")
 *  - Live stats pulled from PlayerSession: total points, solved counts per tier,
 *    and a visual progress bar.
 *  - The player's current rank on the in-memory leaderboard.
 *
 * CRUD notes:
 *  - CREATE  — handled by Login / Registration (PlayerSession.login()).
 *  - READ    — this screen reads live data from PlayerSession on open.
 *  - UPDATE  — the "Edit" button lets the player rename themselves in-memory.
 *  - DELETE  — "Delete Account" resets the session and returns to login.
 */
public class ProfileApplication extends JPanel {

    private static final double SCALE = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0;

    private static int s(int base) {
        return (int) Math.round(base * SCALE);
    }

    private static final Color  BG_DARK      = new Color(42,  37,  64),
                                BG_PANEL     = new Color(30,  27,  48),
                                BG_SIDEBAR   = new Color(26,  23,  40),
                                BG_CARD      = new Color(49,  45,  74),
                                ACCENT_GREEN = new Color(63, 185,  80),
                                ACCENT_GOLD  = new Color(210, 153,  34),
                                ACCENT_RED   = new Color(248,  81,  73),
                                TEXT_PRIMARY = new Color(230, 224, 255),
                                TEXT_MUTED   = new Color(138, 133, 170),
                                BORDER_COLOR = new Color(74,  69, 112);
    private static final Font   PIXEL_FONT   = new Font("Courier New", Font.BOLD, 12),
                                PIXEL_SMALL  = new Font("Courier New", Font.BOLD,  11),
                                PIXEL_LARGE  = new Font("Courier New", Font.BOLD, 15);

    private static final int TOTAL_EASY   = 5,
    						 TOTAL_MEDIUM = 3,
    						 TOTAL_HARD   = 3,
    						 TOTAL_ALL    = TOTAL_EASY + TOTAL_MEDIUM + TOTAL_HARD;

    private final Runnable onBack, onLogin;

    /**
     * Live-updating stat labels held as instance fields so that
     * {@link #refresh()} can push new values without rebuilding the panel.
     */
    private JLabel liveUsername, liveRank, livePoints,
				   liveTotalSolved, liveEasy, liveMedium,
				   liveHard, liveProgressRight;
    private JPanel liveBarBg;
    private double liveProgress = 0.0;
    private final Image rawDeleteImg, rawLogoutImg;

    // ── Rank cache — set externally by the API layer ───────────────
    /** Cached rank string pushed in by the API caller via {@link #setRank(String)}. */
    private String cachedRank = "Unranked";

    public ProfileApplication(Runnable onBack, Runnable onLogin) {
        this.onBack  = onBack;
        this.onLogin = onLogin;

        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        rawDeleteImg = new ImageIcon(getClass().getResource("/Buttons/DeleteAccountButton.png")).getImage();
        rawLogoutImg = new ImageIcon(getClass().getResource("/Buttons/LogOutButton.png")).getImage();

        add(buildSidebar(),      BorderLayout.WEST);
        add(buildProfilePanel(), BorderLayout.CENTER);
    }

    /**
     * Reads the latest values from {@link PlayerSession} and pushes them
     * into every live-stat label on screen.
     */
    public void refresh() {
		
		PlayerSession session = PlayerSession.getInstance();
		System.out.println("--- REFRESH TRIGGERED ---");
		System.out.println("Guard check (liveUsername == null)? " + (liveUsername == null));
    
		System.out.println("Session Memory ID: " + System.identityHashCode(session));
		System.out.println("Session Username:  " + session.getUsername());
		System.out.println("Session Points:    " + session.getPoints());
		
        int    pts         = session.getPoints();
        int    easySolved  = session.solvedCount("EASY");
        int    medSolved   = session.solvedCount("MEDIUM");
        int    hardSolved  = session.solvedCount("HARD");
        int    totalSolved = session.totalSolved();
        String rank        = computeRank();
        String username    = session.getUsername();
        liveProgress = TOTAL_ALL > 0 ? (double) totalSolved / TOTAL_ALL : 0.0;

        // Guard: labels are null until buildProfilePanel() has run
        if (liveUsername == null) return;
		
        liveUsername.setText(username);
        liveRank.setText(rank);
        livePoints.setText(pts + " pts");
        liveTotalSolved.setText(totalSolved + " / " + TOTAL_ALL);
        liveEasy.setText(easySolved  + " / " + TOTAL_EASY   + "  (Easy)");
        liveMedium.setText(medSolved + " / " + TOTAL_MEDIUM + "  (Medium)");
        liveHard.setText(hardSolved  + " / " + TOTAL_HARD   + "  (Hard)");
        liveProgressRight.setText(totalSolved + " / " + TOTAL_ALL + " challenges");
        liveBarBg.repaint();    // triggers paintComponent with the updated liveProgress
    }

    // ── Rank helpers ──────────────────────────────────────────────
    /**
     * Returns the locally-cached rank string.
     * The leaderboard is now fetched from the API, so rank is no longer
     * computed in-memory. Call {@link #setRank(String)} from your ApiClient
     * callback after the leaderboard response arrives.
     */
    private String computeRank() {
        return cachedRank;
    }

    /**
     * Pushes a rank string received from the API into this panel.
     * Immediately updates the on-screen label if the panel is already built.
     *
     * Example from your ApiClient callback:
     * <pre>
     *   profilePanel.setRank("#3");
     * </pre>
     *
     * @param rank Rank string to display, e.g. {@code "#3"} or {@code "Unranked"}.
     */
    public void setRank(String rank) {
        this.cachedRank = (rank == null || rank.isBlank()) ? "Unranked" : rank.trim();
        if (liveRank != null) liveRank.setText(this.cachedRank);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 2, BORDER_COLOR));
        sidebar.setPreferredSize(new Dimension(130, 0));

        ImageIcon rawIcon = new ImageIcon(getClass().getResource("/school_logo.png"));
        Image scaledImg = rawIcon.getImage().getScaledInstance(s(90), s(90), Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImg));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel schoolLabel = new JLabel("OLFU", SwingConstants.CENTER);
        schoolLabel.setFont(PIXEL_SMALL);
        schoolLabel.setForeground(ACCENT_GREEN);
        schoolLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(logoLabel);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(schoolLabel);
        sidebar.add(Box.createVerticalStrut(14));

        for (String item : new String[]{ "<- Back" }) {
            JPanel nav = makeNavItem(item);
            if (item.equals("<- Back")) {
                nav.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        onBack.run();
                    }
                });
            }
            sidebar.add(nav);
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JPanel makeNavItem(String text) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(BG_SIDEBAR);
        item.setMaximumSize(new Dimension(130, 28));
        item.setPreferredSize(new Dimension(130, 28));
        item.setBorder(new MatteBorder(0, 3, 0, 0, BG_SIDEBAR));

        JLabel lbl = new JLabel(text);
        lbl.setFont(PIXEL_SMALL);
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(0, 14, 0, 0));
        item.add(lbl, BorderLayout.CENTER);

        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                item.setBackground(BG_CARD);
                item.setBorder(new MatteBorder(0, 3, 0, 0, ACCENT_GREEN));
                lbl.setForeground(TEXT_PRIMARY);
            }
            @Override public void mouseExited(MouseEvent e) {
                item.setBackground(BG_SIDEBAR);
                item.setBorder(new MatteBorder(0, 3, 0, 0, BG_SIDEBAR));
                lbl.setForeground(TEXT_MUTED);
            }
        });
        return item;
    }
	
    private JPanel buildProfilePanel() {
        PlayerSession session  = PlayerSession.getInstance();
        int    pts         = session.getPoints();
        int    easySolved  = session.solvedCount("EASY");
        int    medSolved   = session.solvedCount("MEDIUM");
        int    hardSolved  = session.solvedCount("HARD");
        int    totalSolved = session.totalSolved();
        String rank        = computeRank();
        String username    = session.getUsername();
        liveProgress = TOTAL_ALL > 0 ? (double) totalSolved / TOTAL_ALL : 0.0;

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_PANEL);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 2),
            new EmptyBorder(24, 38, 24, 38)
        ));
		
        card.setPreferredSize(new Dimension(500, 480));
        card.add(centeredLabel("PLAYER PROFILE", PIXEL_LARGE, ACCENT_GREEN));
        card.add(Box.createVerticalStrut(3));
        card.add(centeredLabel("─────────────────────────", PIXEL_SMALL, BORDER_COLOR));
        card.add(Box.createVerticalStrut(10));

        JPanel usernameRow = new JPanel(new BorderLayout());
        usernameRow.setOpaque(false);
        usernameRow.setMaximumSize(new Dimension(420, 30));
        usernameRow.setPreferredSize(new Dimension(420, 30));

        JLabel uKey = new JLabel("Username");
        uKey.setFont(PIXEL_SMALL);
        uKey.setForeground(TEXT_MUTED);
        uKey.setPreferredSize(new Dimension(130, 30));

        JLabel    uVal  = new JLabel(username);
        uVal.setFont(PIXEL_FONT);
        uVal.setForeground(TEXT_PRIMARY);
        liveUsername = uVal;

        JTextField uEdit = new JTextField(username);
        uEdit.setFont(PIXEL_FONT);
        uEdit.setForeground(new Color(30, 25, 55));
        uEdit.setBackground(new Color(200, 198, 220));
        uEdit.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(1, 4, 1, 4)
        ));
        uEdit.setVisible(false);

        JButton editBtn = new JButton("Edit");
        editBtn.setFont(PIXEL_SMALL);
        editBtn.setForeground(ACCENT_GREEN);
        editBtn.setBackground(BG_CARD);
        editBtn.setFocusPainted(false);
        editBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_GREEN, 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        CardLayout cardLayout = new CardLayout();
        JPanel uCenter = new JPanel(cardLayout);
        uCenter.setOpaque(true);
        uCenter.setBackground(BG_PANEL);
        uVal.setOpaque(false);
        uCenter.add(uVal,  "VIEW");
        uCenter.add(uEdit, "EDIT");
        cardLayout.show(uCenter, "VIEW");

        editBtn.addActionListener(e -> {
            boolean editing = editBtn.getText().equals("Save");
            if (!editing) {
                // Switch to edit mode — pre-fill the text field with the current name
                uEdit.setText(uVal.getText());
                cardLayout.show(uCenter, "EDIT");
                uEdit.requestFocusInWindow();
                editBtn.setText("Save");
            } else {
                String newName = uEdit.getText().trim();
                if (newName.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Username cannot be empty.",
                        "Invalid Username", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (newName.equals(session.getUsername())) {
                    cardLayout.show(uCenter, "VIEW");
                    editBtn.setText("Edit");
                    return;
                }

                // Disable button and show loading state while the API call runs
                editBtn.setEnabled(false);
                editBtn.setText("...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Run API calls on a background thread so the UI stays responsive
                new Thread(() -> {
                    try {
                        SourceCode.UserInstances.ApiClient api = new SourceCode.UserInstances.ApiClient();

                        // Step 1: Check if the username is already taken
                        boolean taken = api.checkUsernameTaken(newName);
                        if (taken) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                    ProfileApplication.this,
                                    "The username \"" + newName + "\" is already taken.\nPlease choose a different one.",
                                    "Username Unavailable",
                                    JOptionPane.ERROR_MESSAGE);
                                editBtn.setEnabled(true);
                                editBtn.setText("Save");
                                setCursor(Cursor.getDefaultCursor());
                                uEdit.requestFocusInWindow();
                            });
                            return;
                        }

                        // Step 2: Persist the new username to the database
                        api.updateUsername(newName);

                        // Step 3: Update UI on success
                        SwingUtilities.invokeLater(() -> {
                            uVal.setText(newName);
                            if (liveRank != null) liveRank.setText(computeRank());
                            cardLayout.show(uCenter, "VIEW");
                            editBtn.setEnabled(true);
                            editBtn.setText("Edit");
                            setCursor(Cursor.getDefaultCursor());
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                ProfileApplication.this,
                                "Failed to update username:\n" + ex.getMessage(),
                                "Update Error",
                                JOptionPane.ERROR_MESSAGE);
                            editBtn.setEnabled(true);
                            editBtn.setText("Save");
                            setCursor(Cursor.getDefaultCursor());
                            uEdit.requestFocusInWindow();
                        });
                    }
                }).start();
            }
        });

        usernameRow.add(uKey,    BorderLayout.WEST);
        usernameRow.add(uCenter, BorderLayout.CENTER);
        usernameRow.add(editBtn, BorderLayout.EAST);
        card.add(usernameRow);
        card.add(Box.createVerticalStrut(4));

        card.add(Box.createVerticalStrut(10));
        card.add(centeredLabel("─── STATS ───", PIXEL_SMALL, BORDER_COLOR));
        card.add(Box.createVerticalStrut(8));

        /*
         * Bug fix: instead of passing string literals directly to infoRow()
         * (which creates anonymous JLabels that can never be updated), we now
         * create the value labels ourselves, store them in the live-label
         * instance fields, and hand them to infoRowWithLabel().
         * refresh() can then call setText() on them whenever the player earns
         * new points or the card is revisited after completing a challenge.
         */
        liveRank        = new JLabel(rank);
        livePoints      = new JLabel(pts + " pts");
        liveTotalSolved = new JLabel(totalSolved + " / " + TOTAL_ALL);
        liveEasy        = new JLabel(easySolved  + " / " + TOTAL_EASY   + "  (Easy)");
        liveMedium      = new JLabel(medSolved   + " / " + TOTAL_MEDIUM + "  (Medium)");
        liveHard        = new JLabel(hardSolved  + " / " + TOTAL_HARD   + "  (Hard)");

        card.add(infoRowWithLabel("Rank",         liveRank,        ACCENT_GOLD));
        card.add(infoRowWithLabel("Total Points", livePoints,      ACCENT_GOLD));
        card.add(infoRowWithLabel("Total Solved", liveTotalSolved, TEXT_PRIMARY));
        card.add(Box.createVerticalStrut(4));
        card.add(infoRowWithLabel("  Easy",   liveEasy,   ACCENT_GREEN));
        card.add(infoRowWithLabel("  Medium", liveMedium, ACCENT_GOLD));
        card.add(infoRowWithLabel("  Hard",   liveHard,   ACCENT_RED));
        card.add(Box.createVerticalStrut(14));

        card.add(centeredLabel("─── COMPLETION ───", PIXEL_SMALL, BORDER_COLOR));
        card.add(Box.createVerticalStrut(8));
        card.add(buildProgressBar(totalSolved, TOTAL_ALL));
        card.add(Box.createVerticalStrut(18));

        int delImgW = rawDeleteImg.getWidth(null), delImgH = rawDeleteImg.getHeight(null);
        int delH = 28;
        int delW = (delImgW > 0 && delImgH > 0) ? (int)(delH * ((double) delImgW / delImgH)) : 100;

        JButton deleteBtn = new JButton(new ImageIcon(rawDeleteImg.getScaledInstance(delW, delH, Image.SCALE_SMOOTH)));
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.setPreferredSize(new Dimension(delW, delH));
        deleteBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                deleteBtn.setBorderPainted(true);
                deleteBtn.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 150), 2));
            }
            public void mouseExited(MouseEvent e) {
                deleteBtn.setBorderPainted(false);
            }
        });

        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete your account? All progress will be lost.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                PlayerSession.getInstance().reset();
                onLogin.run();
            }
        });

        int logImgW = rawLogoutImg.getWidth(null), logImgH = rawLogoutImg.getHeight(null);
        int logH = 28;
        int logW = (logImgW > 0 && logImgH > 0) ? (int)(logH * ((double) logImgW / logImgH)) : 80;

        JButton logoutBtn = new JButton(new ImageIcon(rawLogoutImg.getScaledInstance(logW, logH, Image.SCALE_SMOOTH)));
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(logW, logH));
        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setBorderPainted(true);
                logoutBtn.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 150), 2));
            }
            public void mouseExited(MouseEvent e) {
                logoutBtn.setBorderPainted(false);
            }
        });
        logoutBtn.addActionListener(e -> onLogin.run());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(deleteBtn);
        btnRow.add(logoutBtn);
        card.add(btnRow);

        wrapper.add(card);
        return wrapper;
    }

    // ── Helpers ───────────────────────────────────────────────────
    /**
     * Builds the progress-bar widget and wires it to the live instance fields
     * {@code liveProgressRight} and {@code liveBarBg} so that {@link #refresh()}
     * can update the label text and repaint the fill without rebuilding the panel.
     *
     * @param done   challenges solved at construction time (initial paint)
     * @param total  total challenges available
     */
    private JPanel buildProgressBar(int done, int total) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setMaximumSize(new Dimension(420, 34));
        container.setPreferredSize(new Dimension(420, 34));

        JPanel labelRow = new JPanel(new BorderLayout());
        labelRow.setOpaque(false);
        labelRow.setMaximumSize(new Dimension(420, 14));

        JLabel leftLbl = new JLabel("Progress");
        leftLbl.setFont(PIXEL_SMALL);
        leftLbl.setForeground(TEXT_MUTED);

        // Assign to instance field so refresh() can call setText() on it
        liveProgressRight = new JLabel(done + " / " + total + " challenges");
        liveProgressRight.setFont(PIXEL_SMALL);
        liveProgressRight.setForeground(TEXT_MUTED);

        labelRow.add(leftLbl,          BorderLayout.WEST);
        labelRow.add(liveProgressRight, BorderLayout.EAST);
        container.add(labelRow);
        container.add(Box.createVerticalStrut(4));

        /*
         * The bar reads liveProgress (an instance field) on every repaint.
         * refresh() updates liveProgress then calls liveBarBg.repaint(),
         * so the fill always reflects the current completion ratio.
         */
        liveBarBg = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                int fillW = (int)(getWidth() * liveProgress);
                if (fillW > 0) {
                    Color fill = liveProgress < 0.5  ? ACCENT_GREEN
                               : liveProgress < 0.85 ? ACCENT_GOLD : ACCENT_RED;
                    g2.setColor(fill);
                    g2.fillRoundRect(0, 0, fillW, getHeight(), 6, 6);
                }
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
            }
        };
        liveBarBg.setOpaque(false);
        liveBarBg.setMaximumSize(new Dimension(420, 14));
        liveBarBg.setPreferredSize(new Dimension(420, 14));

        JPanel barWrap = new JPanel(new BorderLayout());
        barWrap.setOpaque(false);
        barWrap.setMaximumSize(new Dimension(420, 14));
        barWrap.add(liveBarBg, BorderLayout.CENTER);
        container.add(barWrap);

        return container;
    }

    /**
     * Builds a two-column info row using a pre-created value {@link JLabel}.
     *
     * <p>This overload is used instead of {@link #infoRow(String, String, Color)}
     * when the caller needs to retain a reference to the value label so it can
     * be updated later by {@link #refresh()} without rebuilding the panel.</p>
     *
     * @param key        Left-hand label text (e.g. "Rank", "Total Points").
     * @param valueLabel Pre-constructed JLabel that will be placed on the right.
     * @param valueColor Foreground colour applied to {@code valueLabel}.
     */
    private JPanel infoRowWithLabel(String key, JLabel valueLabel, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(420, 24));
        row.setPreferredSize(new Dimension(420, 24));

        JLabel keyLbl = new JLabel(key);
        keyLbl.setFont(PIXEL_SMALL);
        keyLbl.setForeground(TEXT_MUTED);
        keyLbl.setPreferredSize(new Dimension(150, 24));

        valueLabel.setFont(PIXEL_FONT);
        valueLabel.setForeground(valueColor);

        row.add(keyLbl,    BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);
        return row;
    }

    /**
     * Legacy string-based info row, kept for any future callers that do not
     * need a live-updatable label reference.
     */
    private JPanel infoRow(String key, String value, Color valueColor) {
        return infoRowWithLabel(key, new JLabel(value), valueColor);
    }

    private JLabel centeredLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(font);
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(420, 22));
        return lbl;
    }
}
