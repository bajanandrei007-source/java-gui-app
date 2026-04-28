package SourceCode.Builders.Levels.Easy;

import SourceCode.Builders.ChallengeConstructor.ChallengeConstructor;
import SourceCode.Judge.JavaJudge;
import SourceCode.Judge.JavaJudge.TestResult;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 * Challenge 4 (Easy) — Reverse a String
 *
 * The student must implement:
 *   public static String solve(String s)
 * returning the characters of s in reverse order.
 * An empty string returns ""; a single character returns itself.
 *
 * Points awarded: 1 (first-time completion only).
 */
public class ReverseStringApp extends ChallengeConstructor {

    // challengeNumber | line 21 | Returns "4" — used as part of the unique challenge key for point tracking
    @Override protected String challengeNumber() { return "4"; }

    // challengeTitle | line 24 | Returns the human-readable challenge name shown in the UI
    @Override protected String challengeTitle()  { return "Reverse a String"; }

    // difficultyLabel | line 27 | Returns "EASY" — controls points key and badge colour
    @Override protected String difficultyLabel() { return "EASY"; }

    // difficultyColor | line 30 | Returns the green accent colour used for the EASY badge
    @Override protected Color  difficultyColor() { return ACCENT_GREEN; }

    // pointsReward | line 33 | Returns 1 — points awarded on first successful submission
    @Override protected int    pointsReward()    { return 1; }

    // ReverseStringApp constructor | line 36 | Passes the parent frame to ChallengeConstructor to build the challenge UI
    public ReverseStringApp(JFrame parent, Runnable onReturn) {
        super(parent, onReturn);
    }

    // problemHtml | line 41 | Returns the styled HTML problem description shown in the left panel
    @Override
    protected String problemHtml() {
        return "<html><body style='font-family:Segoe UI;font-size:12px;"
             + "color:#e6edf3;background:#161b22;padding:14px;'>"
             + "<p style='color:#8b949e;font-size:11px;'>DESCRIPTION</p>"
             + "<h3 style='color:#e6edf3;margin-top:4px;'>Reverse a String</h3>"
             + "<p>Given a string <code style='background:#30363d;padding:1px 5px;"
             + "border-radius:3px;'>s</code>, return the string with its characters "
             + "in <b>reverse order</b>.</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EXAMPLES</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> \"hello\" &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"olleh\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> \"Java\" &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"avaJ\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> \"racecar\" &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"racecar\"</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EDGE CASES</p>"
             + "<p>An empty string <code style='background:#30363d;padding:1px 5px;"
             + "border-radius:3px;'>\"\"</code> should return "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>"
             + "\"\"</code>.<br>A single character returns itself.</p>"
             + "</body></html>";
    }

    // starterCode | line 70 | Returns the pre-filled code skeleton shown in the editor when the challenge opens
    @Override
    protected String starterCode() {
        return "public static String solve(String s) {\n"
             + "    // Return the reverse of s\n"
             + "    \n"
             + "}";
    }

    // judgeFunction | line 78 | Returns a reference to JavaJudge::judgeReverseString to evaluate the student's code
    @Override
    protected Function<String, TestResult> judgeFunction() {
        return JavaJudge::judgeReverseString;
    }
}
