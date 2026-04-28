package SourceCode.Builders.Levels.Medium;

import SourceCode.Builders.ChallengeConstructor.ChallengeConstructor;
import SourceCode.Judge.JavaJudge;
import SourceCode.Judge.JavaJudge.TestResult;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 * Challenge 1 (Medium) — Caesar Cipher
 *
 * The student must implement:
 *   public static String solve(String text, int shift)
 * shifting each letter forward by 'shift' positions in the alphabet, wrapping at Z/z.
 * Non-letter characters remain unchanged; case is preserved.
 *
 * Points awarded: 3 (first-time completion only).
 */
public class CaesarCipherApp extends ChallengeConstructor {

    // challengeNumber | line 21 | Returns "1" — used as part of the unique challenge key for point tracking
    @Override protected String challengeNumber() { return "1"; }

    // challengeTitle | line 24 | Returns the human-readable challenge name shown in the UI
    @Override protected String challengeTitle()  { return "Caesar Cipher"; }

    // difficultyLabel | line 27 | Returns "MEDIUM" — controls points key and badge colour
    @Override protected String difficultyLabel() { return "MEDIUM"; }

    // difficultyColor | line 30 | Returns the gold accent colour used for the MEDIUM badge
    @Override protected Color  difficultyColor() { return ACCENT_GOLD; }

    // pointsReward | line 33 | Returns 3 — points awarded on first successful submission
    @Override protected int    pointsReward()    { return 3; }

    // CaesarCipherApp constructor | line 36 | Passes the parent frame to ChallengeConstructor to build the challenge UI
    public CaesarCipherApp(JFrame parent, Runnable onReturn) {
        super(parent, onReturn);
    }

    // problemHtml | line 41 | Returns the styled HTML problem description shown in the left panel
    @Override
    protected String problemHtml() {
        return "<html><body style='font-family:Segoe UI;font-size:12px;"
             + "color:#e6edf3;background:#161b22;padding:14px;'>"
             + "<p style='color:#8b949e;font-size:11px;'>DESCRIPTION</p>"
             + "<h3 style='color:#e6edf3;margin-top:4px;'>Caesar Cipher</h3>"
             + "<p>Given a string <code style='background:#30363d;padding:1px 5px;"
             + "border-radius:3px;'>text</code> and an integer "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>shift</code>, "
             + "encrypt the text using a Caesar cipher. Shift each letter forward by "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>shift</code> "
             + "positions in the alphabet, wrapping around at Z. "
             + "Non-letter characters remain unchanged. Preserve case.</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EXAMPLES</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> text=\"Hello\", shift=3 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"Khoor\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> text=\"xyz\", shift=3 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"abc\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> text=\"Hello, World!\", shift=13 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"Uryyb, Jbeyq!\"</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>CONSTRAINTS</p>"
             + "<p>0 &le; shift &le; 25 &nbsp;|&nbsp; Non-letters stay unchanged &nbsp;|&nbsp; Preserve case</p>"
             + "</body></html>";
    }

    // starterCode | line 69 | Returns the pre-filled code skeleton shown in the editor when the challenge opens
    @Override
    protected String starterCode() {
        return "public static String solve(String text, int shift) {\n"
             + "    // Shift each letter by 'shift' positions, wrap around Z/z\n"
             + "    // Non-letter characters remain unchanged\n"
             + "    \n"
             + "}";
    }

    // judgeFunction | line 78 | Returns a reference to JavaJudge::judgeCaesarCipher to evaluate the student's code
    @Override
    protected Function<String, TestResult> judgeFunction() {
        return JavaJudge::judgeCaesarCipher;
    }
}
