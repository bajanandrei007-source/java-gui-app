package SourceCode.Builders;

import SourceCode.UserInstances.ApiClient;
import SourceCode.UserInstances.PasswordUtil;
import SourceCode.UserInstances.PlayerSession;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;

/**
 * RegistrationApplication — account creation panel (JPanel card for Main's CardLayout deck).
 *
 * Responsibilities:
 *  - Collects username, email, password, and confirm-password from the user.
 *  - Validates that required fields are filled and that passwords match.
 *  - On successful sign-up, calls PlayerSession to begin a session, then invokes onSuccess.
 *  - Supports showing/hiding passwords via eye-icon toggles on both password fields.
 *  - Scales all fonts and component positions responsively when resized,
 *    using a sequential cursor-based layout (mirrors LoginApplication architecture).
 */
public class RegistrationApplication extends JPanel {

    // ── Swing components ──────────────────────────────────────────
    private final JPanel         panel;
    private final JButton        submitButton, backButton,
                                 toggleBtn1,  toggleBtn2;
    private final JLabel         logoLabel,   titleLabel,
                                 usernameLabel, emailLabel,
                                 passwordLabel, confirmPasswordLabel;
    private final JTextField     usernameTF, emailTF;
    private final JPasswordField passwordPF, confirmPasswordPF;

    // ── Eye-icon images ───────────────────────────────────────────
    private Image     rawLogoImg, rawSignUpImg, rawBackImg,
                      rawShowImg, rawHideImg;
    private ImageIcon showIcon, hideIcon;
    private boolean   isShowingPass    = false,
                      isShowingConfirm = false;

    // ── Colour palette (matches LoginApplication) ─────────────────
    private static final Color BG_COLOR    = new Color( 74,  65, 107),
                               FIELD_BG   = new Color(200, 198, 220),
                               LABEL_COLOR = new Color(230, 225, 255),
                               TEXT_COLOR  = new Color(245, 242, 255),
                               FIELD_FG   = new Color( 30,  25,  55);

    // ── Base design dimensions ────────────────────────────────────
    // Taller than LoginApplication to accommodate 4 fields.
    private static final int BASE_W = 400, BASE_H = 480;
    private static final int TOGGLE_W = 26;

    // ─────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────
    public RegistrationApplication(Runnable onSuccess, Runnable onBack) {
        setLayout(new BorderLayout());

        // Background panel (null layout — positions set manually)
        panel = new JPanel(null);
        panel.setBackground(BG_COLOR);

        // ── Logo (same image resource as LoginApplication) ────────
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
        titleLabel = new JLabel("Create an Account:", SwingConstants.CENTER);
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel);

        // ── Username ──────────────────────────────────────────────
        usernameLabel = makeLabel("Username:");
        panel.add(usernameLabel);

        usernameTF = new JTextField();
        panel.add(usernameTF);

        // ── Email ─────────────────────────────────────────────────
        emailLabel = makeLabel("Email:");
        panel.add(emailLabel);

        emailTF = new JTextField();
        panel.add(emailTF);

        // ── Password ──────────────────────────────────────────────
        passwordLabel = makeLabel("Password:");
        panel.add(passwordLabel);

        passwordPF = new JPasswordField();
        panel.add(passwordPF);

        // ── Confirm Password ──────────────────────────────────────
        confirmPasswordLabel = makeLabel("Confirm Password:");
        panel.add(confirmPasswordLabel);

        confirmPasswordPF = new JPasswordField();
        panel.add(confirmPasswordPF);

        // ── Eye-icon toggles (one per password field) ─────────────
        URL showUrl = getClass().getResource("/ShowP.png"),
            hideUrl = getClass().getResource("/HideP.png");

