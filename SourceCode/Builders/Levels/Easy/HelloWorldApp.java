package SourceCode.Builders.Levels.Easy;

import SourceCode.Builders.ChallengeConstructor.ChallengeConstructor;
import SourceCode.Judge.JavaJudge;
import SourceCode.Judge.JavaJudge.TestResult;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 * Challenge 1 (Easy) — Print Hello World
 *
 * The student must implement:
 *   public static String solve()
 * returning exactly the String "Hello World".
 *
 * Points awarded: 1 (first-time completion only).
 */
public class HelloWorldApp extends ChallengeConstructor {

    // challengeNumber | line 20 | Returns "1" — used as part of the unique challenge key for point tracking
    @Override protected String challengeNumber() { return "1"; }

    // challengeTitle | line 23 | Returns the human-readable challenge name shown in the UI
    @Override protected String challengeTitle()  { return "Print Hello World"; }

    // difficultyLabel | line 26 | Returns "EASY" — controls points key and badge colour
    @Override protected String difficultyLabel() { return "EASY"; }

    // difficultyColor | line 29 | Returns the green accent colour used for the EASY badge
    @Override protected Color  difficultyColor() { return ACCENT_GREEN; }

    // pointsReward | line 32 | Returns 1 — points awarded on first successful submission
    @Override protected int    pointsReward()    { return 1; }

    // HelloWorldApp constructor | line 35 | Passes the parent frame to ChallengeConstructor to build the challenge UI
    public HelloWorldApp(JFrame parent, Runnable onReturn) {
        super(parent, onReturn);
    }

    // problemHtml | line 40 | Returns the styled HTML problem description shown in the left panel
    @Override
    protected String problemHtml() {
        return "<html><body style='font-family:Segoe UI;font-size:12px;"
             + "color:#e6edf3;background:#161b22;padding:14px;'>"
             + "<p style='color:#8b949e;font-size:11px;'>DESCRIPTION</p>"
             + "<h3 style='color:#e6edf3;margin-top:4px;'>Print Hello World</h3>"
             + "<p>Implement the method <code style='background:#30363d;padding:1px 5px;"
             + "border-radius:3px;'>solve()</code> so that it returns the string "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>"
             + "\"Hello World\"</code>.</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EXAMPLE</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> (none)</p>"
             + "<p><b style='color:#3fb950;'>Output:</b> \"Hello World\"</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>NOTE</p>"
             + "<p>Exact capitalisation and spacing matter.<br>"
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>"
             + "\"hello world\"</code> or <code style='background:#30363d;padding:1px 5px;"
             + "border-radius:3px;'>\"HelloWorld\"</code> will not pass.</p>"
             + "</body></html>";
    }

    // starterCode | line 62 | Returns the pre-filled code skeleton shown in the editor when the challenge opens
    @Override
    protected String starterCode() {
        return "public static String solve() {\n"
             + "    // Return the string \"Hello World\"\n"
             + "    \n"
             + "}";
    }

    // judgeFunction | line 70 | Returns a reference to JavaJudge::judgeHelloWorld to evaluate the student's code
    @Override
    protected Function<String, TestResult> judgeFunction() {
        return JavaJudge::judgeHelloWorld;
    }
}
