package SourceCode.Builders.ChallengeConstructor;

import SourceCode.Judge.JavaJudge.CaseResult;
import SourceCode.Judge.JavaJudge.TestResult;
import SourceCode.UserInstances.PlayerSession;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * ChallengeConstructor — abstract base class for every coding challenge screen.
 *
 * Layout (BorderLayout):
 *  NORTH  — top bar (challenge title, difficulty badge, ← Main Menu button).
 *  CENTER — horizontal split: problem description + test-case results (left),
 *            code editor with line numbers (right).
 *  SOUTH  — bottom bar: status label | "▶ Run" button | "Submit" button.
 *
 * Subclasses must implement:
 *  - challengeNumber()   → "1", "2", … (used as part of the points key)
 *  - challengeTitle()    → human-readable name
 *  - difficultyLabel()   → "EASY" | "MEDIUM" | "HARD"
 *  - difficultyColor()   → accent colour constant
 *  - pointsReward()      → points awarded on first full pass (1 / 3 / 5)
 *  - problemHtml()       → HTML describing the problem
 *  - starterCode()       → pre-filled code skeleton
 *  - judgeFunction()     → a Function<String, TestResult> that runs ALL test cases
 *
 * Run vs Submit:
 *  ▶ Run    — executes only the SAMPLE test cases (first 3) so the student
 *             can get quick feedback without seeing all hidden cases.
 *  Submit   — executes ALL test cases; if every case passes, points are awarded
 *             once and a dialog offers to go back to the main menu.
 */
public abstract class ChallengeConstructor extends JFrame {

    protected static final Color BG_DARK      = new Color(13,  17,  23),
                                  BG_PANEL     = new Color(22,  27,  34),
                                  BG_EDITOR    = new Color(30,  35,  42),
                                  ACCENT       = new Color(88, 166, 255),
                                  ACCENT_GREEN = new Color(63, 185,  80),
                                  ACCENT_RED   = new Color(248, 81,  73),
                                  ACCENT_GOLD  = new Color(210, 153, 34),
                                  TEXT_PRIMARY = new Color(230, 237, 243),
                                  TEXT_MUTED   = new Color(125, 133, 144),
                                  BORDER_COLOR = new Color(48,  54,  61);

    protected static final Font MONO_FONT = new Font("JetBrains Mono", Font.PLAIN, 13),
                                 UI_FONT   = new Font("Segoe UI",       Font.PLAIN, 13),
                                 UI_BOLD   = new Font("Segoe UI",       Font.BOLD,  13);

    private final JFrame parent;
    private final Runnable onReturn;

    protected JTextArea codeArea, lineNumbers;
    protected JLabel    statusLabel;
    
    private Image rawRunImg, rawSubmitImg, rawMainMenuImg;

    protected JPanel testCasePanel;
    protected abstract String challengeNumber();
    protected abstract String challengeTitle();
    protected abstract String problemHtml();
    protected abstract String starterCode();
    protected abstract String difficultyLabel();
    protected abstract Color difficultyColor();
    protected abstract int pointsReward();

    /**
     * Returns a Function that, given the student's code as a String,
     * runs ALL test cases and returns a TestResult.
     */
    protected abstract Function<String, TestResult> judgeFunction();

