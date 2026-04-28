package SourceCode.Builders.Levels.Medium;

import SourceCode.Builders.ChallengeConstructor.ChallengeConstructor;
import SourceCode.Judge.JavaJudge;
import SourceCode.Judge.JavaJudge.TestResult;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 * Challenge 3 (Medium) — Pascal's Triangle
 *
 * The student must implement:
 *   public static List&lt;List&lt;Integer&gt;&gt; solve(int numRows)
 * returning the first numRows rows of Pascal's triangle.
 * Edge values are 1; each interior value is the sum of the two values above it.
 *
 * Points awarded: 3 (first-time completion only).
 */
public class PascalTriangleApp extends ChallengeConstructor {

    // challengeNumber | line 21 | Returns "3" — used as part of the unique challenge key for point tracking
    @Override protected String challengeNumber() { return "3"; }

    // challengeTitle | line 24 | Returns the human-readable challenge name shown in the UI
    @Override protected String challengeTitle()  { return "Pascal's Triangle"; }

    // difficultyLabel | line 27 | Returns "MEDIUM" — controls points key and badge colour
    @Override protected String difficultyLabel() { return "MEDIUM"; }

    // difficultyColor | line 30 | Returns the gold accent colour used for the MEDIUM badge
    @Override protected Color  difficultyColor() { return ACCENT_GOLD; }

    // pointsReward | line 33 | Returns 3 — points awarded on first successful submission
    @Override protected int    pointsReward()    { return 3; }

    // PascalTriangleApp constructor | line 36 | Passes the parent frame to ChallengeConstructor to build the challenge UI
    public PascalTriangleApp(JFrame parent, Runnable onReturn) {
        super(parent, onReturn);
    }

    // problemHtml | line 41 | Returns the styled HTML problem description shown in the left panel
    @Override
    protected String problemHtml() {
        return "<html><body style='font-family:Segoe UI;font-size:12px;"
             + "color:#e6edf3;background:#161b22;padding:14px;'>"
             + "<p style='color:#8b949e;font-size:11px;'>DESCRIPTION</p>"
             + "<h3 style='color:#e6edf3;margin-top:4px;'>Pascal's Triangle</h3>"
             + "<p>Given an integer "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>numRows</code>, "
             + "return the first <code style='background:#30363d;padding:1px 5px;"
             + "border-radius:3px;'>numRows</code> rows of Pascal's triangle as a "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>"
             + "List&lt;List&lt;Integer&gt;&gt;</code>. "
             + "Each row's edges are 1, and every interior value is the sum of the two values above it.</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EXAMPLES</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> numRows=1 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> [[1]]</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> numRows=3 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> [[1],[1,1],[1,2,1]]</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> numRows=5 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> [[1],[1,1],[1,2,1],[1,3,3,1],[1,4,6,4,1]]</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>CONSTRAINTS</p>"
             + "<p>1 &le; numRows &le; 10 &nbsp;|&nbsp; Return type must be List&lt;List&lt;Integer&gt;&gt;</p>"
             + "</body></html>";
    }

    // starterCode | line 68 | Returns the pre-filled code skeleton shown in the editor when the challenge opens
    @Override
    protected String starterCode() {
        return "import java.util.*;\n\n"
             + "public static List<List<Integer>> solve(int numRows) {\n"
             + "    // Build Pascal's triangle up to numRows rows\n"
             + "    // Each interior value = sum of two values directly above it\n"
             + "    \n"
             + "}";
    }

    // judgeFunction | line 78 | Returns a reference to JavaJudge::judgePascalTriangle to evaluate the student's code
    @Override
    protected Function<String, TestResult> judgeFunction() {
        return JavaJudge::judgePascalTriangle;
    }
}
