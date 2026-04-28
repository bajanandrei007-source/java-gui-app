package SourceCode.UserInstances;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

private static final String BASE_URL = "https://leetcode-prototype-api-production.up.railway.app";

public class ApiClient {

    public static class AuthResult {
        public String token;
        public int userId;
        public String username;

        public AuthResult(String token, int userId, String username) {
            this.token = token;
            this.userId = userId;
            this.username = username;
        }
    }

    public AuthResult registerUser(String username, String email, String passwordHash) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        // 1. Format the JSON Body
        // Note: We use the hashed password here because your Node.js backend expects it!
        String json = String.format(
            "{\"username\":\"%s\", \"email\":\"%s\", \"password\":\"%s\"}", 
            username, email, passwordHash
        );

        // 2. Build the POST Request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        // 3. Send the Request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();

        // 4. Handle the "Graceful" Backend Responses
        if (statusCode == 201 || statusCode == 200) { 
            // Parse the JSON Object
            JSONObject jsonObject = new JSONObject(response.body());
            
            // Extract the Token (String)
            String realToken = jsonObject.getString("token");
            
            // Extract the Database ID (Integer) - THIS IS THE NEW LINE!
            int realDbId = jsonObject.getInt("userId"); 
            
            // Return them both inside our custom Box
            return new AuthResult(realToken, realDbId, username);
            
        } else if (statusCode == 409) {
            // Caught the duplicate email/username error
            throw new Exception("Registration failed: That email or username is already taken.");
            
        } else if (statusCode == 400) {
            // Caught the missing fields error
            throw new Exception("Registration failed: Please fill out all fields.");
            
        } else {
            // Catch-all for 500 Server Errors or connection drops
            throw new Exception("Server Error (" + statusCode + "): " + response.body());
        }
    }

    public AuthResult loginUser(String email, String rawPassword) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // =========================================================
        // PART 1: Ask the API for the saved hash
        // =========================================================
        String emailJson = String.format("{\"email\":\"%s\"}", email);
        
        HttpRequest hashRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/get-hash"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(emailJson))
                .build();

        HttpResponse<String> hashResponse = client.send(hashRequest, HttpResponse.BodyHandlers.ofString());
        
        if (hashResponse.statusCode() != 200) {
            throw new Exception("Login failed: Invalid email.");
        }

        // Extract the saved hash and ID from the database
        JSONObject hashData = new JSONObject(hashResponse.body());
        String savedHash = hashData.getString("savedHash");
        int dbId = hashData.getInt("userId");

        // =========================================================
        // PART 2: THE STAR OF THE SHOW - Your Java Argon2 Method!
        // =========================================================
        boolean isMatch = PasswordUtil.verifyPassword(rawPassword.toCharArray(), savedHash);

        if (!isMatch) {
            throw new Exception("Login failed: Incorrect password.");
        }

        // =========================================================
        // PART 3: If it matches, claim the JWT Token from the API
        // =========================================================
        String claimJson = String.format("{\"userId\":%d}", dbId);
        
        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/claim-token"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(claimJson))
                .build();

        HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

        if (tokenResponse.statusCode() == 200) {
            JSONObject tokenData = new JSONObject(tokenResponse.body());
            String finalToken = tokenData.getString("token");
            String realUsername = hashData.optString("username", email);

            System.out.println("Java Argon2 Verification Successful!");
            return new AuthResult(finalToken, dbId, realUsername);
        } else {
            throw new Exception("Server refused to issue token.");
        }
    }

    public int submitChallengeResult(String challengeId, boolean passed, int pointsAwarded) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        
        PlayerSession session = PlayerSession.getInstance();
        String token = session.getToken();
        int userId = session.getUserId();

        if (token == null || token.isEmpty()) {
            throw new Exception("Authentication token missing. Please log in again.");
        }

        // Build the JSON payload matching your Node.js req.body
        String jsonBody = String.format(
            "{\"user_id\": %d, \"challenge_id\": \"%s\", \"passed\": %b, \"points\": %d}",
            userId, challengeId, passed, pointsAwarded
        );

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/scores"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            org.json.JSONObject jsonObject = new org.json.JSONObject(response.body());
            // Grab the 'newScore' we attached in the Node.js backend
            return jsonObject.getInt("newScore"); 
        } else {
            org.json.JSONObject errorObj = new org.json.JSONObject(response.body());
            throw new Exception(errorObj.getString("error"));
        }
    }

    public java.util.List<PlayerSession.LeaderboardEntry> fetchGlobalLeaderboard() throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                // Make sure this matches exactly where you put the Node.js route!
                .uri(java.net.URI.create(BASE_URL + "/api/leaderboard"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            java.util.List<PlayerSession.LeaderboardEntry> globalBoard = new java.util.ArrayList<>();
            org.json.JSONArray jsonArray = new org.json.JSONArray(response.body());
            
            // Loop through the JSON array and build LeaderboardEntry objects
            for (int i = 0; i < jsonArray.length(); i++) {
                org.json.JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.getString("username");
                
                // Use optInt in case a user has a NULL total_score in the database
                int score = obj.optInt("total_score", 0); 
                
                if (score > 0) {
                    globalBoard.add(new PlayerSession.LeaderboardEntry(name, score));
                }
            }
            return globalBoard;
        } else {
            throw new Exception("Failed to load leaderboard from cloud.");
        }
    }

    public java.util.List<String> fetchUserProgress(int userId) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        String token = PlayerSession.getInstance().getToken();

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/scores/user/" + userId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        System.out.println("fetchUserProgress status : " + response.statusCode());
        System.out.println("fetchUserProgress body   : " + response.body());

        if (response.statusCode() == 200) {
            org.json.JSONObject body = new org.json.JSONObject(response.body());

            // Sync the official total_score from DB into PlayerSession
            int officialScore = body.getInt("totalScore");
            PlayerSession.getInstance().setScore(officialScore);

            // Load completed challenges
            java.util.List<String> completedIds = new java.util.ArrayList<>();
            org.json.JSONArray jsonArray = body.getJSONArray("scores");
            for (int i = 0; i < jsonArray.length(); i++) {
                org.json.JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getBoolean("passed")) {
                    completedIds.add(obj.getString("challenge_id"));
                }
            }
            return completedIds;
        } else {
            throw new Exception("Failed to load user progress. Status: "
                + response.statusCode() + " Body: " + response.body());
        }
    }

    // ── Username management ──────────────────────────────────────

    /**
     * Checks whether a username is already taken in the database.
     * Uses a public (no-auth) endpoint so the check can run instantly.
     *
     * @param username  The username to check.
     * @return {@code true} if the name is already in use by another account.
     */
    public boolean checkUsernameTaken(String username) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        // URL-encode the username to handle spaces / special chars
        String encoded = java.net.URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8);

        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(
                    BASE_URL + "/api/users/check-username/" + encoded))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        java.net.http.HttpResponse<String> response =
                client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            org.json.JSONObject body = new org.json.JSONObject(response.body());
            boolean taken = body.getBoolean("taken");

            if (taken) {
                // If the owning user is the current user, it's NOT really "taken"
                int ownerId = body.optInt("ownerId", -1);
                int currentUserId = PlayerSession.getInstance().getUserId();
                return ownerId != currentUserId;   // true = taken by someone else
            }
            return false;
        } else {
            throw new Exception("Server error checking username availability.");
        }
    }

    /**
     * Persists a new username to the database via {@code PUT /api/users/:id}.
     * The backend enforces uniqueness and returns 400 if the name is taken.
     *
     * @param newUsername The desired new username.
     * @throws Exception on network errors, auth failures, or duplicate names.
     */
    public void updateUsername(String newUsername) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        PlayerSession session = PlayerSession.getInstance();
        String token  = session.getToken();
        int    userId = session.getUserId();

        if (token == null || token.isEmpty()) {
            throw new Exception("Authentication token missing. Please log in again.");
        }

        // The PUT /api/users/:id route expects { username, email }.
        // We keep email unchanged — fetch the current one from the profile first.
        // For simplicity we pass just the username and let the backend keep email as-is
        // via a COALESCE approach.  However the existing backend expects both fields,
        // so we fetch the current email from the profile endpoint first.

        // -- Step 1: GET current profile to retrieve the email --
        java.net.http.HttpRequest getReq = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(
                    BASE_URL + "/api/users/" + userId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        java.net.http.HttpResponse<String> getResp =
                client.send(getReq, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (getResp.statusCode() != 200) {
            throw new Exception("Failed to fetch current profile before update.");
        }

        org.json.JSONObject profile = new org.json.JSONObject(getResp.body());
        String currentEmail = profile.getString("email");

        // -- Step 2: PUT the update --
        String jsonBody = String.format(
            "{\"username\":\"%s\", \"email\":\"%s\"}",
            newUsername.replace("\"", "\\\""),
            currentEmail.replace("\"", "\\\"")
        );

        java.net.http.HttpRequest putReq = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(
                    BASE_URL + "/api/users/" + userId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        java.net.http.HttpResponse<String> putResp =
                client.send(putReq, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (putResp.statusCode() == 200) {
            // Success — update the local session
            session.setUsername(newUsername);
            System.out.println("Username updated in database to: " + newUsername);
        } else if (putResp.statusCode() == 400) {
            org.json.JSONObject errBody = new org.json.JSONObject(putResp.body());
            throw new Exception(errBody.optString("error", "Username or email already in use."));
        } else if (putResp.statusCode() == 403) {
            throw new Exception("Access denied — you can only update your own profile.");
        } else {
            throw new Exception("Failed to update username. Status: " + putResp.statusCode());
        }
    }

    // ── Email OTP Methods ────────────────────────────────────────

    public void sendOtp(String email) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        String json = String.format("{\"email\":\"%s\"}", email);
        
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/auth/send-otp"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 409) {
            throw new Exception("Email already exists");
        } else if (response.statusCode() != 200) {
            throw new Exception("Failed to send OTP. Server returned: " + response.statusCode());
        }
    }

    public boolean verifyOtp(String email, String otp) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        String json = String.format("{\"email\":\"%s\", \"otp\":\"%s\"}", email, otp);
        
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/auth/verify-otp"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return true;
        } else if (response.statusCode() == 400) {
            org.json.JSONObject errorObj = new org.json.JSONObject(response.body());
            throw new Exception(errorObj.optString("error", "Invalid or expired OTP"));
        } else {
            throw new Exception("Server error verifying OTP");
        }
    }

    public void sendForgotPasswordOtp(String email) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        String json = String.format("{\"email\":\"%s\"}", email);
        
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/auth/forgot-password"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 404) {
            throw new Exception("Email not found in our records");
        } else if (response.statusCode() != 200) {
            throw new Exception("Failed to send reset email");
        }
    }

    public void resetPassword(String email, String otp, String newPasswordHash) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        String json = String.format("{\"email\":\"%s\", \"otp\":\"%s\", \"newPasswordHash\":\"%s\"}", 
            email, otp, newPasswordHash);
        
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/api/auth/reset-password"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 400) {
            org.json.JSONObject errorObj = new org.json.JSONObject(response.body());
            throw new Exception(errorObj.optString("error", "Invalid or expired OTP"));
        } else if (response.statusCode() != 200) {
            throw new Exception("Server error resetting password");
        }
    }
}