    public ChallengeConstructor(JFrame parent, Runnable onReturn) {
        this.parent   = parent;
        this.onReturn = onReturn;

        rawRunImg      = new ImageIcon(getClass().getResource("/Buttons/RunButton.png")).getImage();
        rawSubmitImg   = new ImageIcon(getClass().getResource("/Buttons/SubmitButton.png")).getImage();
        rawMainMenuImg = new ImageIcon(getClass().getResource("/Buttons/MainMenuButton.png")).getImage();

        // Force fullscreen: get the maximum usable screen area
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenBounds = ge.getMaximumWindowBounds();
        setBounds(screenBounds);
        setMinimumSize(new Dimension(900, 560));
        setTitle("Challenge: " + challengeTitle());
        setLayout(new BorderLayout());
        // DISPOSE_ON_CLOSE so the WindowListener fires correctly
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent we) {
                onReturn.run();
            }
        });

        add(buildTopBar(
            challengeNumber() + ": " + challengeTitle(),
            " " + difficultyLabel(),
            difficultyColor(),
            e -> {
                dispose();       // windowClosed listener will call onReturn
            }
        ), BorderLayout.NORTH);

        add(buildMainSplit(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        setVisible(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /**
     * Creates the horizontal JSplitPane dividing the problem panel (left)
     * from the code editor (right). The divider starts at 370 px.
     */
    private JSplitPane buildMainSplit() {
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            buildLeftPanel(problemHtml()),
            buildRightPanel(starterCode())
        );
        split.setDividerLocation(370);
        split.setDividerSize(4);
        split.setBackground(BORDER_COLOR);
        split.setBorder(null);
        return split;
    }

    /**
     * Builds the top bar containing:
     *  LEFT  — challenge title + difficulty badge.
     *  RIGHT — "← Main Menu" back button.
     *
     * Exposed as a protected method so subclasses can override if needed.
     *
     * @param title      Text to display as the challenge name.
     * @param difficulty Short difficulty label (e.g. " EASY").
     * @param diffColor  Colour for the difficulty badge text.
     * @param onBack     ActionListener triggered by the back button.
     */
    protected JPanel buildTopBar(String title, String difficulty,
                                  Color diffColor, ActionListener onBack) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_PANEL);
        bar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(10, 18, 10, 18)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UI_BOLD.deriveFont(15f));
        titleLbl.setForeground(TEXT_PRIMARY);

        JLabel diffLbl = new JLabel(difficulty);
        diffLbl.setFont(UI_BOLD.deriveFont(11f));
        diffLbl.setForeground(diffColor);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(titleLbl);
        left.add(diffLbl);

        int backBtnH = 28;
        int mmImgW = rawMainMenuImg.getWidth(null), mmImgH = rawMainMenuImg.getHeight(null);
        int backBtnW = (mmImgW > 0 && mmImgH > 0)
                       ? (int)(backBtnH * ((double) mmImgW / mmImgH)) : 100;

        JButton backBtn = new JButton(
            new ImageIcon(rawMainMenuImg.getScaledInstance(backBtnW, backBtnH, Image.SCALE_SMOOTH))
        );
        backBtn.setPreferredSize(new Dimension(backBtnW, backBtnH));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setToolTipText("Return to Main Menu");
        backBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                backBtn.setBorderPainted(true);
                backBtn.setBorder(BorderFactory.createLineBorder(
                    new Color(180, 180, 220), 2));
            }
            public void mouseExited(MouseEvent e) {
                backBtn.setBorderPainted(false);
            }
        });
        backBtn.addActionListener(onBack);

        bar.add(left,    BorderLayout.WEST);
        bar.add(backBtn, BorderLayout.EAST);
        return bar;
    }

    /**
     * Builds the left side of the split pane.
     *
     * NORTH  — scrollable HTML problem description.
     * CENTER — scrollable test-case result cards (populated after Run/Submit).
     *
     * @param html  The problem description HTML string.
     */
    protected JPanel buildLeftPanel(String html) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PANEL);

        // Problem description pane (read-only HTML)
        JTextPane problemText = new JTextPane();
        problemText.setContentType("text/html");
        problemText.setText(html);
        problemText.setEditable(false);
        problemText.setBackground(BG_PANEL);
        problemText.setBorder(null);

        JScrollPane problemScroll = new JScrollPane(problemText);
        problemScroll.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));
        problemScroll.setPreferredSize(new Dimension(370, 230));
        problemScroll.getViewport().setBackground(BG_PANEL);

        testCasePanel = new JPanel();
        testCasePanel.setLayout(new BoxLayout(testCasePanel, BoxLayout.Y_AXIS));
        testCasePanel.setBackground(BG_PANEL);
        testCasePanel.setBorder(new EmptyBorder(10, 10, 6, 0));

        JLabel tcHeader = new JLabel("TEST CASES");
        tcHeader.setFont(UI_BOLD.deriveFont(11f));
        tcHeader.setForeground(TEXT_MUTED);
        tcHeader.setBorder(new EmptyBorder(10, 10, 6, 0));

        JScrollPane tcScroll = new JScrollPane(testCasePanel);
        tcScroll.setBorder(null);
        tcScroll.getViewport().setBackground(BG_PANEL);

        JPanel resultsWrapper = new JPanel(new BorderLayout());
        resultsWrapper.setBackground(BG_PANEL);
        resultsWrapper.add(tcHeader,  BorderLayout.NORTH);
        resultsWrapper.add(tcScroll,  BorderLayout.CENTER);

        panel.add(problemScroll,  BorderLayout.NORTH);
        panel.add(resultsWrapper, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds the code editor on the right side:
     *  NORTH  — language badge ("JAVA").
     *  CENTER — scrollable code area with a line-number gutter on the left.
     *
     * @param code  Starter code to pre-fill the editor.
     */
    protected JPanel buildRightPanel(String code) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_EDITOR);

        // Language badge
        JLabel langBadge = new JLabel("JAVA");
        langBadge.setFont(UI_BOLD.deriveFont(11f));
        langBadge.setForeground(ACCENT);
        langBadge.setOpaque(true);
        langBadge.setBackground(new Color(30, 47, 68));
        langBadge.setBorder(new EmptyBorder(4, 8, 4, 8));

        JPanel editorHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        editorHeader.setBackground(BG_EDITOR);
        editorHeader.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));
        editorHeader.add(langBadge);

        codeArea = new JTextArea();
        codeArea.setFont(MONO_FONT);
        codeArea.setBackground(BG_EDITOR);
        codeArea.setForeground(TEXT_PRIMARY);
        codeArea.setCaretColor(ACCENT);
        codeArea.setSelectionColor(new Color(56, 90, 138));
        codeArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        codeArea.setText(code);

        lineNumbers = new JTextArea();
        lineNumbers.setBackground(new Color(22, 27, 34));
        lineNumbers.setForeground(TEXT_MUTED);
        lineNumbers.setEditable(false);
        lineNumbers.setFont(MONO_FONT);
        lineNumbers.setBorder(new EmptyBorder(8, 6, 8, 8));
        lineNumbers.setFocusable(false);
        updateLineNumbers();

        // Keep line numbers in sync as the user types
        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate (DocumentEvent e) { updateLineNumbers(); }
            public void removeUpdate (DocumentEvent e) { updateLineNumbers(); }
            public void changedUpdate(DocumentEvent e) { updateLineNumbers(); }
        });

        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setRowHeaderView(lineNumbers);
        codeScroll.setBorder(null);
        codeScroll.getViewport().setBackground(BG_EDITOR);
        codeScroll.getRowHeader().setBackground(new Color(22, 27, 34));

        panel.add(editorHeader, BorderLayout.NORTH);
        panel.add(codeScroll,   BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds the bottom bar containing:
     *  LEFT   — status label (shows compile errors, pass/fail counts, etc.)
     *  RIGHT  — "▶ Run" button | "Submit" button
     *
     * Run    → calls runSampleTests()  (quick feedback, sample cases only)
     * Submit → calls submitAllTests()  (all cases; awards points on full pass)
     */
    protected JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_PANEL);
        bar.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(10, 18, 10, 18)
        ));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(UI_FONT);
        statusLabel.setForeground(TEXT_MUTED);

        int btnH = 30;
        int runImgW = rawRunImg.getWidth(null), runImgH = rawRunImg.getHeight(null);
        int runBtnW = (runImgW > 0 && runImgH > 0)
                      ? (int)(btnH * ((double) runImgW / runImgH)) : 80;

        JButton runButton = new JButton(
            new ImageIcon(rawRunImg.getScaledInstance(runBtnW, btnH, Image.SCALE_SMOOTH))
        );
        runButton.setPreferredSize(new Dimension(runBtnW, btnH));
        runButton.setContentAreaFilled(false);
        runButton.setBorderPainted(false);
        runButton.setFocusPainted(false);
        runButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        runButton.setToolTipText("Run sample test cases only");
        runButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                runButton.setBorderPainted(true);
                runButton.setBorder(BorderFactory.createLineBorder(
                    new Color(150, 255, 150), 2));
            }
            public void mouseExited(MouseEvent e) {
                runButton.setBorderPainted(false);
            }
        });
        runButton.addActionListener(e -> runSampleTests(runButton));

        int subImgW = rawSubmitImg.getWidth(null), subImgH = rawSubmitImg.getHeight(null);
        int subBtnW = (subImgW > 0 && subImgH > 0)
                      ? (int)(btnH * ((double) subImgW / subImgH)) : 80;

        JButton submitButton = new JButton(
            new ImageIcon(rawSubmitImg.getScaledInstance(subBtnW, btnH, Image.SCALE_SMOOTH))
        );
        submitButton.setPreferredSize(new Dimension(subBtnW, btnH));
        submitButton.setContentAreaFilled(false);
        submitButton.setBorderPainted(false);
        submitButton.setFocusPainted(false);
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitButton.setToolTipText("Run all test cases and submit for grading");
        submitButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                submitButton.setBorderPainted(true);
                submitButton.setBorder(BorderFactory.createLineBorder(
                    new Color(150, 200, 255), 2));
            }
            public void mouseExited(MouseEvent e) {
                submitButton.setBorderPainted(false);
            }
        });
        submitButton.addActionListener(e -> submitAllTests(submitButton));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(runButton);
        btnPanel.add(submitButton);

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(btnPanel,    BorderLayout.EAST);
        return bar;
    }

    /**
     * Executes only the first 3 test cases (sample cases) for quick feedback.
     *
     * This lets students iterate quickly without revealing all hidden cases.
     * Results are displayed in the test-case panel.
     * Points are NOT awarded for Run — only for a successful full Submit.
     *
     * @param runButton  The button that was clicked (disabled while running).
     */
    private void runSampleTests(JButton runButton) {
        String code = codeArea.getText().trim();
        if (code.isEmpty()) {
            setStatus("⚠  Editor is empty.", ACCENT_GOLD);
            return;
        }

        runButton.setEnabled(false);
        setStatus("Running sample tests…", TEXT_MUTED);
        clearTestCasePanel();

        SwingWorker<TestResult, Void> worker = new SwingWorker<>() {

            /** Runs the judge on the full test suite (we'll truncate results after). */
            @Override
            protected TestResult doInBackground() {
                return judgeFunction().apply(code);
            }

            /** Displays only the first 3 case results (sample cases). */
            @Override
            protected void done() {
                try {
                    TestResult result = get();

                    // Compile / runtime / system errors still shown in full
                    if (result.status == TestResult.Status.COMPILE_ERROR ||
                        result.status == TestResult.Status.RUNTIME_ERROR ||
                        result.status == TestResult.Status.TIMEOUT       ||
                        result.status == TestResult.Status.SYSTEM_ERROR) {
                        displayErrorResult(result);
                        return;
                    }

                    // Show only sample cases (first 3)
                    List<CaseResult> allCases = result.cases;
                    int sampleCount = Math.min(3, allCases.size());
                    int passed      = 0;

                    for (int i = 0; i < sampleCount; i++) {
                        CaseResult cr = allCases.get(i);
                        addCaseCard(cr);
                        if (cr.passed) passed++;
                    }

                    // Hint that Submit checks more cases
                    JLabel hint = new JLabel("  ⓘ  " + sampleCount + " sample case(s) shown. "
                        + "Use Submit to check all cases.");
                    hint.setFont(UI_FONT.deriveFont(11f));
                    hint.setForeground(TEXT_MUTED);
                    hint.setBorder(new EmptyBorder(4, 2, 4, 0));
                    testCasePanel.add(hint);

                    setStatus(passed + " / " + sampleCount + " sample cases passed",
                              passed == sampleCount ? ACCENT_GREEN : ACCENT_RED);

                } catch (Exception ex) {
                    setStatus("Unexpected error.", ACCENT_RED);
                } finally {
                    runButton.setEnabled(true);
                    testCasePanel.revalidate();
                    testCasePanel.repaint();
                }
            }
        };
        worker.execute();
    }

    /**
     * Executes ALL test cases and grades the submission.
     *
     * Behaviour:
     *  - Runs every test case defined in judgeFunction().
     *  - Displays all case results in the test-case panel.
     *  - If ALL cases pass:
     *      1. Calls PlayerSession.awardPoints() — points awarded ONCE per challenge.
     *      2. Shows a success dialog offering to return to the main menu.
     *  - If any case fails, shows a failure status but does NOT award points.
     *
     * @param submitButton  The button that was clicked (disabled while running).
     */
    private void submitAllTests(JButton submitButton) {
        String code = codeArea.getText().trim();
        if (code.isEmpty()) {
            setStatus("⚠  Editor is empty.", ACCENT_GOLD);
            return;
        }

        submitButton.setEnabled(false);
        setStatus("Judging all test cases…", TEXT_MUTED);
        clearTestCasePanel();

        SwingWorker<TestResult, Void> worker = new SwingWorker<>() {

            /** Runs the full judge suite in a background thread. */
            @Override
            protected TestResult doInBackground() {
                return judgeFunction().apply(code);
            }

            /** Displays all results and handles points + navigation on full pass. */
            @Override
            protected void done() {
                try {
                    TestResult result = get();

                    // Handle error statuses first
                    if (result.status == TestResult.Status.COMPILE_ERROR ||
                        result.status == TestResult.Status.RUNTIME_ERROR ||
                        result.status == TestResult.Status.TIMEOUT       ||
                        result.status == TestResult.Status.SYSTEM_ERROR) {
                        displayErrorResult(result);
                        return;
                    }

                    // Display all case cards
                    int passed = 0, total = result.cases.size();
                    for (CaseResult cr : result.cases) {
                        addCaseCard(cr);
                        if (cr.passed) passed++;
                    }

                    if (result.allPassed()) {
                        setStatus("✔  All " + total + " test cases passed! Saving to cloud...", ACCENT_GREEN);

                        String diff = difficultyLabel();
                        String num = challengeNumber();
                        String fullChallengeId = diff + "-" + num;
                        int rewardPoints = pointsReward();

                        // Fire off the API request in a separate thread so the UI doesn't freeze
                        new Thread(() -> {
                            try {
                                SourceCode.UserInstances.ApiClient api = new SourceCode.UserInstances.ApiClient();
                                PlayerSession session = PlayerSession.getInstance();

                                // 1. Send the result to the Database
                                int officialDatabaseScore = api.submitChallengeResult(fullChallengeId, true, rewardPoints);

                                // Check if this is their first time passing BEFORE we mark it completed locally
                                boolean firstTimePass = !session.isCompleted(diff, num);

                                // 2. Update the Local Mirror (syncs leaderboard automatically)
                                session.setScore(officialDatabaseScore);
                                session.markChallengeCompleted(diff, num);

                                // 3. Update the UI Text (Must be back on the EDT)
                                SwingUtilities.invokeLater(() -> {
                                    setStatus("✔  Saved to Cloud. Score: " + officialDatabaseScore, ACCENT_GREEN);
                                    
                                    // Build a dynamic message based on whether they actually earned new points
                                    String msg;
                                    if (firstTimePass) {
                                        msg = "<html><b style='color:#3fb950;'>All tests passed!</b><br><br>"
                                            + "+" + rewardPoints + " points awarded<br>" + "<br><br>"
                                            + "Return to Main Menu?</html>";
                                    } else {
                                        msg = "<html><b style='color:#3fb950;'>All tests passed!</b><br><br>"
                                            + "You already completed this challenge.<br>"
                                            + "(No additional points awarded.)<br>" + "<br><br>"
                                            + "Return to Main Menu?</html>";
                                    }

                                    int choice = JOptionPane.showConfirmDialog(
                                        ChallengeConstructor.this,
                                        msg,
                                        "Challenge Passed!",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.INFORMATION_MESSAGE
                                    );
                                    
                                    if (choice == JOptionPane.YES_OPTION) {
                                        dispose(); // Returns to Main Menu
                                    }
                                });

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                SwingUtilities.invokeLater(() -> {
                                    setStatus("✔  Tests passed, but cloud sync failed.", ACCENT_GOLD);
                                    JOptionPane.showMessageDialog(
                                        ChallengeConstructor.this, 
                                        "Network Error: " + ex.getMessage(), 
                                        "Cloud Sync Failed", 
                                        JOptionPane.ERROR_MESSAGE
                                    );
                                });
                            }
                        }).start();

                    } else {
                        // ── Partial / full failure (Keep your existing code here) ──
                        setStatus(passed + " / " + total + " test cases passed", ACCENT_RED);
                    }

                } catch (Exception ex) {
                    setStatus("Unexpected error.", ACCENT_RED);
                } finally {
                    submitButton.setEnabled(true);
                    testCasePanel.revalidate();
                    testCasePanel.repaint();
                }
            }
        };
        worker.execute();
    }

    /**
     * Displays a compile / runtime / timeout / system-error card in the test-case panel.
     * Called by both runSampleTests and submitAllTests when the code cannot execute.
     *
     * @param result  The TestResult whose status is one of the error types.
     */
    private void displayErrorResult(TestResult result) {
        switch (result.status) {
            case COMPILE_ERROR -> { setStatus("Compilation Error",     ACCENT_RED);  addErrorCard("🔴  Compilation Error",    result.errorMessage); }
            case RUNTIME_ERROR -> { setStatus("Runtime Error",         ACCENT_RED);  addErrorCard("💥  Runtime Error",        result.errorMessage); }
            case TIMEOUT       -> { setStatus("Time Limit Exceeded",   ACCENT_GOLD); addErrorCard("⏱  Time Limit Exceeded",  result.errorMessage); }
            case SYSTEM_ERROR  -> { setStatus("System Error",          ACCENT_RED);  addErrorCard("⚙  System Error",         result.errorMessage); }
            default -> {}
        }
        testCasePanel.revalidate();
        testCasePanel.repaint();
    }

    /**
     * Adds a single test-case result card to the test-case panel.
     *
     * Each card shows:
     *  - A coloured header (✔ Passed / ✘ Failed) with a left accent border.
     *  - Input, expected output, and (on failure) the actual output.
     *
     * @param cr  The CaseResult to render.
     */
    private void addCaseCard(CaseResult cr) {
        Color  accent = cr.passed ? ACCENT_GREEN : ACCENT_RED;
        String icon   = cr.passed ? "✔" : "✘";

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(
            cr.passed ? 22 : 40,
            cr.passed ? 40 : 22,
            22
        ));
        card.setBorder(new CompoundBorder(
            new MatteBorder(0, 3, 0, 0, accent),
            new EmptyBorder(8, 10, 8, 10)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel header = new JLabel(icon + "  " + (cr.passed ? "Passed" : "Failed"));
        header.setFont(UI_BOLD.deriveFont(12f));
        header.setForeground(accent);

        // Show input + expected; on failure also show actual (wrong) output
        String detailHtml =
            "<html><span style='color:#8b949e;'>Input:</span> "    + cr.input    +
            " &nbsp;|&nbsp; <span style='color:#8b949e;'>Expected:</span> " + cr.expected +
            (!cr.passed ? " &nbsp;|&nbsp; <span style='color:#f85149;'>Got:</span> " + cr.actual : "") +
            "</html>";

        JLabel details = new JLabel(detailHtml);
        details.setFont(MONO_FONT.deriveFont(11f));
        details.setForeground(TEXT_PRIMARY);

        card.add(header);
        card.add(Box.createVerticalStrut(4));
        card.add(details);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(3, 0, 3, 0));
        wrapper.add(card);
        testCasePanel.add(wrapper);
    }

    /**
     * Adds an error card (compile / runtime / system error) to the test-case panel.
     *
     * @param title  Short error category heading (e.g. "🔴  Compilation Error").
     * @param body   The detailed error message (may contain newlines).
     */
    private void addErrorCard(String title, String body) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 22, 22));
        card.setBorder(new CompoundBorder(
            new MatteBorder(0, 3, 0, 0, ACCENT_RED),
            new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel lbl = new JLabel(
            "<html><b style='color:#f85149;'>" + title + "</b><br><br>" +
            "<span style='color:#e6edf3;font-family:monospace;font-size:11px;'>" +
            (body == null ? "" : body.replace("\n", "<br>").replace(" ", "&nbsp;")) +
            "</span></html>"
        );
        lbl.setFont(UI_FONT);
        card.add(lbl);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(3, 0, 3, 0));
        wrapper.add(card);
        testCasePanel.add(wrapper);
    }

    /**
     * Updates the line-number gutter to match the current line count in codeArea.
     * Called by the DocumentListener on every insert/remove event.
     */
    private void updateLineNumbers() {
        int lines = codeArea.getLineCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) sb.append(i).append("\n");
        lineNumbers.setText(sb.toString());
    }

    /**
     * Removes all children from the test-case panel and repaints it.
     * Called at the start of each Run or Submit action to clear old results.
     */
    private void clearTestCasePanel() {
        testCasePanel.removeAll();
        testCasePanel.revalidate();
        testCasePanel.repaint();
    }

    /**
     * Updates the bottom-bar status label with the given text and colour.
     *
     * @param text   Status message to display.
     * @param color  Foreground colour for the label.
     */
    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    /**
     * Creates a styled rounded button used throughout the challenge UI.
     *
     * The button overrides paintComponent to draw a rounded rectangle background
     * that darkens on press and brightens on hover.
     *
     * @param text  Button label.
     * @param bg    Normal background colour.
     * @param fg    Label foreground colour.
     */
    protected JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? bg.darker()   :
                            getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setFont(UI_BOLD);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(7, 16, 7, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}