package SourceCode.Builders;

import SourceCode.Builders.Levels.Easy.*;
import SourceCode.Builders.Levels.Hard.*;
import SourceCode.Builders.Levels.Medium.*;
import SourceCode.UserInstances.PlayerSession;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * MainMenuApplication — the central hub, now a JPanel card for Main's
 * CardLayout deck.
 *
 * Layout (BorderLayout):
 * WEST — sidebar with navigation icons (Profile link).
 * CENTER — difficulty selector tabs + scrollable challenge list.
 * EAST — live leaderboard panel driven by PlayerSession.
 *
 * Navigation is handled via Runnable callbacks supplied by Main, keeping
 * this panel fully decoupled from the CardLayout/JFrame plumbing.
 */
public class MainMenuApplication extends JPanel {

    // ── DPI-aware scaling ─────────────────────────────────────────
    private static final double SCALE = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0;

    private static int s(int base) {
        return (int) Math.round(base * SCALE);
    }

    private static Font scaledFont(String name, int style, int base) {
        return new Font(name, style, s(base));
    }

    // ── Colour palette (dark pixel-art theme) ─────────────────────
    private static final Color  BG_DARK = new Color(42, 37, 64),
                                BG_PANEL = new Color(30, 27, 48),
                                BG_SIDEBAR = new Color(26, 23, 40),
                                BG_CARD = new Color(49, 45, 74),
                                ACCENT_GREEN = new Color(63, 185, 80),
                                ACCENT_GOLD = new Color(210, 153, 34),
                                ACCENT_RED = new Color(248, 81, 73),
                                TEXT_PRIMARY = new Color(230, 224, 255),
                                TEXT_MUTED = new Color(138, 133, 170),
                                BORDER_COLOR = new Color(74, 69, 112);
    private static final Font   PIXEL_FONT = scaledFont("Courier New", Font.BOLD, 20),
                                PIXEL_SMALL = scaledFont("Courier New", Font.BOLD, 12);

    // ── Challenge difficulty metadata ─────────────────────────────
    private enum Difficulty {
        EASY("EASY", ACCENT_GREEN, new String[] {
                "1 · Print Hello World",
                "2 · Even or Odd Checker",
                "3 · Largest of Two Numbers",
                "4 · Reverse a String",
                "5 · Sum of Array"
        }),
        MEDIUM("MEDIUM", ACCENT_GOLD, new String[] {
                "1 · Caesar Cipher",
                "2 · Integer Reversal",
                "3 · Pascal Triangle"
        }),
        HARD("HARD", ACCENT_RED, new String[] {
                "1 · Median Sorted Array",
                "2 · String to Integer",
                "3 · Spiral Matrix"
        });

        final String label;
        final Color color;
        final String[] items;

        Difficulty(String label, Color color, String[] items) {
            this.label = label;
            this.color = color;
            this.items = items;
        }
    }

    // ── State ─────────────────────────────────────────────────────
    private Difficulty currentDifficulty = null;
    private DefaultListModel<String> listModel;
    private JList<String> challengeList;
    private JButton[] diffButtons;
    private JLabel difficultyHeader;
    private JPanel leaderboardPanel; // held so it can be refreshed on return

    // ── Navigation callbacks ──────────────────────────────────────
    private final Runnable onProfile, onPlayground; // Main shows "pf" card

    // ── Constructor ───────────────────────────────────────────────
    /**
     * Builds the main menu panel.
     *
     * @param onProfile Runnable called when the "Profile" nav item is clicked
     *                  (Main switches to the "pf" card).
     */
    public MainMenuApplication(Runnable onProfile, Runnable onPlayground) {
        this.onProfile = onProfile;
        this.onPlayground = onPlayground;

        // FIX — replace JFrame-specific setup with JPanel equivalents
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG_DARK);
        body.add(buildSidebar(), BorderLayout.WEST);
        body.add(buildCenter(), BorderLayout.CENTER);
        leaderboardPanel = buildLeaderboard(new java.util.ArrayList<>());
        body.add(leaderboardPanel, BorderLayout.EAST);
        add(body, BorderLayout.CENTER);