        if (showUrl != null && hideUrl != null) {
            rawShowImg = new ImageIcon(showUrl).getImage();
            rawHideImg = new ImageIcon(hideUrl).getImage();
            showIcon   = new ImageIcon(rawShowImg.getScaledInstance(18, 18, Image.SCALE_SMOOTH));
            hideIcon   = new ImageIcon(rawHideImg.getScaledInstance(18, 18, Image.SCALE_SMOOTH));
            toggleBtn1 = new JButton(showIcon);
            toggleBtn2 = new JButton(showIcon);
        } else {
            rawShowImg = null;
            rawHideImg = null;
            toggleBtn1 = new JButton("👁");
            toggleBtn2 = new JButton("👁");
            toggleBtn1.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            toggleBtn2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        }

        for (JButton tb : new JButton[]{ toggleBtn1, toggleBtn2 }) {
            tb.setFocusPainted(false);
            tb.setBackground(BG_COLOR);
            tb.setBorder(BorderFactory.createEmptyBorder());
            tb.setContentAreaFilled(false);
            tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        toggleBtn1.addActionListener(e -> togglePassword(1));
        toggleBtn2.addActionListener(e -> togglePassword(2));
        panel.add(toggleBtn1);
        panel.add(toggleBtn2);

        // ── Sign-Up button ────────────────────────────────────────
        URL signUpUrl = getClass().getResource("/Buttons/SignUpButton.png");
        rawSignUpImg  = (signUpUrl != null)
                ? new ImageIcon(signUpUrl).getImage()
                : null;

        submitButton = new JButton();
        submitButton.setContentAreaFilled(false);
        submitButton.setBorderPainted(false);
        submitButton.setFocusPainted(false);
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                submitButton.setBorderPainted(true);
                submitButton.setBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 150), 2));
            }
            @Override public void mouseExited(MouseEvent e) {
                submitButton.setBorderPainted(false);
            }
        });
        submitButton.addActionListener(e -> handleSignUp(onSuccess));
        panel.add(submitButton);

        // ── Back button ───────────────────────────────────────────
        URL backUrl = getClass().getResource("/Buttons/BackButton.png");
        rawBackImg   = (backUrl != null)
                ? new ImageIcon(backUrl).getImage()
                : null;

        backButton = new JButton();
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                backButton.setBorderPainted(true);
                backButton.setBorder(
                    BorderFactory.createLineBorder(new Color(180, 180, 220), 2));
            }
            @Override public void mouseExited(MouseEvent e) {
                backButton.setBorderPainted(false);
            }
        });
        backButton.addActionListener(e -> onBack.run());
        panel.add(backButton);

        // Set default button once the panel is attached to a window
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                JRootPane rp = SwingUtilities.getRootPane(this);
                if (rp != null) rp.setDefaultButton(submitButton);
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
     * Architecture mirrors LoginApplication.layoutComponents() exactly.
     */
    private void layoutComponents(int W, int H) {
        if (W <= 0 || H <= 0) return;

        // Single unified scale factor (same formula as LoginApplication)
        double scale = Math.min((double) W / BASE_W, (double) H / BASE_H);

        // ── Font sizes (clamped to readable minimums) ─────────────
        int fLogo  = Math.max(11, (int)(15 * scale)),
            fTitle = Math.max(13, (int)(18 * scale)),
            fLabel = Math.max(11, (int)(14 * scale)),
            fBtn   = Math.max( 9, (int)(11 * scale));

        Font logoFont  = new Font("Courier New", Font.BOLD,  fLogo);
        Font titleFont = new Font("Courier New", Font.BOLD,  fTitle);
        Font labelFont = new Font("Courier New", Font.BOLD,  fLabel);
        Font btnFont   = new Font("Courier New", Font.PLAIN, fBtn);

        // ── Component heights ─────────────────────────────────────
        int logoH    = (int)(H * 0.25),
            titleH   = Math.max(20, (int)(22 * scale)),
            fieldH   = Math.max(20, (int)(24 * scale)),
            labelH   = Math.max(14, (int)(18 * scale)),
            btnH     = Math.max(22, (int)(36 * scale)),
            gap      = Math.max(4,  (int)( 8 * scale)),
            smallGap = Math.max(2,  (int)( 2 * scale));

        // ── Column geometry ───────────────────────────────────────
        int marginX = (int)(W * 0.15),
            toggleW = (int)(TOGGLE_W * scale),
            fieldW  = W - marginX * 2 - toggleW - Math.max(4, (int)(6 * scale)),
            fieldX  = marginX,
            toggleX = fieldX + fieldW + Math.max(4, (int)(6 * scale));

        // Stretch inner panel to fill
        panel.setBounds(0, 0, W, H);

        // ── Sequential Y cursor ───────────────────────────────────
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

        // ── Username ──────────────────────────────────────────────
        usernameLabel.setFont(labelFont);
        usernameLabel.setBounds(fieldX, y, fieldW, labelH);
        y += labelH + smallGap;

        styleField(usernameTF);
        usernameTF.setFont(labelFont);
        usernameTF.setBounds(fieldX, y, fieldW, fieldH);
        y += fieldH + gap;

        // ── Email ─────────────────────────────────────────────────
        emailLabel.setFont(labelFont);
        emailLabel.setBounds(fieldX, y, fieldW, labelH);
        y += labelH + smallGap;

        styleField(emailTF);
        emailTF.setFont(labelFont);
        emailTF.setBounds(fieldX, y, fieldW, fieldH);
        y += fieldH + gap;

        // ── Password ──────────────────────────────────────────────
        passwordLabel.setFont(labelFont);
        passwordLabel.setBounds(fieldX, y, fieldW, labelH);
        y += labelH + smallGap;

        styleField(passwordPF);
        passwordPF.setFont(labelFont);
        passwordPF.setBounds(fieldX, y, fieldW, fieldH);
        toggleBtn1.setBounds(toggleX, y, toggleW, fieldH);

        // Scale eye icons alongside the field
        if (rawShowImg != null && rawHideImg != null) {
            int sz = Math.max(12, (int)(18 * scale));
            showIcon = new ImageIcon(rawShowImg.getScaledInstance(sz, sz, Image.SCALE_SMOOTH));
            hideIcon = new ImageIcon(rawHideImg.getScaledInstance(sz, sz, Image.SCALE_SMOOTH));
            toggleBtn1.setIcon(isShowingPass    ? hideIcon : showIcon);
            toggleBtn2.setIcon(isShowingConfirm ? hideIcon : showIcon);
        }
        y += fieldH + gap;

        // ── Confirm Password ──────────────────────────────────────
        confirmPasswordLabel.setFont(labelFont);
        confirmPasswordLabel.setBounds(fieldX, y, fieldW, labelH);
        y += labelH + smallGap;

        styleField(confirmPasswordPF);
        confirmPasswordPF.setFont(labelFont);
        confirmPasswordPF.setBounds(fieldX, y, fieldW, fieldH);
        toggleBtn2.setBounds(toggleX, y, toggleW, fieldH);
        y += fieldH + gap;

        // ── Sign-Up button (right-aligned to field edge) ──────────
        submitButton.setFont(btnFont);
        int btnW;
        if (rawSignUpImg != null) {
            int imgW = rawSignUpImg.getWidth(null),
                imgH = rawSignUpImg.getHeight(null);
            btnW = (imgW > 0 && imgH > 0)
                    ? (int)(btnH * ((double) imgW / imgH))
                    : (int)(W * 0.50);
            submitButton.setIcon(new ImageIcon(
                    rawSignUpImg.getScaledInstance(btnW, btnH, Image.SCALE_SMOOTH)));
        } else {
            btnW = (int)(W * 0.40);
            submitButton.setText("Sign Up");
        }
        int btnX = fieldX + fieldW - btnW;
        submitButton.setBounds(btnX, y, btnW, btnH);

        // ── Back button (top-right corner) ────────────────────────
        int backH = Math.max(18, (int)(28 * scale));
        int backW;
        if (rawBackImg != null) {
            int bkW = rawBackImg.getWidth(null),
                bkH = rawBackImg.getHeight(null);
            backW = (bkW > 0 && bkH > 0)
                    ? (int)(backH * ((double) bkW / bkH))
                    : (int)(W * 0.18);
            backButton.setIcon(new ImageIcon(
                    rawBackImg.getScaledInstance(backW, backH, Image.SCALE_SMOOTH)));
        } else {
            backW = (int)(W * 0.18);
            backButton.setText("< Back");
            backButton.setFont(btnFont);
            backButton.setForeground(TEXT_COLOR);
        }
        int backX = W - backW - Math.max(6, (int)(W * 0.03)),
            backY = Math.max(4,  (int)(H * 0.02));
        backButton.setBounds(backX, backY, backW, backH);

        panel.revalidate();
        panel.repaint();
    }

    // ─────────────────────────────────────────────────────────────
    // Toggle password visibility
    // ─────────────────────────────────────────────────────────────

    private void togglePassword(int field) {
        if (field == 1) {
            isShowingPass = !isShowingPass;
            passwordPF.setEchoChar(isShowingPass ? (char) 0 : '•');
            if (showIcon != null && hideIcon != null)
                toggleBtn1.setIcon(isShowingPass ? hideIcon : showIcon);
        } else {
            isShowingConfirm = !isShowingConfirm;
            confirmPasswordPF.setEchoChar(isShowingConfirm ? (char) 0 : '•');
            if (showIcon != null && hideIcon != null)
                toggleBtn2.setIcon(isShowingConfirm ? hideIcon : showIcon);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Sign-up flow
    // ─────────────────────────────────────────────────────────────

    private void handleSignUp(Runnable onSuccess) {
        String username     = usernameTF.getText().trim();
        String email        = emailTF.getText().trim();
        String rawPassword  = new String(passwordPF.getPassword());
        String passwordHash = PasswordUtil.hashPassword(rawPassword.toCharArray());
        String confirm      = new String(confirmPasswordPF.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username cannot be empty.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (rawPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Password cannot be empty.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (rawPassword.length() < 8) {
            JOptionPane.showMessageDialog(this,
                "Password must be 8 characters or more.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!rawPassword.matches(".*[^a-zA-Z0-9].*")) {
            JOptionPane.showMessageDialog(this,
                "Password must have at least 1 special character or more.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!rawPassword.equals(confirm)) {
            JOptionPane.showMessageDialog(this,
                "Passwords do not match.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        }

        submitButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            try {
                ApiClient api = new ApiClient();
                api.sendOtp(email);

                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    String otp = JOptionPane.showInputDialog(
                        RegistrationApplication.this,
                        "An OTP has been sent to " + email + ".\nEnter the 6-digit code:",
                        "Email Verification", JOptionPane.INFORMATION_MESSAGE);

                    if (otp == null || otp.trim().isEmpty()) {
                        submitButton.setEnabled(true);
                        return;
                    }

                    submitButton.setEnabled(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                    new Thread(() -> {
                        try {
                            boolean verified = api.verifyOtp(email, otp.trim());
                            if (verified) {
                                ApiClient.AuthResult result =
                                        api.registerUser(username, email, passwordHash);
                                PlayerSession.getInstance()
                                        .register(result.userId, username, email, result.token);

                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(null, "Registration successful!");
                                    submitButton.setEnabled(true);
                                    setCursor(Cursor.getDefaultCursor());
                                    onSuccess.run();
                                });
                            }
                        } catch (Exception ex) {
                            handleError(ex);
                        }
                    }).start();
                });
            } catch (Exception ex) {
                handleError(ex);
            }
        }).start();
    }

    private void handleError(Exception ex) {
        ex.printStackTrace();
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank())
            msg = "An unexpected error occurred. Check the console.";
        String finalMsg = String.format(
                "Action Failed.\n\nError Type: %s\nDetails: %s",
                ex.getClass().getSimpleName(), msg);
        SwingUtilities.invokeLater(() -> {
            submitButton.setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(null, finalMsg, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(LABEL_COLOR);
        return lbl;
    }

    private void styleField(JTextField field) {
        field.setBackground(FIELD_BG);
        field.setForeground(FIELD_FG);
        field.setCaretColor(FIELD_FG);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(130, 120, 170), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)));
    }
}