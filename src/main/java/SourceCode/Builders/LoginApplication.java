package SourceCode.Builders;

import SourceCode.UserInstances.ApiClient;
import SourceCode.UserInstances.PlayerSession;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * LoginApp — a JPanel card used inside Main's CardLayout deck.
 *
 * Responsibilities:
 *  - Collects a username and password from the user.
 *  - On "Sign in", stores the username into PlayerSession so the
 *    rest of the app knows who is playing, then navigates to the main menu.
 *  - Provides a link to RegistrationApp for new users.
 *  - Supports showing/hiding the typed password via an eye-icon toggle.
 *  - Scales all fonts and component positions responsively when resized.
 *
 * NOTE: No real authentication is performed — any non-empty username is accepted.
 */
public class LoginApplication extends JPanel {

    // ── Swing components ──────────────────────────────────────────
    private final JPanel         panel;
    private final JButton        goToMainMenuBtn, toggleButton;
    private final JLabel         logoLabel, titleLabel, l1, l2, registerPromptLabel, forgotPasswordLabel;
    private final JTextField     usernameTF;
    private final JPasswordField passwordPF;

    // ── Eye-icon images (loaded from disk; falls back to text emoji) ──
    private Image     rawShowImg, rawHideImg, rawLoginImg;
    private ImageIcon showIcon,   hideIcon;
    private boolean   isShowing = false;   // tracks whether password is visible

    // ── Colour palette (purple-slate theme) ───────────────────────
    private static final Color BG_COLOR     = new Color(74,  65, 107),
                                FIELD_BG    = new Color(200, 198, 220),
                                LABEL_COLOR = new Color(230, 225, 255),
                                TEXT_COLOR  = new Color(245, 242, 255),
                                FIELD_FG    = new Color( 30,  25,  55);

    // ── Base design dimensions the proportional layout was built on ──
    private static final int BASE_W = 400, BASE_H = 340;

    /** Unscaled width reserved for the eye-toggle button. */
    private static final int TOGGLE_W = 26;

    // ── Constructor ───────────────────────────────────────────────
    /**
     * Builds the login panel.
     *
     * FIX #3 — instead of referencing Main's CardLayout directly, accept two
     * Runnable callbacks so LoginApplication stays decoupled from Main:
     *   onLogin    → called after a successful sign-in  (Main shows "mm" card)
     *   onRegister → called when the register link is clicked (Main shows "reg" card)
     */
    public LoginApplication(Runnable onLogin, Runnable onRegister) {

        // FIX #5 — fill the parent JPanel so the inner null-layout panel is visible
        setLayout(new BorderLayout());

        // ── Background panel (null layout — we position everything manually) ──
        panel = new JPanel(null);
        panel.setBackground(BG_COLOR);

        // ── Logo / title labels ───────────────────────────────────
        logoLabel = new JLabel("LC CLN", SwingConstants.CENTER);
        logoLabel.setForeground(TEXT_COLOR);
        panel.add(logoLabel);

        titleLabel = new JLabel("Sign in to play", SwingConstants.CENTER);
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel);

        // ── Username field ────────────────────────────────────────
        l1 = new JLabel("Email:");
        l1.setForeground(LABEL_COLOR);
        panel.add(l1);

        usernameTF = new JTextField();
        panel.add(usernameTF);

        // ── Password field ────────────────────────────────────────
        l2 = new JLabel("Password:");
        l2.setForeground(LABEL_COLOR);
        panel.add(l2);

        passwordPF = new JPasswordField();
        panel.add(passwordPF);

        // ── Eye-icon toggle: tries to load image files, falls back to emoji ──
        URL showUrl = getClass().getResource("/ShowP.png"),
            hideUrl = getClass().getResource("/HideP.png");

        if (showUrl != null && hideUrl != null) {
            rawShowImg = new ImageIcon(showUrl).getImage();
            rawHideImg = new ImageIcon(hideUrl).getImage();
            showIcon   = new ImageIcon(rawShowImg.getScaledInstance(18, 18, Image.SCALE_SMOOTH));
            hideIcon   = new ImageIcon(rawHideImg.getScaledInstance(18, 18, Image.SCALE_SMOOTH));
            toggleButton = new JButton(showIcon);
        } else {
            toggleButton = new JButton("👁");
            toggleButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        }
        toggleButton.setFocusPainted(false);
        toggleButton.setBackground(BG_COLOR);
        toggleButton.setBorder(BorderFactory.createEmptyBorder());
        toggleButton.setContentAreaFilled(false);
        toggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleButton.addActionListener(e -> togglePassword());
        panel.add(toggleButton);