        refreshLeaderboard();
    }

    // ── Sidebar ───────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 2, BORDER_COLOR));
        sidebar.setPreferredSize(new Dimension(s(130), 0));

        JLabel logoLabel = new JLabel("LC CLN", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int pad = s(10);
                g2.setColor(BG_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(ACCENT_GREEN);
                g2.setStroke(new BasicStroke(s(3)));
                g2.drawRect(pad, pad, getWidth() - pad * 2, getHeight() - pad * 2);
                g2.drawOval(pad + s(6), pad + s(6), getWidth() - pad * 2 - s(12), getHeight() - pad * 2 - s(12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoLabel.setFont(PIXEL_SMALL);
        logoLabel.setForeground(ACCENT_GREEN);
        logoLabel.setPreferredSize(new Dimension(s(70), s(70)));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel schoolLabel = new JLabel("OLFU", SwingConstants.CENTER);
        schoolLabel.setFont(PIXEL_SMALL);
        schoolLabel.setForeground(ACCENT_GREEN);
        schoolLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(Box.createVerticalStrut(s(14)));
        sidebar.add(logoLabel);
        sidebar.add(Box.createVerticalStrut(s(4)));
        sidebar.add(schoolLabel);
        sidebar.add(Box.createVerticalStrut(s(14)));

        String[] navItems = { "Profile", "Playground", "Settings" };
        for (String item : navItems) {
            JPanel navItem = makeNavItem(item);
            if (item.equals("Profile")) {
                navItem.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // FIX — use callback instead of new ProfileApplication(this) + dispose()
                        onProfile.run();
                    }
                });
            }
            if (item.equals("Playground")) {
                navItem.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        onPlayground.run();
                    }
                });
            }
            sidebar.add(navItem);
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    /**
     * Creates a single sidebar navigation row with hover highlighting.
     */
    private JPanel makeNavItem(String text) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(BG_SIDEBAR);
        item.setMaximumSize(new Dimension(s(130), s(28)));
        item.setPreferredSize(new Dimension(s(130), s(28)));
        item.setBorder(new MatteBorder(0, s(3), 0, 0, BG_SIDEBAR));

        JLabel lbl = new JLabel(text);
        lbl.setFont(PIXEL_SMALL);
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(0, s(14), 0, 0));
        item.add(lbl, BorderLayout.CENTER);

        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(BG_CARD);
                item.setBorder(new MatteBorder(0, s(3), 0, 0, ACCENT_GREEN));
                lbl.setForeground(TEXT_PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(BG_SIDEBAR);
                item.setBorder(new MatteBorder(0, s(3), 0, 0, BG_SIDEBAR));
                lbl.setForeground(TEXT_MUTED);
            }
        });
        return item;
    }

    // ── Centre panel ──────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG_DARK);
        center.add(buildDifficultyRow(), BorderLayout.NORTH);
        center.add(buildChallengeCard(), BorderLayout.CENTER);
        return center;
    }

    // ── Difficulty tab bar ────────────────────────────────────────
    private JPanel buildDifficultyRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 0, 0));
        row.setBackground(BG_PANEL);
        row.setBorder(new MatteBorder(0, 0, s(2), 0, BORDER_COLOR));

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
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                boolean selected = (currentDifficulty == d);
                boolean hover = getModel().isRollover();
                Color bg = (selected || hover) ? d.color : BG_PANEL;
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                setForeground((selected || hover) ? new Color(13, 17, 23) : d.color);
                super.paintComponent(g);
            }
        };
        btn.setFont(PIXEL_FONT);
        btn.setForeground(d.color);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, s(2), BORDER_COLOR),
                new EmptyBorder(s(12), s(4), s(12), s(4))));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> selectDifficulty(d));
        return btn;
    }

    // ── Challenge list card ───────────────────────────────────────
    private JPanel buildChallengeCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_PANEL);

        difficultyHeader = new JLabel("  SELECT A DIFFICULTY ABOVE");
        difficultyHeader.setFont(PIXEL_SMALL);
        difficultyHeader.setForeground(TEXT_MUTED);
        difficultyHeader.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, s(1), 0, BORDER_COLOR),
                new EmptyBorder(s(8), s(14), s(8), s(14))));

        listModel = new DefaultListModel<>();
        challengeList = new JList<>(listModel);
        challengeList.setBackground(BG_PANEL);
        challengeList.setForeground(TEXT_PRIMARY);
        challengeList.setFont(PIXEL_FONT);
        challengeList.setFixedCellHeight(s(40));
        challengeList.setSelectionBackground(new Color(56, 52, 90));
        challengeList.setSelectionForeground(TEXT_PRIMARY);
        challengeList.setBorder(new EmptyBorder(s(4), 0, s(4), 0));
        challengeList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        challengeList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setFont(PIXEL_FONT);
                lbl.setBorder(new CompoundBorder(
                        new MatteBorder(0, s(3), 0, 0,
                                currentDifficulty != null ? currentDifficulty.color : BORDER_COLOR),
                        new EmptyBorder(0, s(14), 0, 0)));
                lbl.setBackground(isSelected ? new Color(56, 52, 90) : BG_PANEL);
                lbl.setForeground(TEXT_PRIMARY);
                lbl.setOpaque(true);
                return lbl;
            }
        });

        challengeList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || currentDifficulty == null)
                return;
            int index = challengeList.getSelectedIndex();
            if (index < 0)
                return;
            openChallenge(currentDifficulty, index);
            SwingUtilities.invokeLater(() -> challengeList.clearSelection());
        });

        JLabel hint = new JLabel("  ▶ Click a challenge to open it");
        hint.setFont(PIXEL_SMALL);
        hint.setForeground(TEXT_MUTED);
        hint.setBorder(new CompoundBorder(
                new MatteBorder(s(1), 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(s(7), s(14), s(7), s(14))));

        JScrollPane scroll = new JScrollPane(challengeList);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_PANEL);
        scroll.getVerticalScrollBar().setBackground(BG_PANEL);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(s(6), 0));

        card.add(difficultyHeader, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    // ── Leaderboard panel ─────────────────────────────────────────
    private JPanel buildLeaderboard(List<PlayerSession.LeaderboardEntry> entries) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SIDEBAR);
        panel.setBorder(new MatteBorder(0, s(2), 0, 0, BORDER_COLOR));
        panel.setPreferredSize(new Dimension(s(170), 0));

        JLabel header = new JLabel("  Leaderboard");
        header.setFont(PIXEL_FONT);
        header.setForeground(TEXT_PRIMARY);
        header.setBackground(BG_SIDEBAR);
        header.setOpaque(true);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, s(2), 0, BORDER_COLOR),
                new EmptyBorder(s(10), s(12), s(10), s(12))));
        panel.add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(BG_SIDEBAR);

        if (entries.isEmpty() || entries == null) {
            JLabel empty = new JLabel("<html><center>Loading scores...<br>or none yet!</center></html>",
                    SwingConstants.CENTER);
            empty.setFont(PIXEL_SMALL);
            empty.setForeground(TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            list.add(Box.createVerticalStrut(s(20)));
            list.add(empty);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                PlayerSession.LeaderboardEntry e = entries.get(i);
                list.add(makeLeaderboardRow(i + 1, e.name, String.valueOf(e.score)));
            }
        }
        list.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_SIDEBAR);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(s(4), 0));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel makeLeaderboardRow(int rank, String name, String score) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG_SIDEBAR);
        row.setMaximumSize(new Dimension(s(170), s(30)));
        row.setPreferredSize(new Dimension(s(170), s(30)));
        row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, s(1), 0, BORDER_COLOR),
                new EmptyBorder(0, s(12), 0, s(12))));

        Color rankColor = (rank <= 3) ? ACCENT_GREEN : ACCENT_GOLD;

        JLabel rankLbl = new JLabel(rank + ".");
        rankLbl.setFont(PIXEL_SMALL);
        rankLbl.setForeground(rankColor);
        rankLbl.setPreferredSize(new Dimension(s(20), s(30)));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(PIXEL_SMALL);
        nameLbl.setForeground(TEXT_PRIMARY);

        JLabel scoreLbl = new JLabel(score + " pts");
        scoreLbl.setFont(PIXEL_SMALL);
        scoreLbl.setForeground(TEXT_MUTED);

        row.add(rankLbl, BorderLayout.WEST);
        row.add(nameLbl, BorderLayout.CENTER);
        row.add(scoreLbl, BorderLayout.EAST);
        return row;
    }

    /**
     * Fetches live data from the cloud in a background thread, then rebuilds
     * the leaderboard panel in-place so the UI doesn't freeze.
     */
    public void refreshLeaderboard() {
        new Thread(() -> {
            try {
                // 1. Fetch from Node.js
                SourceCode.UserInstances.ApiClient api = new SourceCode.UserInstances.ApiClient();
                List<PlayerSession.LeaderboardEntry> liveBoard = api.fetchGlobalLeaderboard();

                // 2. Update the UI back on the Swing Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    // Pass the live data into our updated builder
                    JPanel newLeaderboard = buildLeaderboard(liveBoard);

                    if (leaderboardPanel != null && leaderboardPanel.getParent() != null) {
                        Container parent = leaderboardPanel.getParent();
                        parent.remove(leaderboardPanel);
                        parent.add(newLeaderboard, BorderLayout.EAST);
                        leaderboardPanel = newLeaderboard;

                        parent.revalidate();
                        parent.repaint();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    // ── Difficulty selection ──────────────────────────────────────
    private void selectDifficulty(Difficulty d) {
        currentDifficulty = d;
        difficultyHeader.setText("  " + d.label + " CHALLENGES");
        difficultyHeader.setForeground(d.color);
        listModel.clear();
        for (String item : d.items)
            listModel.addElement(item);
        for (JButton btn : diffButtons)
            btn.repaint();
        challengeList.repaint();
    }

    // ── Challenge launcher ────────────────────────────────────────
    /**
     * Opens the ChallengeConstructor screen for the selected challenge.
     * The Main Menu window is hidden while the challenge is open, and
     * restored automatically when the challenge window closes.
     * NOTE: Challenge screens still open their own JFrames as before —
     * only the hub panels (Login, Register, Menu, Profile) are now JPanel cards.
     */
    private void openChallenge(Difficulty difficulty, int index) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        // Hide the main menu while the challenge is active
        if (parentFrame != null)
            parentFrame.setVisible(false);
        // Restore the main menu when the challenge window closes
        Runnable onReturn = () -> {
            if (parentFrame != null) {
                refreshLeaderboard(); // pull in any newly awarded points
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