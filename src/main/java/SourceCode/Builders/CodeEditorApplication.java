package SourceCode.Builders;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * CodeEditorApplication
 *
 * A JPanel-based Java code editor with:
 *  - Left pane  : editable input with basic syntax highlighting
 *  - Right pane : read-only output console
 *  - Run button : compiles & runs via system JDK (javac + java)
 *  - Back button: navigates back via a Runnable callback
 */
public class CodeEditorApplication extends JPanel {

    // ── Palette ────────────────────────────────────────────────────────────────
    private static final Color  BG_DARK       = new Color(30,  30,  30),
                                BG_PANEL      = new Color(40,  40,  40),
                                BG_EDITOR     = new Color(22,  22,  30),
                                BG_CONSOLE    = new Color(18,  18,  18),
                                FG_DEFAULT    = new Color(212, 212, 212),
                                FG_KEYWORD    = new Color(86,  156, 214), // blue
                                FG_STRING     = new Color(206, 145, 120),   // orange-brown
                                FG_COMMENT    = new Color(106, 153,  85),   // green
                                FG_NUMBER     = new Color(181, 206, 168),   // light green
                                FG_TYPE       = new Color(78,  201, 176),   // teal
                                FG_ANNOTATION = new Color(220, 220, 170),   // yellow
                                FG_CONSOLE_TX = new Color(180, 230, 180),   // console green
                                FG_ERROR      = new Color(244, 135, 113),   // error red
                                DIVIDER       = new Color(60,  60,  70);

    // ── Keywords ───────────────────────────────────────────────────────────────
    private static final String[] KEYWORDS = {
        "abstract","assert","boolean","break","byte","case","catch","char",
        "class","const","continue","default","do","double","else","enum",
        "extends","final","finally","float","for","goto","if","implements",
        "import","instanceof","int","interface","long","native","new",
        "package","private","protected","public","return","short","static",
        "strictfp","super","switch","synchronized","this","throw","throws",
        "transient","try","void","volatile","while","true","false","null",
        "var","record","sealed","permits","yield"
    };

    private static final String[] TYPES = {
        "String","Integer","Double","Float","Long","Short","Byte","Boolean",
        "Character","Object","System","Math","ArrayList","HashMap","HashSet",
        "List","Map","Set","Optional","Scanner","StringBuilder","PrintStream",
        "Exception","RuntimeException","Thread","Runnable"
    };

    // ── Fields ─────────────────────────────────────────────────────────────────
    private JTextPane  editorPane;
    private JTextArea  consoleArea;
    private StyledDocument editorDoc;
    private boolean    isHighlighting = false;

    // ── Raw button images ──────────────────────────────────────────────────────
    private Image rawBackImg, rawRunImg;

    // ── Constructor ────────────────────────────────────────────────────────────
    public CodeEditorApplication(Runnable onBack) {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_DARK);

        // Pre-load button images
        rawBackImg = new ImageIcon(getClass().getResource("/Buttons/BackButton.png")).getImage();
        rawRunImg  = new ImageIcon(getClass().getResource("/Buttons/RunButton.png")).getImage();

        // ── Top bar ────────────────────────────────────────────────────────────
        JPanel topBar = buildTopBar(onBack);
        add(topBar, BorderLayout.NORTH);

        // ── Split editor / console ─────────────────────────────────────────────
        JSplitPane split = buildSplitPane();   // sets editorPane & consoleArea
        add(split, BorderLayout.CENTER);

        editorDoc = editorPane.getStyledDocument();

