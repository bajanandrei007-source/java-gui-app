package SourceCode.Builders;

import SourceCode.UserInstances.ApiClient;
import SourceCode.UserInstances.PlayerSession;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;

/**
 * LoginApplication — a JPanel card used inside Main's CardLayout deck.
 *
 * Responsibilities:
 *  - Collects a username and password from the user.
 *  - On "Sign in", calls the API, stores session data, then navigates to the main menu.
 *  - Provides a link to RegistrationApp for new users.
 *  - Supports showing/hiding the typed password via an eye-icon toggle.
 *  - Scales all fonts and component positions responsively when resized,
 *    using a sequential cursor-based layout to prevent overlapping.
 */
public class LoginApplication extends JPanel {

    // ── Swing components ──────────────────────────────────────────
    private final JPanel         panel;
    private final JButton        goToMainMenuBtn, toggleButton;
    private final JLabel         logoLabel, titleLabel, l1, l2,
                                 registerPromptLabel, forgotPasswordLabel;
    private final JTextField     usernameTF;
    private final JPasswordField passwordPF;

    // ── Eye-icon images ───────────────────────────────────────────
    private Image     rawShowImg, rawHideImg, rawLoginImg, rawLogoImg;
    private ImageIcon showIcon, hideIcon;
    private boolean   isShowing = false;

    // ── Colour palette (purple-slate theme) ───────────────────────
    private static final Color BG_COLOR    = new Color( 74,  65, 107),
                               FIELD_BG   = new Color(200, 198, 220),
                               LABEL_COLOR = new Color(230, 225, 255),
                               TEXT_COLOR  = new Color(245, 242, 255),
                               FIELD_FG   = new Color( 30,  25,  55);

    // ── Base design dimensions ────────────────────────────────────
    private static final int BASE_W = 400, BASE_H = 340;
    private static final int TOGGLE_W = 26;

    // ─────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────
    public LoginApplication(Runnable onLogin, Runnable onRegister) {

        setLayout(new BorderLayout());

        // Background panel (null layout — positions set manually)
        panel = new JPanel(null);
        panel.setBackground(BG_COLOR);

        // ── Logo ──────────────────────────────────────────────────
        URL logoUrl = getClass().getResource("/LogicLab_LogoLabel_White.png");
        if (logoUrl != null) {
            rawLogoImg = new ImageIcon(logoUrl).getImage();
            logoLabel  = new JLabel(new ImageIcon(rawLogoImg), SwingConstants.CENTER);
        } else {
            rawLogoImg = null;
            logoLabel  = new JLabel("LOGIC LAB", SwingConstants.CENTER);
            logoLabel.setForeground(TEXT_COLOR);
        }
        panel.add(logoLabel);

        // ── Title ─────────────────────────────────────────────────
        titleLabel = new JLabel("Sign in:", SwingConstants.CENTER);
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel);

        // ── Email field ───────────────────────────────────────────
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

        // ── Eye-icon toggle ───────────────────────────────────────
        URL showUrl = getClass().getResource("/ShowP.png"),
            hideUrl = getClass().getResource("/HideP.png");

