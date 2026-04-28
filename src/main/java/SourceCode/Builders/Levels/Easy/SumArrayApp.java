package SourceCode.Builders.Levels.Easy;

import SourceCode.Builders.ChallengeConstructor.ChallengeConstructor;
import SourceCode.Judge.JavaJudge;
import SourceCode.Judge.JavaJudge.TestResult;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 * Challenge 5 (Easy) — Sum of Array
 *
 * The student must implement:
 *   public static String solve(int[] arr)
 * returning String.valueOf(sum of all elements).
 * All sums are guaranteed to fit within a standard int.
 *
 * Points awarded: 1 (first-time completion only).
 */
public class SumArrayApp extends ChallengeConstructor {

    // challengeNumber | line 21 | Returns "5" — used as part of the unique challenge key for point tracking
    @Override protected String challengeNumber() { return "5"; }

    // challengeTitle | line 24 | Returns the human-readable challenge name shown in the UI
    @Override protected String challengeTitle()  { return "Sum of Array"; }

    // difficultyLabel | line 27 | Returns "EASY" — controls points key and badge colour
    @Override protected String difficultyLabel() { return "EASY"; }

    // difficultyColor | line 30 | Returns the green accent colour used for the EASY badge
    @Override protected Color  difficultyColor() { return ACCENT_GREEN; }

    // pointsReward | line 33 | Returns 1 — points awarded on first successful submission
    @Override protected int    pointsReward()    { return 1; }

    // SumArrayApp constructor | line 36 | Passes the parent frame to ChallengeConstructor to build the challenge UI
    public SumArrayApp(JFrame parent, Runnable onReturn) {
        super(parent, onReturn);
    }

    // problemHtml | line 41 | Returns the styled HTML problem description shown in the left panel
    @Override
    protected String problemHtml() {
        return "<html><body style='font-family:Segoe UI;font-size:12px;"
             + "color:#e6edf3;background:#161b22;padding:14px;'>"
             + "<p style='color:#8b949e;font-size:11px;'>DESCRIPTION</p>"
             + "<h3 style='color:#e6edf3;margin-top:4px;'>Sum of Array</h3>"
             + "<p>Given an integer array <code style='background:#30363d;padding:1px 5px;"
             + "border-radius:3px;'>arr</code>, return the <b>sum</b> of all its elements "
             + "as a String.</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EXAMPLES</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> [1,2,3,4,5] &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"15\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> [10,20,30] &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"60\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> [-5,5] &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"0\"</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EDGE CASES</p>"
             + "<p>Works with negative numbers and single-element arrays.<br>"
             + "All sums fit within a standard <code style='background:#30363d;"
             + "padding:1px 5px;border-radius:3px;'>int</code>.</p>"
             + "</body></html>";
    }

    // starterCode | line 68 | Returns the pre-filled code skeleton shown in the editor when the challenge opens
    @Override
    protected String starterCode() {
        return "public static String solve(int[] arr) {\n"
             + "    // Return the sum of all elements in arr as a String\n"
             + "    \n"
             + "}";
    }

    // judgeFunction | line 76 | Returns a reference to JavaJudge::judgeSumArray to evaluate the student's code
    @Override
    protected Function<String, TestResult> judgeFunction() {
        return JavaJudge::judgeSumArray;
    }
}
