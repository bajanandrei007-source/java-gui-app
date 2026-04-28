package SourceCode.Builders.Levels.Medium;

import SourceCode.Builders.ChallengeConstructor.ChallengeConstructor;
import SourceCode.Judge.JavaJudge;
import SourceCode.Judge.JavaJudge.TestResult;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 * Challenge 2 (Medium) — Integer Reversal
 *
 * The student must implement:
 *   public static int solve(int n)
 * returning the digits of n reversed as an int.
 * Returns 0 if the reversed number overflows the 32-bit signed int range.
 *
 * Points awarded: 3 (first-time completion only).
 */
public class IntegerReversalApp extends ChallengeConstructor {

    // challengeNumber | line 21 | Returns "2" — used as part of the unique challenge key for point tracking
    @Override protected String challengeNumber() { return "2"; }

    // challengeTitle | line 24 | Returns the human-readable challenge name shown in the UI
    @Override protected String challengeTitle()  { return "Integer Reversal"; }

    // difficultyLabel | line 27 | Returns "MEDIUM" — controls points key and badge colour
    @Override protected String difficultyLabel() { return "MEDIUM"; }

    // difficultyColor | line 30 | Returns the gold accent colour used for the MEDIUM badge
    @Override protected Color  difficultyColor() { return ACCENT_GOLD; }

    // pointsReward | line 33 | Returns 3 — points awarded on first successful submission
    @Override protected int    pointsReward()    { return 3; }

    // IntegerReversalApp constructor | line 36 | Passes the parent frame to ChallengeConstructor to build the challenge UI
    public IntegerReversalApp(JFrame parent, Runnable onReturn) {
        super(parent, onReturn);
    }

    // problemHtml | line 41 | Returns the styled HTML problem description shown in the left panel
    @Override
    protected String problemHtml() {
        return "<html><body style='font-family:Segoe UI;font-size:12px;"
             + "color:#e6edf3;background:#161b22;padding:14px;'>"
             + "<p style='color:#8b949e;font-size:11px;'>DESCRIPTION</p>"
             + "<h3 style='color:#e6edf3;margin-top:4px;'>Integer Reversal</h3>"
             + "<p>Given a signed 32-bit integer "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>n</code>, "
             + "return its digits reversed. If the reversed integer overflows the 32-bit signed "
             + "integer range <b>[-2<sup>31</sup>, 2<sup>31</sup> - 1]</b>, return "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>0</code>.</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EXAMPLES</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> n=123 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> 321</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> n=-456 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> -654</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> n=1200 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> 21</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> n=1534236469 &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> 0 (overflow)</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>CONSTRAINTS</p>"
             + "<p>-2<sup>31</sup> &le; n &le; 2<sup>31</sup> - 1 &nbsp;|&nbsp; Return 0 on overflow</p>"
             + "</body></html>";
    }

    // starterCode | line 70 | Returns the pre-filled code skeleton shown in the editor when the challenge opens
    @Override
    protected String starterCode() {
        return "public static int solve(int n) {\n"
             + "    // Reverse the digits of n\n"
             + "    // Return 0 if the result overflows 32-bit signed int range\n"
             + "    \n"
             + "}";
    }

    // judgeFunction | line 79 | Returns a reference to JavaJudge::judgeIntegerReversal to evaluate the student's code
    @Override
    protected Function<String, TestResult> judgeFunction() {
        return JavaJudge::judgeIntegerReversal;
    }
}
