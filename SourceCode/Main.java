package SourceCode;

import SourceCode.Builders.*;
import SourceCode.UserInstances.ApiClient;
import SourceCode.UserInstances.PlayerSession;
import java.awt.*;
import javax.swing.*;

// andrei@fatima.edu.ph

public class Main extends JFrame {

    // ── DPI-aware scaling ─────────────────────────────────────────
    private static final double SCALE = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0;

    private static int s(int base) {
        return (int) Math.round(base * SCALE);
    }

    CardLayout cl = new CardLayout();
    private final JPanel deck;

    public Main() {
        setTitle("Main");
        setSize(s(500), s(500));
        setMinimumSize(new Dimension(s(400), s(400)));
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        deck = new JPanel(cl);

        // ── Build each card with its navigation callbacks ──────────
        LoginApplication loginPanel = new LoginApplication(
                () -> cl.show(deck, "mm"), // onLogin → main menu
                () -> cl.show(deck, "reg") // onRegister → registration
        );

        RegistrationApplication regPanel = new RegistrationApplication(
                () -> cl.show(deck, "mm"), // onSuccess → main menu
                () -> cl.show(deck, "log") // onBack → login
        );

        // We use an array reference to break the circular dependency between Profile and MainMenu
        MainMenuApplication[] mmPanelRef = new MainMenuApplication[1];

        ProfileApplication pfPanel = new ProfileApplication(
                () -> {
                    cl.show(deck, "mm");
                    if (mmPanelRef[0] != null) mmPanelRef[0].refreshLeaderboard();
                }, // onBack → main menu AND refresh leaderboard
                () -> cl.show(deck, "log") // onLogin → login (logout/delete)
        );

        CodeEditorApplication editorPanel = new CodeEditorApplication(
                () -> cl.show(deck, "mm") // Back → main menu
        );

        MainMenuApplication mmPanel = new MainMenuApplication(
                () -> {
                    new Thread(() -> {
                        try {
                            ApiClient api = new ApiClient();
                            PlayerSession session = PlayerSession.getInstance();

                            // ── DEBUG: print session state before anything ──
                            System.out.println("=== PROFILE OPEN ===");
                            System.out.println("userId   : " + session.getUserId());
                            System.out.println("username : " + session.getUsername());
                            System.out.println("points   : " + session.getPoints());

                            // 1. Clear stale state, reload from DB
                            session.reset();
                            java.util.List<String> progress = api.fetchUserProgress(session.getUserId());

                            // ── DEBUG: print raw API response ──
                            System.out.println("fetchUserProgress returned " + progress.size() + " entries:");
                            for (String id : progress)
                                System.out.println("  -> " + id);

                            for (String challengeId : progress) {
                                String[] parts = challengeId.split("-", 2);
                                if (parts.length == 2) {
                                    session.markChallengeCompleted(parts[0], parts[1]);
                                    System.out.println("marked: " + parts[0] + " / " + parts[1]);
                                }
                            }

                            // ── DEBUG: print session state after marking ──
                            System.out.println("totalSolved after marking: " + session.totalSolved());
                            System.out.println("EASY solved: " + session.solvedCount("EASY"));

                            // 2. Fetch leaderboard to resolve rank
                            java.util.List<PlayerSession.LeaderboardEntry> board = api.fetchGlobalLeaderboard();
                            String currentUser = session.getUsername();

                            // ── DEBUG: print leaderboard entries ──
                            System.out.println(
                                    "Leaderboard (" + board.size() + " entries), looking for: '" + currentUser + "'");
                            for (int i = 0; i < board.size(); i++) {
                                System.out.println(
                                        "  #" + (i + 1) + " '" + board.get(i).name + "' = " + board.get(i).score);
                            }

                            String rank = "Unranked";
                            for (int i = 0; i < board.size(); i++) {
                                if (board.get(i).name.equals(currentUser)) {
                                    rank = "#" + (i + 1);
                                    break;
                                }
                            }
                            System.out.println("Resolved rank: " + rank);
                            final String finalRank = rank;

                            // 3. Push to UI on EDT
                            SwingUtilities.invokeLater(() -> {
                                pfPanel.refresh();
                                pfPanel.setRank(finalRank);
                                cl.show(deck, "pf");
                            });

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            SwingUtilities.invokeLater(() -> {
                                pfPanel.refresh();
                                cl.show(deck, "pf");
                            });
                        }
                    }).start();
                },
                () -> cl.show(deck, "editor"));

        // Assign the reference so pfPanel can access it later
        mmPanelRef[0] = mmPanel;

        deck.add(editorPanel, "editor");
        deck.add(loginPanel, "log");
        deck.add(regPanel, "reg");
        deck.add(mmPanel, "mm");
        deck.add(pfPanel, "pf");

        add(deck, BorderLayout.CENTER);
        
        setLocationRelativeTo(null);
        cl.show(deck, "log");
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}