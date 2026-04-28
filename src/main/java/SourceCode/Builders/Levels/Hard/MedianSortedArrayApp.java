package SourceCode.Builders.Levels.Hard;

import SourceCode.Builders.ChallengeConstructor.ChallengeConstructor;
import SourceCode.Judge.JavaJudge;
import SourceCode.Judge.JavaJudge.TestResult;
import java.awt.Color;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 * Challenge 1 (Hard) — Median of Two Sorted Arrays
 *
 * The student must implement:
 *   public static String solve(int[] nums1, int[] nums2)
 * returning the median of the two combined sorted arrays, formatted to exactly
 * 1 decimal place as a String (e.g. "2.0" or "2.5").
 *
 * Points awarded: 5 (first-time completion only).
 */
public class MedianSortedArrayApp extends ChallengeConstructor {

    // challengeNumber | line 21 | Returns "1" — used as part of the unique challenge key for point tracking
    @Override protected String challengeNumber() { return "1"; }

    // challengeTitle | line 24 | Returns the human-readable challenge name shown in the UI
    @Override protected String challengeTitle()  { return "Median of Two Sorted Arrays"; }

    // difficultyLabel | line 27 | Returns "HARD" — controls points key and badge colour
    @Override protected String difficultyLabel() { return "HARD"; }

    // difficultyColor | line 30 | Returns the red accent colour used for the HARD badge
    @Override protected Color  difficultyColor() { return ACCENT_RED; }

    // pointsReward | line 33 | Returns 5 — points awarded on first successful submission
    @Override protected int    pointsReward()    { return 5; }

    // MedianSortedArrayApp constructor | line 36 | Passes the parent frame to ChallengeConstructor to build the challenge UI
    public MedianSortedArrayApp(JFrame parent, Runnable onReturn) {
        super(parent, onReturn);
    }

    // problemHtml | line 41 | Returns the styled HTML problem description shown in the left panel
    @Override
    protected String problemHtml() {
        return "<html><body style='font-family:Segoe UI;font-size:12px;"
             + "color:#e6edf3;background:#161b22;padding:14px;'>"
             + "<p style='color:#8b949e;font-size:11px;'>DESCRIPTION</p>"
             + "<h3 style='color:#e6edf3;margin-top:4px;'>Median of Two Sorted Arrays</h3>"
             + "<p>Given two sorted integer arrays "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>nums1</code> and "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>nums2</code>, "
             + "return the median of the two arrays combined. "
             + "The overall runtime complexity should be <b>O(log(m+n))</b>. "
             + "Return the result formatted to exactly <b>1 decimal place</b> as a String "
             + "(e.g. <code style='background:#30363d;padding:1px 5px;border-radius:3px;'>\"2.0\"</code> or "
             + "<code style='background:#30363d;padding:1px 5px;border-radius:3px;'>\"2.5\"</code>).</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>EXAMPLES</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> nums1=[1,3], nums2=[2] &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"2.0\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> nums1=[1,2], nums2=[3,4] &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"2.5\"</p>"
             + "<p><b style='color:#58a6ff;'>Input:</b> nums1=[], nums2=[1] &nbsp;&nbsp;"
             + "<b style='color:#3fb950;'>Output:</b> \"1.0\"</p>"
             + "<p style='color:#8b949e;font-size:11px;margin-top:16px;'>CONSTRAINTS</p>"
             + "<p>nums1 and nums2 are sorted in ascending order &nbsp;|&nbsp; "
             + "0 &le; m, n &le; 1000 &nbsp;|&nbsp; Return result as a String with 1 decimal place</p>"
             + "</body></html>";
    }

    // starterCode | line 70 | Returns the pre-filled code skeleton shown in the editor when the challenge opens
    @Override
    protected String starterCode() {
        return "public static String solve(int[] nums1, int[] nums2) {\n"
             + "    // Find the median of the two sorted arrays combined\n"
             + "    // Return result formatted to 1 decimal place e.g. \"2.0\" or \"2.5\"\n"
             + "    \n"
             + "}";
    }

    // judgeFunction | line 79 | Returns a reference to JavaJudge::judgeMedianSortedArray to evaluate the student's code
    @Override
    protected Function<String, TestResult> judgeFunction() {
        return JavaJudge::judgeMedianSortedArray;
    }
}