        // Seed with a starter template
        SwingUtilities.invokeLater(() -> {
            editorPane.setText(defaultTemplate());
            rehighlight();
        });
    }

    // ── Top bar ────────────────────────────────────────────────────────────────
    private JPanel buildTopBar(Runnable onBack) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER));
        bar.setPreferredSize(new Dimension(0, 50));

        // Left: Image-based Back button
        int backBtnH = 30;
        int bkImgW = rawBackImg.getWidth(null), bkImgH = rawBackImg.getHeight(null);
        int backBtnW = (bkImgW > 0 && bkImgH > 0)
                       ? (int)(backBtnH * ((double) bkImgW / bkImgH)) : 90;

        JButton backBtn = new JButton(
            new ImageIcon(rawBackImg.getScaledInstance(backBtnW, backBtnH, Image.SCALE_SMOOTH))
        );
        backBtn.setPreferredSize(new Dimension(backBtnW, backBtnH));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setToolTipText("Back");
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
        backBtn.addActionListener(e -> onBack.run());

        JPanel leftWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        leftWrap.setOpaque(false);
        leftWrap.add(backBtn);
        bar.add(leftWrap, BorderLayout.WEST);

        // Center: Title
        JLabel title = new JLabel("Java Code Editor", SwingConstants.CENTER);
        title.setForeground(FG_DEFAULT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bar.add(title, BorderLayout.CENTER);

        // Right: Image-based Run button
        int runBtnH = 30;
        int rnImgW = rawRunImg.getWidth(null), rnImgH = rawRunImg.getHeight(null);
        int runBtnW = (rnImgW > 0 && rnImgH > 0)
                      ? (int)(runBtnH * ((double) rnImgW / rnImgH)) : 80;

        JButton runBtn = new JButton(
            new ImageIcon(rawRunImg.getScaledInstance(runBtnW, runBtnH, Image.SCALE_SMOOTH))
        );
        runBtn.setPreferredSize(new Dimension(runBtnW, runBtnH));
        runBtn.setContentAreaFilled(false);
        runBtn.setBorderPainted(false);
        runBtn.setFocusPainted(false);
        runBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        runBtn.setToolTipText("Run code");
        runBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                runBtn.setBorderPainted(true);
                runBtn.setBorder(BorderFactory.createLineBorder(
                    new Color(150, 255, 150), 2));
            }
            public void mouseExited(MouseEvent e) {
                runBtn.setBorderPainted(false);
            }
        });
        runBtn.addActionListener(e -> runCode());

        JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        rightWrap.setOpaque(false);
        rightWrap.add(runBtn);
        bar.add(rightWrap, BorderLayout.EAST);

        return bar;
    }

    // ── Split pane ─────────────────────────────────────────────────────────────
    private JSplitPane buildSplitPane() {
        // ── Editor (left) ──────────────────────────────────────────────────────
        editorPane = new JTextPane();
        editorPane.setBackground(BG_EDITOR);
        editorPane.setForeground(FG_DEFAULT);
        editorPane.setCaretColor(Color.WHITE);
        editorPane.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        editorPane.setMargin(new Insets(10, 14, 10, 10));
        editorPane.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { scheduleHighlight(); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { scheduleHighlight(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { }
        });

        JScrollPane editorScroll = darkScroll(editorPane);
        editorScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel editorWrapper = new JPanel(new BorderLayout());
        editorWrapper.setBackground(BG_DARK);
        editorWrapper.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 4));
        editorWrapper.add(editorScroll, BorderLayout.CENTER);

        // Label
        JLabel editorLabel = panelLabel("  Code Input");
        editorWrapper.add(editorLabel, BorderLayout.NORTH);

        // ── Console (right) ────────────────────────────────────────────────────
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setBackground(BG_CONSOLE);
        consoleArea.setForeground(FG_CONSOLE_TX);
        consoleArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        consoleArea.setMargin(new Insets(10, 14, 10, 10));
        consoleArea.setLineWrap(true);
        consoleArea.setWrapStyleWord(true);

        JScrollPane consoleScroll = darkScroll(consoleArea);
        consoleScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel consoleWrapper = new JPanel(new BorderLayout());
        consoleWrapper.setBackground(BG_DARK);
        consoleWrapper.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 8));
        consoleWrapper.add(panelLabel("  Output Console"), BorderLayout.NORTH);
        consoleWrapper.add(consoleScroll, BorderLayout.CENTER);

        // ── Split ──────────────────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorWrapper, consoleWrapper);
        split.setResizeWeight(0.5);
        split.setDividerSize(6);
        split.setBackground(DIVIDER);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setDividerLocation(0.5);

        // Style the divider
        split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, e ->
            split.getComponent(0).setBackground(DIVIDER));

        return split;
    }

    // ── Compile & Run ──────────────────────────────────────────────────────────
    private void runCode() {
        String source = editorPane.getText().trim();
        if (source.isEmpty()) {
            appendToConsole("// No code to run.\n", false);
            return;
        }

        consoleArea.setText("");
        appendToConsole("Compiling...\n", false);

        // Extract public class name
        String className = extractClassName(source);
        if (className == null) {
            appendToConsole("Error: Could not find a public class declaration.\n", true);
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Path tmpDir = Files.createTempDirectory("code_editor_");
                File srcFile = tmpDir.resolve(className + ".java").toFile();

                try {
                    Files.writeString(srcFile.toPath(), source);

                    // ── Compile ────────────────────────────────────────────────
                    ProcessBuilder compileCmd = new ProcessBuilder("javac", srcFile.getAbsolutePath());
                    compileCmd.redirectErrorStream(true);
                    compileCmd.directory(tmpDir.toFile());
                    Process compileProc = compileCmd.start();

                    String compileOut = new String(compileProc.getInputStream().readAllBytes());
                    int compileExit   = compileProc.waitFor();

                    if (compileExit != 0) {
                        SwingUtilities.invokeLater(() -> {
                            appendToConsole("Compilation failed:\n\n", true);
                            appendToConsole(compileOut, true);
                        });
                        return null;
                    }

                    SwingUtilities.invokeLater(() -> appendToConsole("Compiled successfully. Running...\n\n", false));

                    // ── Run ────────────────────────────────────────────────────
                    ProcessBuilder runCmd = new ProcessBuilder("java", "-cp", tmpDir.toString(), className);
                    runCmd.redirectErrorStream(true);
                    runCmd.directory(tmpDir.toFile());
                    Process runProc = runCmd.start();

                    // Stream output in real time
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(runProc.getInputStream()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            final String out = line + "\n";
                            SwingUtilities.invokeLater(() -> appendToConsole(out, false));
                        }
                    }

                    int runExit = runProc.waitFor();
                    SwingUtilities.invokeLater(() ->
                        appendToConsole("\nProcess exited with code " + runExit + "\n", runExit != 0));

                } finally {
                    // Cleanup temp files
                    try (var walk = Files.walk(tmpDir)) {
                        walk.sorted(java.util.Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                    }
                }
                return null;
            }
        };
        worker.execute();
    }

    // ── Console helpers ────────────────────────────────────────────────────────
    private void appendToConsole(String text, boolean isError) {
        consoleArea.setForeground(isError ? FG_ERROR : FG_CONSOLE_TX);
        consoleArea.append(text);
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }

    // ── Syntax Highlighting ────────────────────────────────────────────────────
    private Timer highlightTimer;

    private void scheduleHighlight() {
        if (highlightTimer != null && highlightTimer.isRunning()) highlightTimer.stop();
        highlightTimer = new Timer(300, e -> rehighlight());
        highlightTimer.setRepeats(false);
        highlightTimer.start();
    }

    private void rehighlight() {
        if (isHighlighting) return;
        isHighlighting = true;

        SwingUtilities.invokeLater(() -> {
            try {
                int caretPos = editorPane.getCaretPosition();
                String text  = editorDoc.getText(0, editorDoc.getLength());

                // Reset all to default
                StyleContext sc = StyleContext.getDefaultStyleContext();
                AttributeSet def = sc.addAttribute(
                    SimpleAttributeSet.EMPTY, StyleConstants.Foreground, FG_DEFAULT);
                editorDoc.setCharacterAttributes(0, text.length(), def, true);

                // Apply each rule in order
                highlightPattern(text, "//(.*?)(\n|$)",          FG_COMMENT,    false);
                highlightPattern(text, "/\\*(.|\\n)*?\\*/",       FG_COMMENT,    false);
                highlightPattern(text, "\"(\\\\.|[^\"])*\"",      FG_STRING,     false);
                highlightPattern(text, "'(\\\\.|[^'])*'",         FG_STRING,     false);
                highlightPattern(text, "\\b\\d+(\\.\\d+)?[fFdDlL]?\\b", FG_NUMBER, false);
                highlightPattern(text, "@\\w+",                   FG_ANNOTATION, false);
                highlightWords(text, TYPES,    FG_TYPE);
                highlightWords(text, KEYWORDS, FG_KEYWORD);

                // Restore caret
                if (caretPos <= editorDoc.getLength())
                    editorPane.setCaretPosition(caretPos);

            } catch (BadLocationException ignored) {
            } finally {
                isHighlighting = false;
            }
        });
    }

    private void highlightPattern(String text, String regex, Color color, boolean bold) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher m = p.matcher(text);
        StyleContext sc = StyleContext.getDefaultStyleContext();
        while (m.find()) {
            AttributeSet as = sc.addAttribute(SimpleAttributeSet.EMPTY,
                StyleConstants.Foreground, color);
            if (bold) as = sc.addAttribute(as, StyleConstants.Bold, true);
            editorDoc.setCharacterAttributes(m.start(), m.end() - m.start(), as, false);
        }
    }

    private void highlightWords(String text, String[] words, Color color) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet as = sc.addAttribute(SimpleAttributeSet.EMPTY,
            StyleConstants.Foreground, color);
        for (String word : words) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\\b" + java.util.regex.Pattern.quote(word) + "\\b");
            java.util.regex.Matcher m = p.matcher(text);
            while (m.find()) {
                editorDoc.setCharacterAttributes(m.start(), m.end() - m.start(), as, false);
            }
        }
    }

    // ── Utilities ──────────────────────────────────────────────────────────────
    private String extractClassName(String source) {
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("public\\s+class\\s+(\\w+)")
            .matcher(source);
        return m.find() ? m.group(1) : null;
    }

    private JScrollPane darkScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new DarkScrollBarUI());
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        sp.getHorizontalScrollBar().setBackground(BG_DARK);
        sp.setBorder(BorderFactory.createLineBorder(DIVIDER, 1));
        return sp;
    }

    private JLabel panelLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(140, 140, 160));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private String defaultTemplate() {
        return
            "public class Main {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Hello, World!\");\n" +
            "    }\n" +
            "}\n";
    }

    // ── Dark Scrollbar UI ──────────────────────────────────────────────────────
    private static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        private static final Color THUMB = new Color(80, 80, 100);
        private static final Color TRACK = new Color(30, 30, 30);

        @Override protected void configureScrollBarColors() {
            thumbColor       = THUMB;
            trackColor       = TRACK;
        }
        @Override protected JButton createDecreaseButton(int o) { return invisibleBtn(); }
        @Override protected JButton createIncreaseButton(int o) { return invisibleBtn(); }
        private JButton invisibleBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isDragging ? THUMB.brighter() : THUMB);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(TRACK);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}