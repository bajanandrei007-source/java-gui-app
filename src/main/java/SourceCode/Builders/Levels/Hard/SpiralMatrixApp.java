package SourceCode.Builders.Levels.Hard;

import SourceCode.Builders.ChallengeConstructor.ChallengeConstructor;
import SourceCode.Judge.JavaJudge;
import SourceCode.Judge.JavaJudge.TestResult;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 * Challenge 3 (Hard) — Spiral Matrix
 *
 * The student must implement:
 *   public static String solve(int[][] matrix)
 * returning all matrix elements traversed in clockwise spiral order,
 * joined as a single space-separated String.
 * Example: [[1,2,3],[4,5,6],[7,8,9]] → "1 2 3 6 9 8 7 4 5"
 *
 * Points awarded: 5 (first-time completion only).
 */
public class SpiralMatrixApp extends ChallengeConstructor {

    // challengeNumber | line 22 | Returns "3" — used as part of the unique challenge key for point tracking
    @Override protected String challengeNumber() { return "3"; }

    // challengeTitle | line 25 | Returns the human-readable challenge name shown in the UI
    @Override protected String challengeTitle()  { return "Spiral Matrix"; }

    // difficultyLabel | line 28 | Returns "HARD" — controls points key and badge colour
    @Override protected String difficultyLabel() { return "HARD"; }

    // difficultyColor | line 31 | Returns the red accent colour used for the HARD badge
    @Override protected Color  difficultyColor() { return ACCENT_RED; }

    // pointsReward | line 34 | Returns 5 — points awarded on first successful submission
    @Override protected int    pointsReward()    { return 5; }

    // SpiralMatrixApp constructor | line 37 | Passes the parent frame to ChallengeConstructor to build the challenge UI
    public SpiralMatrixApp(JFrame parent, Runnable onReturn) {
        super(parent, onReturn);
    }

    // problemHtml | line 42 | Returns the styled HTML problem description shown in the left panel
    @Override
    protected String problemHtml() {
        return "<html><body style='font-family:Segoe UI;font-size:12px;"
             + "color:#e6edf3;background:#161b22;padding:14px;'>"
             + "<p style='color:#8b949e;font-size:11px;'>DESCRIPTION</p>"
             + "<h3 style='color:#e6edf3;margin-top:4px;'>Spiral Matrix</h3>"
             + "<p>Given an <code style='background:#30363d;padding:1px 5px;border-radius:3px;'>m x n</code> "
             + "matrix, return all elements of the matrix in <b>spiral order</b> "
             + "(clockwise from the top-left), as a space-separated String.</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EXAMPLES</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> [[1,2,3],[4,5,6],[7,8,9]]<br>"
             + "<b style='color:#3fb950;'>Output:</b> \"1 2 3 6 9 8 7 4 5\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> [[1,2,3,4],[5,6,7,8],[9,10,11,12]]<br>"
             + "<b style='color:#3fb950;'>Output:</b> \"1 2 3 4 8 12 11 10 9 5 6 7\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> [[7]]<br>"
             + "<b style='color:#3fb950;'>Output:</b> \"7\"</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>CONSTRAINTS</p>"
             + "<p>m == matrix.length &nbsp;|&nbsp; n == matrix[i].length &nbsp;|&nbsp; "
             + "1 &le; m, n &le; 10 &nbsp;|&nbsp; Return elements space-separated as a String</p>"
             + "</body></html>";
    }

    // starterCode | line 70 | Returns the pre-filled code skeleton shown in the editor when the challenge opens
    @Override
    protected String starterCode() {
        return "public static String solve(int[][] matrix) {\n"
             + "    // Traverse the matrix in clockwise spiral order\n"
             + "    // Return all elements as a space-separated String\n"
             + "    // e.g. [[1,2,3],[4,5,6],[7,8,9]] -> \"1 2 3 6 9 8 7 4 5\"\n"
             + "    \n"
             + "}";
    }

    // judgeFunction | line 80 | Returns a reference to JavaJudge::judgeSpiralMatrix to evaluate the student's code
    @Override
    protected Function<String, TestResult> judgeFunction() {
        return JavaJudge::judgeSpiralMatrix;
    }
}