        if (showUrl != null && hideUrl != null) {
            rawShowImg   = new ImageIcon(showUrl).getImage();
            rawHideImg   = new ImageIcon(hideUrl).getImage();
            showIcon     = new ImageIcon(rawShowImg.getScaledInstance(18, 18, Image.SCALE_SMOOTH));
            hideIcon     = new ImageIcon(rawHideImg.getScaledInstance(18, 18, Image.SCALE_SMOOTH));
            toggleButton = new JButton(showIcon);
        } else {
            rawShowImg   = null;
            rawHideImg   = null;
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

        // ── Forgot Password link ──────────────────────────────────
        forgotPasswordLabel = new JLabel("Forgot Password?");
        forgotPasswordLabel.setForeground(new Color(176, 196, 255));
        forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleForgotPassword(); }
            @Override public void mouseEntered(MouseEvent e) {
                forgotPasswordLabel.setText("<html><u>Forgot Password?</u></html>");
            }
            @Override public void mouseExited(MouseEvent e) {
                forgotPasswordLabel.setText("Forgot Password?");
            }
        });
        panel.add(forgotPasswordLabel);

        // ── Register link ─────────────────────────────────────────
        registerPromptLabel = buildRegisterLink(onRegister);
        panel.add(registerPromptLabel);

        // ── Login button ──────────────────────────────────────────
        URL loginBtnUrl = getClass().getResource("/Buttons/LoginButton.png");
        rawLoginImg = (loginBtnUrl != null)
                ? new ImageIcon(loginBtnUrl).getImage()
                : null;

        goToMainMenuBtn = new JButton();
        goToMainMenuBtn.setContentAreaFilled(false);
        goToMainMenuBtn.setBorderPainted(false);
        goToMainMenuBtn.setFocusPainted(false);
        goToMainMenuBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        goToMainMenuBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                goToMainMenuBtn.setBorderPainted(true);
                goToMainMenuBtn.setBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 150), 2));
            }
            @Override public void mouseExited(MouseEvent e) {
                goToMainMenuBtn.setBorderPainted(false);
            }
        });
        goToMainMenuBtn.addActionListener(e -> handleLogin(onLogin));
        panel.add(goToMainMenuBtn);

        // Set default button once the panel is visible
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                JRootPane rp = SwingUtilities.getRootPane(this);
                if (rp != null) rp.setDefaultButton(goToMainMenuBtn);
            }
        });

        add(panel, BorderLayout.CENTER);

        // Re-layout on resize or show
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { layoutComponents(); }
            @Override public void componentShown (ComponentEvent e) { layoutComponents(); }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // Layout
    // ─────────────────────────────────────────────────────────────

    private void layoutComponents() {
        layoutComponents(getWidth(), getHeight());
    }

    /**
     * Sequential cursor-based layout — each component is placed directly
     * below the previous one, making overlapping impossible at any size.
     */
    private void layoutComponents(int W, int H) {
        if (W <= 0 || H <= 0) return;

        double scale = Math.min((double) W / BASE_W, (double) H / BASE_H);

        // Font sizes (clamped to readable minimums)
        int fLogo  = Math.max(11, (int)(15 * scale)),
            fTitle = Math.max(13, (int)(18 * scale)),
            fLabel = Math.max(11, (int)(14 * scale)),
            fBtn   = Math.max( 9, (int)(11 * scale)),
            fLink  = Math.max( 8, (int)(10 * scale));

        Font logoFont  = new Font("Courier New", Font.BOLD,  fLogo);
        Font titleFont = new Font("Courier New", Font.BOLD,  fTitle);
        Font labelFont = new Font("Courier New", Font.BOLD,  fLabel);
        Font btnFont   = new Font("Courier New", Font.PLAIN, fBtn);
        Font linkFont  = new Font("Courier New", Font.PLAIN, fLink);

        // Component heights
        int logoH    = (int)(H * 0.25),
            titleH   = Math.max(20, (int)(22 * scale)),
            fieldH   = Math.max(20, (int)(24 * scale)),
            labelH   = Math.max(14, (int)(18 * scale)),
            linkH    = Math.max(14, (int)(16 * scale)),
            btnH     = Math.max(22, (int)(36 * scale)),
            gap      = Math.max(4,  (int)( 4 * scale)),
            smallGap = Math.max(2,  (int)( 2 * scale));

        // Column geometry
        int marginX = (int)(W * 0.15),
            toggleW = (int)(TOGGLE_W * scale),
            fieldW  = W - marginX * 2 - toggleW - Math.max(4, (int)(6 * scale)),
            fieldX  = marginX,
            toggleX = fieldX + fieldW + Math.max(4, (int)(6 * scale));

        // Stretch inner panel
        panel.setBounds(0, 0, W, H);

        // Sequential Y cursor
        int y = (int)(H * 0.03);

        // ── Logo ──────────────────────────────────────────────────
        logoLabel.setBounds(0, y, W, logoH);
        if (rawLogoImg != null) {
            int origW = rawLogoImg.getWidth(null),
                origH = rawLogoImg.getHeight(null);
            int iH = Math.max(80, logoH);
            int iW = (origW > 0 && origH > 0)
                    ? (int)(iH * ((double) origW / origH)) : iH;
            logoLabel.setIcon(new ImageIcon(
                    rawLogoImg.getScaledInstance(iW, iH, Image.SCALE_SMOOTH)));
            logoLabel.setText(null);
        } else {
            logoLabel.setFont(logoFont);
        }
        y += logoH + gap;

        // ── Title ─────────────────────────────────────────────────
        titleLabel.setFont(titleFont);
        titleLabel.setBounds(0, y, W, titleH);
        y += titleH + gap;

        // ── Email ─────────────────────────────────────────────────
        l1.setFont(labelFont);
        l1.setBounds(fieldX, y, fieldW, labelH);
        y += labelH + smallGap;

        styleField(usernameTF);
        usernameTF.setFont(labelFont);
        usernameTF.setBounds(fieldX, y, fieldW, fieldH);
        y += fieldH + smallGap;

        // ── Register link ─────────────────────────────────────────
        registerPromptLabel.setFont(linkFont);
        registerPromptLabel.setBounds(0, y, W, linkH);
        y += linkH + smallGap;

        // ── Password ──────────────────────────────────────────────
        l2.setFont(labelFont);
        l2.setBounds(fieldX, y, fieldW, labelH);
        y += labelH + smallGap;

        styleField(passwordPF);
        passwordPF.setFont(labelFont);
        passwordPF.setBounds(fieldX, y, fieldW, fieldH);
        toggleButton.setBounds(toggleX, y, toggleW, fieldH);

        if (rawShowImg != null && rawHideImg != null) {
            int sz = Math.max(12, (int)(18 * scale));
            showIcon = new ImageIcon(rawShowImg.getScaledInstance(sz, sz, Image.SCALE_SMOOTH));
            hideIcon = new ImageIcon(rawHideImg.getScaledInstance(sz, sz, Image.SCALE_SMOOTH));
            toggleButton.setIcon(isShowing ? hideIcon : showIcon);
        }
        y += fieldH + smallGap;

        // ── Forgot Password ───────────────────────────────────────
        forgotPasswordLabel.setFont(linkFont);
        forgotPasswordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        forgotPasswordLabel.setBounds(fieldX, y, fieldW, linkH);
        y += linkH + gap;

        // ── Login button ──────────────────────────────────────────
        goToMainMenuBtn.setFont(btnFont);
        int btnW;
        if (rawLoginImg != null) {
            int imgW = rawLoginImg.getWidth(null),
                imgH = rawLoginImg.getHeight(null);
            btnW = (imgW > 0 && imgH > 0)
                    ? (int)(btnH * ((double) imgW / imgH))
                    : (int)(W * 0.50);
            goToMainMenuBtn.setIcon(new ImageIcon(
                    rawLoginImg.getScaledInstance(btnW, btnH, Image.SCALE_SMOOTH)));
        } else {
            btnW = (int)(W * 0.40);
            goToMainMenuBtn.setText("Login");
        }
        int btnX = fieldX + fieldW - btnW;
        goToMainMenuBtn.setBounds(btnX, y, btnW, btnH);

        panel.revalidate();
        panel.repaint();
    }

    // ─────────────────────────────────────────────────────────────
    // Forgot Password flow
    // ─────────────────────────────────────────────────────────────

    private void handleForgotPassword() {
        String email = JOptionPane.showInputDialog(this,
                "Enter your email address:", "Forgot Password",
                JOptionPane.QUESTION_MESSAGE);
        if (email == null || email.trim().isEmpty()) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            try {
                ApiClient api = new ApiClient();
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
        int option = JOptionPane.showConfirmDialog(this,
                new Object[]{"New Password:", pf1, "Confirm New Password:", pf2},
                "Reset Password", JOptionPane.OK_CANCEL_OPTION);

        if (option != JOptionPane.OK_OPTION) return;

        String pass1 = new String(pf1.getPassword());
        String pass2 = new String(pf2.getPassword());

        if (pass1.isEmpty() || pass1.length() < 8 || !pass1.matches(".*[^a-zA-Z0-9].*")) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 8 characters and contain 1 special character.",
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!pass1.equals(pass2)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String hashed = SourceCode.UserInstances.PasswordUtil.hashPassword(pf1.getPassword());

        new Thread(() -> {
            try {
                api.resetPassword(email, otp, hashed);
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(this,
                            "Password reset successfully! You can now log in.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception ex) {
                showError("Reset Failed", ex);
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────
    // Login flow
    // ─────────────────────────────────────────────────────────────

    private void handleLogin(Runnable onLogin) {
        String email = usernameTF.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter an email.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String password = new String(passwordPF.getPassword());
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a password.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            try {
                ApiClient api = new ApiClient();
                ApiClient.AuthResult result = api.loginUser(email, password);
                PlayerSession.getInstance().login(result.userId, result.username, result.token);

                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(null, "Login successful!");
                    onLogin.run();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(null, ex.getMessage(),
                            "Login Failed", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────
    // Toggle password visibility
    // ─────────────────────────────────────────────────────────────

    private void togglePassword() {
        isShowing = !isShowing;
        passwordPF.setEchoChar(isShowing ? (char) 0 : '•');
        if (showIcon != null && hideIcon != null)
            toggleButton.setIcon(isShowing ? hideIcon : showIcon);
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private JLabel buildRegisterLink(Runnable onRegister) {
        JLabel lbl = new JLabel(
            "<html><center>" +
            "<span style='color:#c8c0e0;'>Don't Have an Account?&nbsp;</span>" +
            "<span style='color:#b0c4ff;'><u>Register</u></span>" +
            "</center></html>",
            SwingConstants.CENTER
        );
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onRegister.run(); }
            @Override public void mouseEntered(MouseEvent e) {
                lbl.setText(
                    "<html><center>" +
                    "<span style='color:#c8c0e0;'>Don't Have an Account?&nbsp;</span>" +
                    "<span style='color:#d0d8ff;'><u>Register</u></span>" +
                    "</center></html>");
            }
            @Override public void mouseExited(MouseEvent e) {
                lbl.setText(
                    "<html><center>" +
                    "<span style='color:#c8c0e0;'>Don't Have an Account?&nbsp;</span>" +
                    "<span style='color:#b0c4ff;'><u>Register</u></span>" +
                    "</center></html>");
            }
        });
        return lbl;
    }

    private void styleField(JTextField field) {
        field.setBackground(FIELD_BG);
        field.setForeground(FIELD_FG);
        field.setCaretColor(FIELD_FG);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(130, 120, 170), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
    }

    private void showError(String title, Exception ex) {
        ex.printStackTrace();
        SwingUtilities.invokeLater(() -> {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, ex.getMessage(), title,
                    JOptionPane.ERROR_MESSAGE);
        });
    }
}