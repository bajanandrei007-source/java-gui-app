package SourceCode.Builders;

import SourceCode.UserInstances.ApiClient;
import SourceCode.UserInstances.PasswordUtil;
import SourceCode.UserInstances.PlayerSession;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * RegistrationApp — account creation panel (JPanel card for Main's CardLayout deck).
 *
 * Responsibilities:
 *  - Collects username, email, password, and confirm-password from the user.
 *  - Validates that required fields are filled and that passwords match.
 *  - On successful sign-up, calls PlayerSession.login() to begin a session
 *    with the new username, then invokes onSuccess so Main can switch cards.
 *  - Scales all components proportionally when the panel is resized.
 *
 * NOTE: No data is persisted to disk. The "account" only survives for this session.
 */
public class RegistrationApplication extends JPanel {

    // ── Swing components ──────────────────────────────────────────
    private final JButton        submitButton, backButton;
    private final JLabel         usernameLabel, emailLabel, passwordLabel,
                                 confirmPasswordLabel, titleLabel, logoLabel;
    private final JTextField     usernameTF, emailTF;
    private final JPasswordField passwordPF, confirmPasswordPF;
    private final Image          rawSignUpImg, rawBackImg;

    // ── Colour palette ────────────────────────────────────────────
    private static final Color  BG_COLOR     = new Color(74,  65, 107),
                                FIELD_BGC    = new Color(210, 210, 230),
                                TEXT_COLOR   = new Color(240, 240, 255),
                                LABEL_COLOR  = new Color(220, 215, 245);

    // ── Proportional layout ratios (stacked: label above field) ──
    private static final double FIELD_X_RATIO  = 0.15,
                                FIELD_W_RATIO  = 0.70,
                                LABEL_H_RATIO  = 0.05,
                                FIELD_H_RATIO  = 0.075,
                                LABEL_GAP      = 0.005,
                                START_Y_RATIO  = 0.24,
                                GAP_RATIO      = 0.14,
                                BTN_W_RATIO    = 0.35,
                                BTN_H_RATIO    = 0.09;

    // ── Constructor ───────────────────────────────────────────────
    /**
     * Builds the registration panel.
     *
     * @param onSuccess  Runnable called after a successful sign-up
     *                   (Main switches to the "mm" card).
     * @param onBack     Runnable called when "< Back" is clicked
     *                   (Main switches to the "log" card).
     */
    public RegistrationApplication(Runnable onSuccess, Runnable onBack) {
        // FIX — null layout on this JPanel directly (replaces getContentPane())
        setLayout(null);
        setBackground(BG_COLOR);

        // ── Logo ──────────────────────────────────────────────────
        logoLabel = new JLabel("LogicLab", SwingConstants.CENTER);
        logoLabel.setForeground(TEXT_COLOR);
        logoLabel.setFont(new Font("Courier New", Font.BOLD, 13));

        // ── Title ─────────────────────────────────────────────────
        titleLabel = new JLabel("Create an Account:", SwingConstants.CENTER);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Courier New", Font.PLAIN, 12));

        // ── Form fields ───────────────────────────────────────────
        usernameLabel       = makeLabel("Username:");
        usernameTF          = new JTextField();
        styleField(usernameTF);

        emailLabel          = makeLabel("Email:");
        emailTF             = new JTextField();
        styleField(emailTF);

        passwordLabel       = makeLabel("Password:");
        passwordPF          = new JPasswordField();
        styleField(passwordPF);

        confirmPasswordLabel= makeLabel("Confirm Password:");
        confirmPasswordPF   = new JPasswordField();
        styleField(confirmPasswordPF);

        // ── Sign-Up button (image-based) ───────────────────────────
        rawSignUpImg = new ImageIcon("Buttons/SignUpButton.png").getImage();
        submitButton = new JButton();
        submitButton.setContentAreaFilled(false);
        submitButton.setBorderPainted(false);
        submitButton.setFocusPainted(false);
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // FIX — defer setDefaultButton until the panel is attached to a window
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                JRootPane rp = SwingUtilities.getRootPane(this);
                if (rp != null) rp.setDefaultButton(submitButton);
            }
        });

        // Hover effect — slight border glow
        submitButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                submitButton.setBorderPainted(true);
                submitButton.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 150), 2));
            }
            public void mouseExited(MouseEvent e) {
                submitButton.setBorderPainted(false);
            }
        });

        submitButton.addActionListener(e -> handleSignUp(onSuccess));

        // ── Back button (image-based) ─────────────────────────────
        rawBackImg = new ImageIcon("Buttons/BackButton.png").getImage();
        backButton = new JButton();
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                backButton.setBorderPainted(true);
                backButton.setBorder(BorderFactory.createLineBorder(
                    new Color(180, 180, 220), 2));
            }
            public void mouseExited(MouseEvent e) {
                backButton.setBorderPainted(false);
            }
        });
        // FIX — use callback instead of new LoginApplication(this) + dispose()
        backButton.addActionListener(e -> onBack.run());

        // Add all components directly to this JPanel
        add(logoLabel);  add(titleLabel);        add(usernameLabel);
        add(usernameTF); add(passwordLabel);      add(confirmPasswordPF);
        add(emailLabel); add(passwordPF);         add(confirmPasswordLabel);
        add(emailTF);    add(submitButton);        add(backButton);

        // Re-position everything whenever the panel size changes
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) { relayout(); }
            public void componentShown (ComponentEvent e) { relayout(); }
        });
    }

    // ── Sign-up handler ───────────────────────────────────────────
    private void handleSignUp(Runnable onSuccess) {
        String username  = usernameTF.getText().trim();
        String email     = emailTF.getText().trim();
        String rawPassword  = new String(passwordPF.getPassword());
        String passwordHash = PasswordUtil.hashPassword(rawPassword.toCharArray());
        String confirm   = new String(confirmPasswordPF.getPassword());
        String studGmail   = "lag@student.fatima.edu.ph",
               fatimaGmail = "@fatima.edu.ph";

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username cannot be empty.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        }

        /*if (!(email.endsWith(studGmail) || email.endsWith(fatimaGmail))) {
            JOptionPane.showMessageDialog(this,
                "E-mail domain must be from OLFU.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        }*/

        if (rawPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Password cannot be empty.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (rawPassword.length() < 8) {
            JOptionPane.showMessageDialog(this,
                "Password must be 8 characters or more.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        } else if (!rawPassword.matches(".*[^a-zA-Z0-9].*")) {
            JOptionPane.showMessageDialog(this,
                "Password must atleast have 1 special character or more.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!rawPassword.equals(confirm)) {
            JOptionPane.showMessageDialog(this,
                "Passwords do not match.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Disable button while processing
        submitButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            try {
                ApiClient api = new ApiClient();
                
                // --- 1. SEND OTP ---
                api.sendOtp(email);
                
                // Prompt user for OTP on EDT
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    String otp = JOptionPane.showInputDialog(
                        RegistrationApplication.this, 
                        "An OTP has been sent to " + email + ".\nEnter the 6-digit code:",
                        "Email Verification",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    if (otp == null || otp.trim().isEmpty()) {
                        submitButton.setEnabled(true);
                        return; // User cancelled
                    }
                    
                    // --- 2. VERIFY OTP AND REGISTER ---
                    submitButton.setEnabled(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    
                    new Thread(() -> {
                        try {
                            boolean verified = api.verifyOtp(email, otp.trim());
                            if (verified) {
                                // 3. Get the "Box" back from the API
                                ApiClient.AuthResult result = api.registerUser(username, email, passwordHash);

                                // 4. Open the Box and update the Singleton!
                                PlayerSession.getInstance().register(result.userId, username, email, result.token);

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
        String errorType = ex.getClass().getSimpleName();
        String specificMessage = ex.getMessage();
        if (specificMessage == null || specificMessage.isBlank()) {
            specificMessage = "An unexpected system error occurred. Check the console.";
        }
        String finalMessage = String.format("Action Failed.\n\nError Type: %s\nDetails: %s", errorType, specificMessage);

        SwingUtilities.invokeLater(() -> {
            submitButton.setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(
                null, 
                finalMessage, 
                "Error", 
                JOptionPane.ERROR_MESSAGE
            );
        });
    }

    // ── Responsive layout ─────────────────────────────────────────
    /**
     * Repositions and resizes all components proportionally to the current panel size.
     * FIX — reads getWidth()/getHeight() directly (replaces getContentPane()).
     */
    private void relayout() {
        int w = getWidth(),
            h = getHeight();
        if (w <= 0 || h <= 0) return;

        int fieldX = (int)(w * FIELD_X_RATIO),
            fieldW = (int)(w * FIELD_W_RATIO),
            labelH = (int)(h * LABEL_H_RATIO),
            fieldH = (int)(h * FIELD_H_RATIO),
            labelGap = (int)(h * LABEL_GAP),
            startY = (int)(h * START_Y_RATIO),
            gap    = (int)(h * GAP_RATIO);

        int logoH = (int)(h * 0.08),
            logoY = (int)(h * 0.04);
        logoLabel .setBounds(0, logoY,        w, logoH);
        logoLabel .setFont(scaledFont("Courier New", Font.BOLD,  w, 0.042f));
        titleLabel.setBounds(0, logoY + logoH, w, logoH);
        titleLabel.setFont(scaledFont("Courier New", Font.PLAIN, w, 0.036f));

        JLabel[]     labels = { usernameLabel, emailLabel, passwordLabel, confirmPasswordLabel };
        JTextField[] fields = { usernameTF,    emailTF,    passwordPF,    confirmPasswordPF    };

        for (int i = 0; i < labels.length; i++) {
            int rowY = startY + i * gap;

            // Label sits directly above the field
            labels[i].setBounds(fieldX, rowY, fieldW, labelH);
            labels[i].setFont(scaledFont("Courier New", Font.BOLD, w, 0.033f));
            fields[i].setBounds(fieldX, rowY + labelH + labelGap, fieldW, fieldH);
            fields[i].setFont(scaledFont("Courier New", Font.PLAIN, w, 0.033f));
        }

        // Scale button image proportionally (no stretching)
        int imgW = rawSignUpImg.getWidth(null),
            imgH = rawSignUpImg.getHeight(null);
        int btnH = (int)(h * BTN_H_RATIO);
        int btnW = (imgW > 0 && imgH > 0) ? (int)(btnH * ((double) imgW / imgH)) : (int)(w * BTN_W_RATIO);
        int btnX = fieldX + fieldW - btnW,   // right-aligned with field edge
            btnY = startY + labels.length * gap + (int)(h * 0.02);
        submitButton.setBounds(btnX, btnY, btnW, btnH);
        submitButton.setIcon(new ImageIcon(rawSignUpImg.getScaledInstance(btnW, btnH, Image.SCALE_SMOOTH)));

        int bkImgW = rawBackImg.getWidth(null), bkImgH = rawBackImg.getHeight(null);
        int backH = (int)(h * 0.055);
        int backW = (bkImgW > 0 && bkImgH > 0)
                    ? (int)(backH * ((double) bkImgW / bkImgH)) : (int)(w * 0.18);
        int backX = w - backW - (int)(w * 0.03),
            backY = (int)(h * 0.02);
        backButton.setBounds(backX, backY, backW, backH);
        backButton.setIcon(new ImageIcon(rawBackImg.getScaledInstance(backW, backH, Image.SCALE_SMOOTH)));

        revalidate();
        repaint();
    }

    // ── Helpers ───────────────────────────────────────────────────
    private Font scaledFont(String name, int style, int windowWidth, float ratio) {
        int size = Math.max(9, (int)(windowWidth * ratio));
        return new Font(name, style, size);
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(LABEL_COLOR);
        lbl.setFont(new Font("Courier New", Font.BOLD, 12));
        return lbl;
    }

    private void styleField(JTextField field) {
        field.setBackground(FIELD_BGC);
        field.setForeground(new Color(30, 25, 55));
        field.setFont(new Font("Courier New", Font.BOLD, 12));
        field.setCaretColor(new Color(30, 25, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(130, 120, 170), 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)
        ));
    }
}