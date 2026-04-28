package SourceCode.Builders.Levels.Easy;

import SourceCode.Builders.ChallengeConstructor.ChallengeConstructor;
import SourceCode.Judge.JavaJudge;
import SourceCode.Judge.JavaJudge.TestResult;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 * Challenge 2 (Easy) — Even or Odd Checker
 *
 * The student must implement:
 *   public static String solve(int n)
 * returning "Even" if n is even, or "Odd" if n is odd.
 * Works correctly for negative numbers and zero.
 *
 * Points awarded: 1 (first-time completion only).
 */
public class EvenOddApp extends ChallengeConstructor {

    // challengeNumber | line 21 | Returns "2" — used as part of the unique challenge key for point tracking
    @Override protected String challengeNumber() { return "2"; }

    // challengeTitle | line 24 | Returns the human-readable challenge name shown in the UI
    @Override protected String challengeTitle()  { return "Even or Odd Checker"; }

    // difficultyLabel | line 27 | Returns "EASY" — controls points key and badge colour
    @Override protected String difficultyLabel() { return "EASY"; }

    // difficultyColor | line 30 | Returns the green accent colour used for the EASY badge
    @Override protected Color  difficultyColor() { return ACCENT_GREEN; }

    // pointsReward | line 33 | Returns 1 — points awarded on first successful submission
    @Override protected int    pointsReward()    { return 1; }

    // EvenOddApp constructor | line 36 | Passes the parent frame to ChallengeConstructor to build the challenge UI
    public EvenOddApp(JFrame parent, Runnable onReturn) {
        super(parent, onReturn);
    }

    // problemHtml | line 41 | Returns the styled HTML problem description shown in the left panel
    @Override
    protected String problemHtml() {
        return "<html><body style='font-family:Segoe UI;font-size:12px;"
             + "color:#e6edf3;background:#161b22;padding:14px;'>"
             + "<p style='color:#8b949e;font-size:11px;'>DESCRIPTION</p>"
             + "<h3 style='color:#e6edf3;margin-top:4px;'>Even or Odd Checker</h3>"
             + "<p>Given an integer <code style='background:#30363d;padding:1px 5px;"
             + "border-radius:3px;'>n</code>, return "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>\"Even\"</code>"
             + " if it is even, or "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>\"Odd\"</code>"
             + " if it is odd.</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EXAMPLES</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> n=4 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"Even\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> n=7 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"Odd\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> n=0 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"Even\"</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>CONSTRAINTS</p>"
             + "<p>Works with negative numbers and zero.</p>"
             + "</body></html>";
    }

    // starterCode | line 67 | Returns the pre-filled code skeleton shown in the editor when the challenge opens
    @Override
    protected String starterCode() {
        return "public static String solve(int n) {\n"
             + "    // Return \"Even\" if n is even, \"Odd\" if n is odd\n"
             + "    \n"
             + "}";
    }

    // judgeFunction | line 75 | Returns a reference to JavaJudge::judgeEvenOdd to evaluate the student's code
    @Override
    protected Function<String, TestResult> judgeFunction() {
        return JavaJudge::judgeEvenOdd;
    }
}
