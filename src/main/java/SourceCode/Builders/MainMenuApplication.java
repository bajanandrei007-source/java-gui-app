package SourceCode.Builders;

import SourceCode.Builders.Levels.Easy.*;
import SourceCode.Builders.Levels.Hard.*;
import SourceCode.Builders.Levels.Medium.*;
import SourceCode.UserInstances.PlayerSession;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * MainMenuApplication — the central hub, now a JPanel card for Main's
 * CardLayout deck.
 *
 * Layout (BorderLayout):
 *   WEST   — sidebar with navigation icons (Profile / Playground links).
 *   CENTER — difficulty selector tabs + scrollable challenge list.
 *   EAST   — live leaderboard panel driven by PlayerSession.
 *
 * Scaling strategy (matched to LoginApplication):
 *   • BASE_W = 400, BASE_H = 340  ← same reference dimensions as Login.
 *   • On every resize: liveScale = Math.min(W / BASE_W, H / BASE_H).
 *   • Every font size, cell height, gap and preferred size is derived
 *     from sc(base) = (int)(base * liveScale), with Math.max() floors.
 *   • Sidebar and leaderboard widths are panel-relative percentages so
 *     all three columns grow equally as the window expands.
 *   • A ComponentListener fires layoutComponents() on resize and show,
 *     mirroring the pattern used in LoginApplication exactly.
 */
public class MainMenuApplication extends JPanel {

    // ── Base design dimensions (matches LoginApplication) ─────────────
    private static final int BASE_W = 400, BASE_H = 340;

    // ── Live scale — updated on every resize, read everywhere ─────────
    private double liveScale = 1.0;

    /** Scale a base pixel value by the current live scale factor. */
    private int sc(int base) {
        return Math.max(1, (int) Math.round(base * liveScale));
    }

    // ── Colour palette (dark pixel-art theme) ─────────────────────────
    private static final Color BG_DARK      = new Color( 42,  37,  64),
                               BG_PANEL     = new Color( 30,  27,  48),
                               BG_SIDEBAR   = new Color( 26,  23,  40),
                               BG_CARD      = new Color( 49,  45,  74),
                               ACCENT_GREEN = new Color( 63, 185,  80),
                               ACCENT_GOLD  = new Color(210, 153,  34),
                               ACCENT_RED   = new Color(248,  81,  73),
                               TEXT_PRIMARY = new Color(230, 224, 255),
                               TEXT_MUTED   = new Color(138, 133, 170),
                               BORDER_COLOR = new Color( 74,  69, 112);

    // ── Challenge difficulty metadata ──────────────────────────────────
    private enum Difficulty {
        EASY("EASY", ACCENT_GREEN, new String[]{
                "1 · Print Hello World",
                "2 · Even or Odd Checker",
                "3 · Largest of Two Numbers",
                "4 · Reverse a String",
                "5 · Sum of Array"
        }),
        MEDIUM("MEDIUM", ACCENT_GOLD, new String[]{
                "1 · Caesar Cipher",
                "2 · Integer Reversal",
                "3 · Pascal Triangle"
        }),
        HARD("HARD", ACCENT_RED, new String[]{
                "1 · Median Sorted Array",
                "2 · String to Integer",
                "3 · Spiral Matrix"
        });

        final String   label;
        final Color    color;
        final String[] items;

        Difficulty(String label, Color color, String[] items) {
            this.label = label; this.color = color; this.items = items;
        }
    }

    // ── State ──────────────────────────────────────────────────────────
    private Difficulty            currentDifficulty = null;
    private DefaultListModel<String> listModel;
    private JList<String>         challengeList;
    private JButton[]             diffButtons;
    private JLabel                difficultyHeader;
    private JPanel                leaderboardPanel;

    // ── Scaleable component references ─────────────────────────────────
    private JPanel                sidebar;
    private JLabel                schoolLabel;
    private Image                 rawSchoolImg;
    private final List<JPanel>    navItemPanels = new ArrayList<>();
    private final List<JLabel>    navItemLabels = new ArrayList<>();
    private JLabel                leaderboardHeader;
    private JPanel                leaderboardList;
    private JLabel                challengeHint;

    // ── Navigation callbacks ───────────────────────────────────────────
    private final Runnable onProfile, onPlayground;

