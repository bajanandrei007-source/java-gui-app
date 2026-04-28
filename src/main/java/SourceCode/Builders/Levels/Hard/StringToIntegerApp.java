package SourceCode.Builders.Levels.Hard;

import SourceCode.Builders.ChallengeConstructor.ChallengeConstructor;
import SourceCode.Judge.JavaJudge;
import SourceCode.Judge.JavaJudge.TestResult;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 * Challenge 2 (Hard) — String to Integer (Words to Number)
 *
 * The student must implement:
 *   public static String solve(String words)
 * parsing English number words and returning the integer value as a String.
 * Examples: "Thirty Three" → "33", "One Hundred Five" → "105"
 * Supports ones, teens, tens, hundreds, and thousands up to 999,999.
 *
 * Points awarded: 5 (first-time completion only).
 */
public class StringToIntegerApp extends ChallengeConstructor {

    // challengeNumber | line 22 | Returns "2" — used as part of the unique challenge key for point tracking
    @Override protected String challengeNumber() { return "2"; }

    // challengeTitle | line 25 | Returns the human-readable challenge name shown in the UI
    @Override protected String challengeTitle()  { return "String to Integer"; }

    // difficultyLabel | line 28 | Returns "HARD" — controls points key and badge colour
    @Override protected String difficultyLabel() { return "HARD"; }

    // difficultyColor | line 31 | Returns the red accent colour used for the HARD badge
    @Override protected Color  difficultyColor() { return ACCENT_RED; }

    // pointsReward | line 34 | Returns 5 — points awarded on first successful submission
    @Override protected int    pointsReward()    { return 5; }

    // StringToIntegerApp constructor | line 37 | Passes the parent frame to ChallengeConstructor to build the challenge UI
    public StringToIntegerApp(JFrame parent, Runnable onReturn) {
        super(parent, onReturn);
    }

    // problemHtml | line 42 | Returns the styled HTML problem description shown in the left panel
    @Override
    protected String problemHtml() {
        return "<html><body style='font-family:Segoe UI;font-size:12px;"
             + "color:#e6edf3;background:#161b22;padding:14px;'>"
             + "<p style='color:#8b949e;font-size:11px;'>DESCRIPTION</p>"
             + "<h3 style='color:#e6edf3;margin-top:4px;'>String to Integer</h3>"
             + "<p>Given a string of English number words, convert it to its integer value and "
             + "return it as a String. The input uses standard English number naming "
             + "(ones, teens, tens, hundreds, thousands). Words are space-separated and may be "
             + "mixed case.</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EXAMPLES</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> \"Thirty Three\" &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"33\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> \"One Hundred Five\" &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"105\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> \"Two Thousand Four Hundred Twelve\" &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"2412\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> \"Twenty\" &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"20\"</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>CONSTRAINTS</p>"
             + "<p>Input is always a valid English number phrase &nbsp;|&nbsp; "
             + "0 &le; result &le; 999,999 &nbsp;|&nbsp; Return the integer value as a String</p>"
             + "</body></html>";
    }

    // starterCode | line 72 | Returns the pre-filled code skeleton shown in the editor when the challenge opens
    @Override
    protected String starterCode() {
        return "public static String solve(String words) {\n"
             + "    // Convert English number words to an integer, return as String\n"
             + "    // e.g. \"Thirty Three\"                    -> \"33\"\n"
             + "    // e.g. \"One Hundred Five\"                -> \"105\"\n"
             + "    // e.g. \"Two Thousand Four Hundred Twelve\" -> \"2412\"\n"
             + "    \n"
             + "}";
    }

    // judgeFunction | line 83 | Returns a reference to JavaJudge::judgeStringToInteger to evaluate the student's code
    @Override
    protected Function<String, TestResult> judgeFunction() {
        return JavaJudge::judgeStringToInteger;
    }
}
