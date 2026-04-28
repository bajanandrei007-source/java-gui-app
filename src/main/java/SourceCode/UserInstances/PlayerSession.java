package SourceCode.UserInstances;

import java.util.HashSet;
import java.util.Set;

/**
 * Singleton that tracks the currently logged-in player's session data.
 *
 * Responsibilities:
 * - Stores the logged-in username and JWT Token.
 * - Acts as a local mirror of the database's total score.
 * * NOTE: The Leaderboard logic has been removed. Leaderboards are now fetched 
 * dynamically directly from the database via ApiClient.
 */
public class PlayerSession {

    // ── Singleton instance ────────────────────────────────────────
    private static PlayerSession instance;

    // ── Current player data ─────────────────────────────────────── 
    private String username = "Guest";
    private String email;
    private int points = 0; 

    // ── API Fields ───────────────────────────────────────
    private String jwtToken; 
    private int userId;      

    // Key format: "<DIFFICULTY>-<challengeNumber>", e.g. "EASY-1"
    private final Set<String> completedChallenges = new HashSet<>();

    // ── Leaderboard Data Structure (Kept for ApiClient to use) ────
    public static class LeaderboardEntry {
        public final String name;
        public int score;

        public LeaderboardEntry(String name, int score) {
            this.name  = name;
            this.score = score;
        }
    }

    private PlayerSession() {}

    public static PlayerSession getInstance() {
        if (instance == null) instance = new PlayerSession();
        return instance;
    }

    // ── Register & Login ──────────────────────────────────────────
    public void register(int databaseId, String username, String email, String jwtToken) {
        this.userId = databaseId;
        this.username = username;
        this.email = email;
        this.jwtToken = jwtToken;
        this.points = 0;
    }

    public void login(int databaseId, String email, String jwtToken) {
        this.userId = databaseId;
        this.username = email;
        this.jwtToken = jwtToken;
        this.points = 0;
        this.completedChallenges.clear();
    }

    // ── Getters & Setters ─────────────────────────────────────────
    public String getToken() { return jwtToken; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public int getPoints() { return points; }
    
    public void setUsername(String newUsername) {
        if (newUsername == null || newUsername.isBlank()) return;
        this.username = newUsername.trim();
    }

    // ── API Synchronization Methods ───────────────────────────────
    public void setScore(int officialNewScore) {
        this.points = officialNewScore;
    }

    public void markChallengeCompleted(String difficulty, String number) {
        completedChallenges.add(difficulty + "-" + number);
    }

    // ── Challenge state helpers ───────────────────────────────────
    public boolean isCompleted(String difficulty, String number) {
        return completedChallenges.contains(difficulty + "-" + number);
    }

    public int solvedCount(String difficulty) {
        int count = 0;
        for (String key : completedChallenges)
            if (key.startsWith(difficulty + "-")) count++;
        return count;
    }

    public int totalSolved() { return completedChallenges.size(); }

    public void reset() {
        points = 0;
        completedChallenges.clear();
    }
}