    // ─────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────
    public MainMenuApplication(Runnable onProfile, Runnable onPlayground) {
        this.onProfile    = onProfile;
        this.onPlayground = onPlayground;

        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG_DARK);
        body.add(buildSidebar(),   BorderLayout.WEST);
        body.add(buildCenter(),    BorderLayout.CENTER);
        leaderboardPanel = buildLeaderboard(new ArrayList<>());
        body.add(leaderboardPanel, BorderLayout.EAST);
        add(body, BorderLayout.CENTER);

        refreshLeaderboard();

        // ── Live resize listener — mirrors LoginApplication ────────
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { layoutComponents(); }
            @Override public void componentShown  (ComponentEvent e) { layoutComponents(); }
        });
    }

    // ─────────────────────────────────────────────────────────────────
    // Live layout — called on every resize, exactly like Login
    // ─────────────────────────────────────────────────────────────────
    private void layoutComponents() {
        int W = getWidth(), H = getHeight();
        if (W <= 0 || H <= 0) return;

        // Compute uniform scale — same formula as LoginApplication
        liveScale = Math.min((double) W / BASE_W, (double) H / BASE_H);

        // ── Sidebar ───────────────────────────────────────────────
        int sideW = (int)(W * 0.14);          // 14 % of total width
        sidebar.setPreferredSize(new Dimension(sideW, 0));

        // School logo — preserve aspect ratio, scale with window
        if (rawSchoolImg != null) {
            int logoSz = Math.max(24, sc(28)); // base 28 → ~90px at 3.2×
            schoolLabel.setIcon(new ImageIcon(
                    rawSchoolImg.getScaledInstance(logoSz, logoSz, Image.SCALE_SMOOTH)));
        }

        // Nav items — height and font scale together
        Font navFont = new Font("Courier New", Font.BOLD, Math.max(8, sc(6)));
        int  navH    = Math.max(20, sc(14));
        for (int i = 0; i < navItemPanels.size(); i++) {
            JPanel np = navItemPanels.get(i);
            np.setMaximumSize(new Dimension(sideW, navH));
            np.setPreferredSize(new Dimension(sideW, navH));
            JLabel nl = navItemLabels.get(i);
            nl.setFont(navFont);
            nl.setBorder(new EmptyBorder(0, sc(7), 0, 0));
        }

        // ── Leaderboard (EAST) ────────────────────────────────────
        int lbW = (int)(W * 0.18);            // 18 % of total width
        leaderboardPanel.setPreferredSize(new Dimension(lbW, 0));

        if (leaderboardHeader != null) {
            leaderboardHeader.setFont(
                    new Font("Courier New", Font.BOLD, Math.max(10, sc(10))));
            leaderboardHeader.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, sc(1), 0, BORDER_COLOR),
                    new EmptyBorder(sc(5), sc(6), sc(5), sc(6))));
        }

        // Leaderboard rows — iterate and update in-place
        if (leaderboardList != null) {
            Font lbFont  = new Font("Courier New", Font.BOLD, Math.max(7, sc(6)));
            int  lbRowH  = Math.max(18, sc(15));
            for (Component c : leaderboardList.getComponents()) {
                if (!(c instanceof JPanel row)) continue;
                row.setMaximumSize(new Dimension(lbW, lbRowH));
                row.setPreferredSize(new Dimension(lbW, lbRowH));
                row.setBorder(new CompoundBorder(
                        new MatteBorder(0, 0, sc(1), 0, BORDER_COLOR),
                        new EmptyBorder(0, sc(6), 0, sc(6))));
                for (Component child : row.getComponents()) {
                    if (child instanceof JLabel lbl) lbl.setFont(lbFont);
                }
            }
        }

        // ── Difficulty tab buttons ────────────────────────────────
        Font diffFont   = new Font("Courier New", Font.BOLD, Math.max(11, sc(10)));
        int  diffPadV   = Math.max(6,  sc(6));
        int  diffPadH   = Math.max(2,  sc(2));
        for (JButton btn : diffButtons) {
            btn.setFont(diffFont);
            btn.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 0, sc(1), BORDER_COLOR),
                    new EmptyBorder(diffPadV, diffPadH, diffPadV, diffPadH)));
        }

        // ── Difficulty header label ───────────────────────────────
        difficultyHeader.setFont(
                new Font("Courier New", Font.BOLD, Math.max(8, sc(6))));
        difficultyHeader.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, sc(1), 0, BORDER_COLOR),
                new EmptyBorder(sc(4), sc(7), sc(4), sc(7))));

        // ── Challenge list ────────────────────────────────────────
        Font listFont = new Font("Courier New", Font.BOLD, Math.max(11, sc(10)));
        challengeList.setFont(listFont);
        challengeList.setFixedCellHeight(Math.max(22, sc(20)));
        challengeList.setBorder(new EmptyBorder(sc(2), 0, sc(2), 0));

        // ── Challenge hint footer ─────────────────────────────────
        if (challengeHint != null) {
            challengeHint.setFont(
                    new Font("Courier New", Font.BOLD, Math.max(7, sc(6))));
            challengeHint.setBorder(new CompoundBorder(
                    new MatteBorder(sc(1), 0, 0, 0, BORDER_COLOR),
                    new EmptyBorder(sc(4), sc(7), sc(4), sc(7))));
        }

        revalidate();
        repaint();
    }

    // ─────────────────────────────────────────────────────────────────
    // Sidebar
    // ─────────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 2, BORDER_COLOR));
        sidebar.setPreferredSize(new Dimension(130, 0)); // initial; overwritten by layoutComponents

        URL schoolUrl = getClass().getResource("/school_logo.png");
        if (schoolUrl != null) {
            rawSchoolImg = new ImageIcon(schoolUrl).getImage();
            schoolLabel  = new JLabel(new ImageIcon(
                    rawSchoolImg.getScaledInstance(90, 90, Image.SCALE_SMOOTH)));
        } else {
            rawSchoolImg = null;
            schoolLabel  = new JLabel("SCHOOL", SwingConstants.CENTER);
            schoolLabel.setForeground(TEXT_PRIMARY);
            schoolLabel.setFont(new Font("Courier New", Font.BOLD, 12));
        }
        schoolLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(schoolLabel);
        sidebar.add(Box.createVerticalStrut(14));

        for (String item : new String[]{ "Profile", "Playground" }) {
            JPanel navItem = makeNavItem(item);
            if ("Profile".equals(item)) {
                navItem.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) { onProfile.run(); }
                });
            }
            if ("Playground".equals(item)) {
                navItem.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) { onPlayground.run(); }
                });
            }
            sidebar.add(navItem);
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    /**
     * Creates a single sidebar navigation row with hover highlighting.
     * The panel and label are stored in navItemPanels / navItemLabels
     * so layoutComponents() can rescale them on every resize.
     */
    private JPanel makeNavItem(String text) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(BG_SIDEBAR);
        item.setMaximumSize(new Dimension(130, 28));  // overwritten on resize
        item.setPreferredSize(new Dimension(130, 28));
        item.setBorder(new MatteBorder(0, 3, 0, 0, BG_SIDEBAR));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Courier New", Font.BOLD, 12));
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

        // Register for live scaling
        navItemPanels.add(item);
        navItemLabels.add(lbl);
        return item;
    }

    // ─────────────────────────────────────────────────────────────────
    // Centre panel
    // ─────────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG_DARK);
        center.add(buildDifficultyRow(),  BorderLayout.NORTH);
        center.add(buildChallengeCard(), BorderLayout.CENTER);
        return center;
    }

    // ── Difficulty tab bar ────────────────────────────────────────────
    private JPanel buildDifficultyRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 0, 0));
        row.setBackground(BG_PANEL);
        row.setBorder(new MatteBorder(0, 0, 2, 0, BORDER_COLOR));

        diffButtons = new JButton[Difficulty.values().length];
        for (Difficulty d : Difficulty.values()) {
            JButton btn = makeDiffButton(d);
            diffButtons[d.ordinal()] = btn;
            row.add(btn);
        }
        return row;
    }

    private JButton makeDiffButton(Difficulty d) {
        JButton btn = new JButton(d.label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                boolean selected = (currentDifficulty == d);
                boolean hover    = getModel().isRollover();
                g2.setColor((selected || hover) ? d.color : BG_PANEL);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                setForeground((selected || hover) ? new Color(13, 17, 23) : d.color);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Courier New", Font.BOLD, 20)); // overwritten on resize
        btn.setForeground(d.color);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 2, BORDER_COLOR),
                new EmptyBorder(12, 4, 12, 4)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> selectDifficulty(d));
        return btn;
    }

    // ── Challenge list card ───────────────────────────────────────────
    private JPanel buildChallengeCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_PANEL);

        difficultyHeader = new JLabel("  SELECT A DIFFICULTY ABOVE");
        difficultyHeader.setFont(new Font("Courier New", Font.BOLD, 12));
        difficultyHeader.setForeground(TEXT_MUTED);
        difficultyHeader.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(8, 14, 8, 14)));

        listModel     = new DefaultListModel<>();
        challengeList = new JList<>(listModel);
        challengeList.setBackground(BG_PANEL);
        challengeList.setForeground(TEXT_PRIMARY);
        challengeList.setFont(new Font("Courier New", Font.BOLD, 20)); // overwritten on resize
        challengeList.setFixedCellHeight(40);                           // overwritten on resize
        challengeList.setSelectionBackground(new Color(56, 52, 90));
        challengeList.setSelectionForeground(TEXT_PRIMARY);
        challengeList.setBorder(new EmptyBorder(4, 0, 4, 0));
        challengeList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Cell renderer tracks the list's live font so it grows with the window
        challengeList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setFont(challengeList.getFont()); // always mirrors the live font
                lbl.setBorder(new CompoundBorder(
                        new MatteBorder(0, sc(3), 0, 0,
                                currentDifficulty != null ? currentDifficulty.color : BORDER_COLOR),
                        new EmptyBorder(0, sc(14), 0, 0)));
                lbl.setBackground(isSelected ? new Color(56, 52, 90) : BG_PANEL);
                lbl.setForeground(TEXT_PRIMARY);
                lbl.setOpaque(true);
                return lbl;
            }
        });

        challengeList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || currentDifficulty == null) return;
            int index = challengeList.getSelectedIndex();
            if (index < 0) return;
            openChallenge(currentDifficulty, index);
            SwingUtilities.invokeLater(() -> challengeList.clearSelection());
        });

        challengeHint = new JLabel("  ▶ Click a challenge to open it");
        challengeHint.setFont(new Font("Courier New", Font.BOLD, 12));
        challengeHint.setForeground(TEXT_MUTED);
        challengeHint.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(7, 14, 7, 14)));

        JScrollPane scroll = new JScrollPane(challengeList);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_PANEL);
        scroll.getVerticalScrollBar().setBackground(BG_PANEL);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));

        card.add(difficultyHeader, BorderLayout.NORTH);
        card.add(scroll,           BorderLayout.CENTER);
        card.add(challengeHint,    BorderLayout.SOUTH);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────
    // Leaderboard panel
    // ─────────────────────────────────────────────────────────────────
    private JPanel buildLeaderboard(List<PlayerSession.LeaderboardEntry> entries) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SIDEBAR);
        panel.setBorder(new MatteBorder(0, 2, 0, 0, BORDER_COLOR));
        panel.setPreferredSize(new Dimension(170, 0)); // initial; overwritten on resize

        leaderboardHeader = new JLabel("  Leaderboard");
        leaderboardHeader.setFont(new Font("Courier New", Font.BOLD, 20));
        leaderboardHeader.setForeground(TEXT_PRIMARY);
        leaderboardHeader.setBackground(BG_SIDEBAR);
        leaderboardHeader.setOpaque(true);
        leaderboardHeader.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, BORDER_COLOR),
                new EmptyBorder(10, 12, 10, 12)));
        panel.add(leaderboardHeader, BorderLayout.NORTH);

        leaderboardList = new JPanel();
        leaderboardList.setLayout(new BoxLayout(leaderboardList, BoxLayout.Y_AXIS));
        leaderboardList.setBackground(BG_SIDEBAR);

        if (entries == null || entries.isEmpty()) {
            JLabel empty = new JLabel(
                    "<html><center>Loading scores...<br>or none yet!</center></html>",
                    SwingConstants.CENTER);
            empty.setFont(new Font("Courier New", Font.BOLD, 12));
            empty.setForeground(TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            leaderboardList.add(Box.createVerticalStrut(20));
            leaderboardList.add(empty);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                PlayerSession.LeaderboardEntry e = entries.get(i);
                leaderboardList.add(makeLeaderboardRow(i + 1, e.name, String.valueOf(e.score)));
            }
        }
        leaderboardList.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(leaderboardList);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_SIDEBAR);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel makeLeaderboardRow(int rank, String name, String score) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG_SIDEBAR);
        row.setMaximumSize(new Dimension(170, 30));   // overwritten by layoutComponents
        row.setPreferredSize(new Dimension(170, 30));
        row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(0, 12, 0, 12)));

        Color rankColor = (rank <= 3) ? ACCENT_GREEN : ACCENT_GOLD;

        JLabel rankLbl  = new JLabel(rank + ".");
        rankLbl.setFont(new Font("Courier New", Font.BOLD, 12));
        rankLbl.setForeground(rankColor);
        rankLbl.setPreferredSize(new Dimension(20, 30));

        JLabel nameLbl  = new JLabel(name);
        nameLbl.setFont(new Font("Courier New", Font.BOLD, 12));
        nameLbl.setForeground(TEXT_PRIMARY);

        JLabel scoreLbl = new JLabel(score + " pts");
        scoreLbl.setFont(new Font("Courier New", Font.BOLD, 12));
        scoreLbl.setForeground(TEXT_MUTED);

        row.add(rankLbl,  BorderLayout.WEST);
        row.add(nameLbl,  BorderLayout.CENTER);
        row.add(scoreLbl, BorderLayout.EAST);
        return row;
    }

    /**
     * Fetches live data from the cloud in a background thread, then rebuilds
     * the leaderboard panel in-place so the UI doesn't freeze.
     * After rebuilding, layoutComponents() is called so the new rows
     * immediately inherit the current window scale.
     */
    public void refreshLeaderboard() {
        new Thread(() -> {
            try {
                SourceCode.UserInstances.ApiClient api = new SourceCode.UserInstances.ApiClient();
                List<PlayerSession.LeaderboardEntry> liveBoard = api.fetchGlobalLeaderboard();

                SwingUtilities.invokeLater(() -> {
                    JPanel newLB = buildLeaderboard(liveBoard);
                    if (leaderboardPanel != null && leaderboardPanel.getParent() != null) {
                        Container parent = leaderboardPanel.getParent();
                        parent.remove(leaderboardPanel);
                        parent.add(newLB, BorderLayout.EAST);
                        leaderboardPanel = newLB;
                        parent.revalidate();
                        parent.repaint();
                    }
                    // Apply the current window scale to the freshly built rows
                    layoutComponents();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────
    // Difficulty selection
    // ─────────────────────────────────────────────────────────────────
    private void selectDifficulty(Difficulty d) {
        currentDifficulty = d;
        difficultyHeader.setText("  " + d.label + " CHALLENGES");
        difficultyHeader.setForeground(d.color);
        listModel.clear();
        for (String item : d.items) listModel.addElement(item);
        for (JButton btn : diffButtons) btn.repaint();
        challengeList.repaint();
    }

    // ─────────────────────────────────────────────────────────────────
    // Challenge launcher
    // ─────────────────────────────────────────────────────────────────
    /**
     * Opens the ChallengeConstructor screen for the selected challenge.
     * The Main Menu window is hidden while the challenge is open, and
     * restored automatically when the challenge window closes.
     */
    private void openChallenge(Difficulty difficulty, int index) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame != null) parentFrame.setVisible(false);

        Runnable onReturn = () -> {
            if (parentFrame != null) {
                refreshLeaderboard();
                parentFrame.setVisible(true);
            }
        };

        switch (difficulty) {
            case EASY -> {
                switch (index) {
                    case 0 -> new HelloWorldApp(parentFrame, onReturn);
                    case 1 -> new EvenOddApp(parentFrame, onReturn);
                    case 2 -> new LargestNumberApp(parentFrame, onReturn);
                    case 3 -> new ReverseStringApp(parentFrame, onReturn);
                    case 4 -> new SumArrayApp(parentFrame, onReturn);
                }
            }
            case MEDIUM -> {
                switch (index) {
                    case 0 -> new CaesarCipherApp(parentFrame, onReturn);
                    case 1 -> new IntegerReversalApp(parentFrame, onReturn);
                    case 2 -> new PascalTriangleApp(parentFrame, onReturn);
                }
            }
            case HARD -> {
                switch (index) {
                    case 0 -> new MedianSortedArrayApp(parentFrame, onReturn);
                    case 1 -> new StringToIntegerApp(parentFrame, onReturn);
                    case 2 -> new SpiralMatrixApp(parentFrame, onReturn);
                }
            }
        }
    }
}