        // ── "Forgot Password?" link ───────────────────────────────
        forgotPasswordLabel = new JLabel("Forgot Password?");
        forgotPasswordLabel.setForeground(new Color(176, 196, 255));
        forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                handleForgotPassword();
            }
            @Override public void mouseEntered(MouseEvent e) {
                forgotPasswordLabel.setText("<html><u>Forgot Password?</u></html>");
            }
            @Override public void mouseExited(MouseEvent e) {
                forgotPasswordLabel.setText("Forgot Password?");
            }
        });
        panel.add(forgotPasswordLabel);

        // ── "Don't have an account? Register" link ────────────────
        // FIX #3 — pass the onRegister callback instead of directly calling cl/deck
        registerPromptLabel = buildRegisterLink(onRegister);
        panel.add(registerPromptLabel);

        // ── Sign-in button (image-based) ────────────────────────
        rawLoginImg = new ImageIcon(getClass().getResource("/Buttons/LoginButton.png")).getImage();
        goToMainMenuBtn = new JButton();
        goToMainMenuBtn.setContentAreaFilled(false);
        goToMainMenuBtn.setBorderPainted(false);
        goToMainMenuBtn.setFocusPainted(false);
        goToMainMenuBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Hover effect — slight border glow
        goToMainMenuBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                goToMainMenuBtn.setBorderPainted(true);
                goToMainMenuBtn.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 150), 2));
            }
            public void mouseExited(MouseEvent e) {
                goToMainMenuBtn.setBorderPainted(false);
            }
        });
        // FIX #3 — pass the onLogin callback instead of directly calling cl/deck
        goToMainMenuBtn.addActionListener(e -> handleLogin(onLogin));
        panel.add(goToMainMenuBtn);

        // FIX #2 — defer setDefaultButton until after the panel is shown,
        // so getRootPane() is non-null. Use a HierarchyListener for this.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                JRootPane rp = SwingUtilities.getRootPane(this);
                if (rp != null) rp.setDefaultButton(goToMainMenuBtn);
            }
        });

        // FIX #5 — add the inner panel to fill this JPanel
        add(panel, BorderLayout.CENTER);

        // Re-layout all components whenever the panel is resized or first shown
        addComponentListener(new ComponentAdapter() {
            // FIX #1 — call the no-arg wrapper, which reads the current size
            @Override public void componentResized(ComponentEvent e) { layoutComponents(); }
            @Override public void componentShown (ComponentEvent e) { layoutComponents(); }
        });
    }

    // ── Layout trampoline (no-arg) ────────────────────────────────
    /**
     * FIX #1 — the ComponentListener needs a no-arg call; this reads the
     * current panel size and delegates to the real layout method.
     */
    private void layoutComponents() {
        layoutComponents(getWidth(), getHeight());
    }

    // ── Forgot Password handler ───────────────────────────────────
    private void handleForgotPassword() {
        String email = JOptionPane.showInputDialog(this, 
            "Enter your email address:", "Forgot Password", JOptionPane.QUESTION_MESSAGE);
            
        if (email == null || email.trim().isEmpty()) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            try {
                ApiClient api = new ApiClient();
                // 1. Send OTP
                api.sendForgotPasswordOtp(email);

                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    String otp = JOptionPane.showInputDialog(this, 
                        "An OTP has been sent to " + email + ".\nEnter the 6-digit code:", 
                        "Verify Email", JOptionPane.INFORMATION_MESSAGE);

                    if (otp == null || otp.trim().isEmpty()) return;
                    
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    
                    new Thread(() -> {
                        try {
                            // 2. Verify OTP
                            if (api.verifyOtp(email, otp.trim())) {
                                SwingUtilities.invokeLater(() -> {
                                    setCursor(Cursor.getDefaultCursor());
                                    promptForNewPassword(email, otp.trim(), api);
                                });
                            }
                        } catch (Exception ex) {
                            showError("Verification Failed", ex);
                        }
                    }).start();
                });

            } catch (Exception ex) {
                showError("Forgot Password Failed", ex);
            }
        }).start();
    }

    private void promptForNewPassword(String email, String otp, ApiClient api) {
        JPasswordField pf1 = new JPasswordField();
        JPasswordField pf2 = new JPasswordField();
        Object[] message = {
            "New Password:", pf1,
            "Confirm New Password:", pf2
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Reset Password", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String pass1 = new String(pf1.getPassword());
            String pass2 = new String(pf2.getPassword());

            if (pass1.isEmpty() || pass1.length() < 8 || !pass1.matches(".*[^a-zA-Z0-9].*")) {
                JOptionPane.showMessageDialog(this, "Password must be at least 8 characters and contain 1 special character.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!pass1.equals(pass2)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            String hashed = SourceCode.UserInstances.PasswordUtil.hashPassword(pf1.getPassword());

            new Thread(() -> {
                try {
                    api.resetPassword(email, otp, hashed);
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        JOptionPane.showMessageDialog(this, "Password reset successfully! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception ex) {
                    showError("Reset Failed", ex);
                }
            }).start();
        }
    }

    private void showError(String title, Exception ex) {
        ex.printStackTrace();
        SwingUtilities.invokeLater(() -> {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, ex.getMessage(), title, JOptionPane.ERROR_MESSAGE);
        });
    }

    // ── Login handler ─────────────────────────────────────────────
    /**
     * Called when the user clicks "Sign in".
     *
     * Validates that a username was entered, then:
     *  1. Calls PlayerSession.login() to record the username for this session.
     *  2. Invokes the onLogin callback so Main can switch to the main-menu card.
     */
    private void handleLogin(Runnable onLogin) {
        String email = usernameTF.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a username.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }
        //dito walang password hashing
        String password = new String(passwordPF.getPassword());
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a password.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                ApiClient api = new ApiClient();
        
                // 1. Send login request to API (Assuming you update loginUser to return AuthResult too!)
                ApiClient.AuthResult result = api.loginUser(email, password);

                // 2. Pass the ID, username, and Token to the session
                PlayerSession.getInstance().login(result.userId, result.username, result.token);

                javax.swing.SwingUtilities.invokeLater(() -> {
                    javax.swing.JOptionPane.showMessageDialog(null, "Login successful!");
                    onLogin.run();
                });
    
            } catch (Exception ex) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
                });
            }
        }).start();
    }

    // ── Responsive layout ─────────────────────────────────────────
    /**
     * Positions and sizes all components proportionally to the current panel size.
     * Called on resize and first paint. Uses a scale factor derived from the ratio
     * of current size to the BASE_W × BASE_H design dimensions.
     */
    private void layoutComponents(int W, int H) {
        if (W <= 0 || H <= 0) return;

        double scale = Math.min((double) W / BASE_W, (double) H / BASE_H);

        // Scaled font sizes (clamped to readable minimums)
        int fLogo  = Math.max(11, (int)(15 * scale)),
            fTitle = Math.max(13, (int)(18 * scale)),
            fLabel = Math.max( 11, (int)(14 * scale)),
            fBtn   = Math.max( 9, (int)(11 * scale)),
            fLink  = Math.max( 8, (int)(10 * scale));

        Font logoFont  = new Font("Courier New", Font.BOLD,  fLogo);
        Font titleFont = new Font("Courier New", Font.BOLD,  fTitle);
        Font labelFont = new Font("Courier New", Font.BOLD, fLabel);
        Font btnFont   = new Font("Courier New", Font.PLAIN, fBtn);
        Font linkFont  = new Font("Courier New", Font.PLAIN, fLink);

        logoLabel          .setFont(logoFont);
        titleLabel         .setFont(titleFont);
        l1                 .setFont(labelFont);
        l2                 .setFont(labelFont);
        goToMainMenuBtn    .setFont(btnFont);
        registerPromptLabel.setFont(linkFont);
        forgotPasswordLabel.setFont(linkFont);

        // Style the text fields (background, border, etc.)
        styleField(usernameTF);
        styleField(passwordPF);
        usernameTF.setFont(labelFont);
        passwordPF.setFont(labelFont);

        // Rescale eye-icon images to match current panel scale
        if (rawShowImg != null && rawHideImg != null) {
            int sz = Math.max(12, (int)(18 * scale));
            showIcon = new ImageIcon(rawShowImg.getScaledInstance(sz, sz, Image.SCALE_SMOOTH));
            hideIcon = new ImageIcon(rawHideImg.getScaledInstance(sz, sz, Image.SCALE_SMOOTH));
            toggleButton.setIcon(isShowing ? hideIcon : showIcon);
        }

        // Column geometry (stacked: label above field)
        int marginX = (int)(W * 0.15),
            toggleW = (int)(TOGGLE_W * scale),
            fieldW  = W - marginX * 2 - toggleW - Math.max(4, (int)(6 * scale)),
            fieldX  = marginX,
            toggleX = fieldX + fieldW + Math.max(4, (int)(6 * scale)),
            fieldH  = Math.max(20, (int)(24 * scale)),
            labelH  = Math.max(14, (int)(18 * scale)),
            labelGap = Math.max(2, (int)(3 * scale));

        // Row positions (proportional to panel height)
        int rowLogo   = (int)(H * 0.05),
            logoH     = (int)(H * 0.12),
            titleH    = (int)(H * 0.09),
            rowUser   = (int)(H * 0.30),
            rowPass   = (int)(H * 0.44),
            rowLink   = (int)(H * 0.63),
            linkH     = Math.max(14, (int)(16 * scale)),
            rowSignIn = (int)(H * 0.73);

        // Stretch the inner panel to fill this JPanel
        panel.setBounds(0, 0, W, H);

        // Logo + title (centred, stacked)
        logoLabel .setBounds(0, rowLogo,         W, logoH);
        titleLabel.setBounds(0, rowLogo + logoH, W, titleH);

        // Username: label above field
        l1        .setBounds(fieldX, rowUser, fieldW, labelH);
        usernameTF.setBounds(fieldX, rowUser + labelH + labelGap, fieldW, fieldH);

        // Password: label above field + toggle button
        l2          .setBounds(fieldX, rowPass, fieldW, labelH);
        passwordPF  .setBounds(fieldX, rowPass + labelH + labelGap, fieldW, fieldH);
        toggleButton.setBounds(toggleX, rowPass + labelH + labelGap, toggleW, fieldH);

        // Forgot Password link (right-aligned under the password field)
        int forgotY = rowPass + labelH + labelGap + fieldH + Math.max(2, (int)(4 * scale));
        forgotPasswordLabel.setBounds(fieldX, forgotY, fieldW, linkH);
        forgotPasswordLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Register link
        registerPromptLabel.setBounds(0, rowLink, W, linkH);

        // Sign-in button (image-based, right-aligned, proportional)
        int imgW = rawLoginImg.getWidth(null),
            imgH = rawLoginImg.getHeight(null);
        int btnH = Math.max(22, (int)(26 * scale));
        int btnW = (imgW > 0 && imgH > 0) ? (int)(btnH * ((double) imgW / imgH)) : (int)(W * 0.50);
        int btnX = fieldX + fieldW - btnW;   // right-aligned with field edge
        goToMainMenuBtn.setBounds(btnX, rowSignIn, btnW, btnH);
        goToMainMenuBtn.setIcon(new ImageIcon(rawLoginImg.getScaledInstance(btnW, btnH, Image.SCALE_SMOOTH)));

        panel.revalidate();
        panel.repaint();
    }

    // ── Toggle password visibility ────────────────────────────────
    /**
     * Flips between showing and hiding the typed password.
     * Updates the echo character and the toggle button icon accordingly.
     */
    private void togglePassword() {
        isShowing = !isShowing;
        passwordPF.setEchoChar(isShowing ? (char) 0 : '•');
        if (hideIcon != null && showIcon != null)
            toggleButton.setIcon(isShowing ? hideIcon : showIcon);
    }

    // ── Helper: "Don't Have an Account?" link label ───────────────
    /**
     * Builds the clickable register-prompt label.
     * FIX #3 — uses an onRegister Runnable instead of referencing cl/deck directly.
     */
    private JLabel buildRegisterLink(Runnable onRegister) {
        JLabel lbl = new JLabel(
            "<html><center><span style='color:#c8c0e0;'>Don't Have an Account?&nbsp;</span>" +
            "<span style='color:#b0c4ff;'><u>Register</u></span></center></html>",
            SwingConstants.CENTER
        );
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                onRegister.run();   // Main switches to the "reg" card
            }
            @Override public void mouseEntered(MouseEvent e) {
                lbl.setText(
                    "<html><center><span style='color:#c8c0e0;'>Don't Have an Account?&nbsp;</span>" +
                    "<span style='color:#d0d8ff;'><u>Register</u></span></center></html>"
                );
            }
            @Override public void mouseExited(MouseEvent e) {
                lbl.setText(
                    "<html><center><span style='color:#c8c0e0;'>Don't Have an Account?&nbsp;</span>" +
                    "<span style='color:#b0c4ff;'><u>Register</u></span></center></html>"
                );
            }
        });
        return lbl;
    }

    // ── Helper: style a text/password field ──────────────────────
    /**
     * Applies a consistent visual style to a JTextField or JPasswordField.
     */
    private void styleField(JTextField field) {
        field.setBackground(FIELD_BG);
        field.setForeground(FIELD_FG);
        field.setCaretColor(FIELD_FG);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(130, 120, 170), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
    }
}