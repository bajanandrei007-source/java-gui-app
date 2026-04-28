package SourceCode.Builders.Levels.Easy;

import SourceCode.Builders.ChallengeConstructor.ChallengeConstructor;
import SourceCode.Judge.JavaJudge;
import SourceCode.Judge.JavaJudge.TestResult;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 * Challenge 3 (Easy) — Largest of Two Numbers
 *
 * The student must implement:
 *   public static String solve(int a, int b)
 * returning String.valueOf(Math.max(a, b)).
 * If a and b are equal, either value is accepted.
 *
 * Points awarded: 1 (first-time completion only).
 */
public class LargestNumberApp extends ChallengeConstructor {

    // challengeNumber | line 21 | Returns "3" — used as part of the unique challenge key for point tracking
    @Override protected String challengeNumber() { return "3"; }

    // challengeTitle | line 24 | Returns the human-readable challenge name shown in the UI
    @Override protected String challengeTitle()  { return "Largest of Two Numbers"; }

    // difficultyLabel | line 27 | Returns "EASY" — controls points key and badge colour
    @Override protected String difficultyLabel() { return "EASY"; }

    // difficultyColor | line 30 | Returns the green accent colour used for the EASY badge
    @Override protected Color  difficultyColor() { return ACCENT_GREEN; }

    // pointsReward | line 33 | Returns 1 — points awarded on first successful submission
    @Override protected int    pointsReward()    { return 1; }

    // LargestNumberApp constructor | line 36 | Passes the parent frame to ChallengeConstructor to build the challenge UI
    public LargestNumberApp(JFrame parent, Runnable onReturn) {
        super(parent, onReturn);
    }

    // problemHtml | line 41 | Returns the styled HTML problem description shown in the left panel
    @Override
    protected String problemHtml() {
        return "<html><body style='font-family:Segoe UI;font-size:12px;"
             + "color:#e6edf3;background:#161b22;padding:14px;'>"
             + "<p style='color:#8b949e;font-size:11px;'>DESCRIPTION</p>"
             + "<h3 style='color:#e6edf3;margin-top:4px;'>Largest of Two Numbers</h3>"
             + "<p>Given two integers <code style='background:#30363d;padding:1px 5px;"
             + "border-radius:3px;'>a</code> and "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>b</code>,"
             + " return the <b>larger</b> of the two as a String.</p>"
             + "<p>If they are equal, return either value.</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EXAMPLES</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> a=3, b=7 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"7\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> a=10, b=2 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"10\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> a=5, b=5 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"5\"</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>CONSTRAINTS</p>"
             + "<p>Works with negative numbers and <code style='background:#30363d;"
             + "padding:1px 5px;border-radius:3px;'>Integer.MAX_VALUE</code>.</p>"
             + "</body></html>";
    }

    // starterCode | line 67 | Returns the pre-filled code skeleton shown in the editor when the challenge opens
    @Override
    protected String starterCode() {
        return "public static String solve(int a, int b) {\n"
             + "    // Return the larger of a and b as a String\n"
             + "    \n"
             + "}";
    }

    // judgeFunction | line 75 | Returns a reference to JavaJudge::judgeLargestNumber to evaluate the student's code
    @Override
    protected Function<String, TestResult> judgeFunction() {
        return JavaJudge::judgeLargestNumber;
    }
}
