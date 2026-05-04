package SourceCode.Judge;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.tools.*;

/**
 * JavaJudge — runtime code compiler and test-case evaluator.
 *
 * Responsibilities:
 *  - Defines all test suites (one per challenge) as lists of {@link TestCase} objects.
 *  - Compiles user-submitted Java code on-the-fly using the system JavaCompiler (JDK required).
 *  - Executes the compiled code against each test case in a sandboxed, timed thread.
 *  - Returns a {@link TestResult} summarising pass / fail counts and any errors.
 *
 * Architecture:
 *  1. Test-suite methods (e.g. {@code helloWorldTests()}) build and return test data.
 *  2. {@code judge()} is the core engine: compile → load → invoke → compare.
 *  3. Convenience entry-point methods (e.g. {@code judgeHelloWorld()}) wire a suite to the engine.
 *
 * NOTE:
 *  - Requires a full JDK on the classpath; a JRE-only environment will return SYSTEM_ERROR.
 *  - Compiled class files are written to a {@code temp/} directory and deleted after each run.
 *  - Execution is limited to {@value #TIMEOUT_SECONDS} seconds per full submission.
 */
public class JavaJudge {

    // ─────────────────────────────────────────────────────────────────────────
    //  Result model
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Encapsulates the outcome of running a full test suite against user code.
     *
     * {@code status}       — overall result (PASSED, FAILED, COMPILE_ERROR, etc.)
     * {@code cases}        — per-case detail (see {@link CaseResult})
     * {@code errorMessage} — human-readable error string; null on clean PASSED/FAILED
     */
    public static class TestResult {

        /** All possible outcomes for a submission. */
        public enum Status {
            PASSED,         // Every test case passed
            FAILED,         // At least one test case failed
            COMPILE_ERROR,  // Code did not compile
            RUNTIME_ERROR,  // Code threw an exception during execution
            TIMEOUT,        // Execution exceeded TIMEOUT_SECONDS
            SYSTEM_ERROR    // Judge infrastructure failed (e.g. no JDK found)
        }

        public final Status           status;
        public final List<CaseResult> cases;
        public final String           errorMessage;

        // TestResult constructor | line 54 | Stores status, case results, and optional error message
        public TestResult(Status status, List<CaseResult> cases, String errorMessage) {
            this.status       = status;
            this.cases        = Collections.unmodifiableList(cases);
            this.errorMessage = errorMessage;
        }

        // allPassed | line 61 | Returns true only when status is PASSED (all cases green)
        public boolean allPassed() { return status == Status.PASSED; }
    }

    /**
     * Holds the result of a single test case execution.
     *
     * {@code passed}   — whether the actual output matched the expected output
     * {@code input}    — human-readable representation of the inputs used
     * {@code expected} — the correct output string
     * {@code actual}   — what the student's code actually returned
     */
    public static class CaseResult {
        public final boolean passed;
        public final String  input;
        public final String  expected;
        public final String  actual;

        // CaseResult constructor | line 75 | Stores pass/fail flag and input/expected/actual strings
        public CaseResult(boolean passed, String input, String expected, String actual) {
            this.passed   = passed;
            this.input    = input;
            this.expected = expected;
            this.actual   = actual;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TestCase definition
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Describes a single test case: the method parameter types, argument values,
     * the expected String result, and a human-readable input description.
     *
     * Used by {@code judge()} to invoke the student's method via reflection.
     */
    public static class TestCase {
        public final Class<?>[] paramTypes;   // Java types of each method parameter
        public final Object[]   args;         // Actual argument values to pass
        public final String     expected;     // The correct return value as a String
        public final String     displayInput; // Shown in the UI alongside the result

        // TestCase constructor | line 97 | Stores parameter types, args, expected output, and display label
        public TestCase(Class<?>[] paramTypes, Object[] args,
                        String expected, String displayInput) {
            this.paramTypes   = paramTypes;
            this.args         = args;
            this.expected     = expected;
            this.displayInput = displayInput;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Test suites — Easy
    // ─────────────────────────────────────────────────────────────────────────

    // helloWorldTests | line 111 | Returns 3 identical test cases for Challenge 1 (no input, expect "Hello World")
    public static List<TestCase> helloWorldTests() {
        return List.of(
            new TestCase(new Class<?>[]{}, new Object[]{}, "Hello World", "(no input)"),
            new TestCase(new Class<?>[]{}, new Object[]{}, "Hello World", "(no input) #2"),
            new TestCase(new Class<?>[]{}, new Object[]{}, "Hello World", "(no input) #3")
        );
    }

    // evenOddTests | line 120 | Returns 8 test cases for Challenge 2: checks even/odd for positive, negative, and zero inputs
    public static List<TestCase> evenOddTests() {
        int[] inputs = {2, 3, 10, 7, 0, -4, -7, 1000001};
        List<TestCase> tests = new ArrayList<>();
        for (int n : inputs) {
            String expected = (n % 2 == 0) ? "Even" : "Odd";
            tests.add(new TestCase(
                new Class<?>[]{int.class},
                new Object[]{n},
                expected,
                String.valueOf(n)
            ));
        }
        return tests;
    }

    // largestNumberTests | line 135 | Returns 6 test cases for Challenge 3: checks max of two ints including negatives and Integer.MAX_VALUE
    public static List<TestCase> largestNumberTests() {
        int[][] pairs = {
            {3, 7}, {10, 2}, {5, 5}, {-1, -8}, {0, 100}, {Integer.MAX_VALUE, 0}
        };
        List<TestCase> tests = new ArrayList<>();
        for (int[] p : pairs) {
            tests.add(new TestCase(
                new Class<?>[]{int.class, int.class},
                new Object[]{p[0], p[1]},
                String.valueOf(Math.max(p[0], p[1])),
                p[0] + ", " + p[1]
            ));
        }
        return tests;
    }

    // reverseStringTests | line 152 | Returns 7 test cases for Challenge 4: reversal of normal, palindrome, empty, and single-char strings
    public static List<TestCase> reverseStringTests() {
        String[][] cases = {
            {"hello",    "olleh"},
            {"Java",     "avaJ"},
            {"racecar",  "racecar"},
            {"",         ""},
            {"a",        "a"},
            {"LeetCode", "edoCteeL"},
            {"12345",    "54321"}
        };
        List<TestCase> tests = new ArrayList<>();
        for (String[] c : cases) {
            tests.add(new TestCase(
                new Class<?>[]{String.class},
                new Object[]{c[0]},
                c[1],
                "\"" + c[0] + "\""
            ));
        }
        return tests;
    }

    // sumArrayTests | line 175 | Returns 7 test cases for Challenge 5: sum of arrays including negatives, zeros, and single elements
    public static List<TestCase> sumArrayTests() {
        int[][] arrays = {
            {1, 2, 3, 4, 5},
            {10, 20, 30},
            {0, 0, 0},
            {-5, 5},
            {100},
            {-1, -2, -3},
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
        };
        List<TestCase> tests = new ArrayList<>();
        for (int[] arr : arrays) {
            tests.add(new TestCase(
                new Class<?>[]{int[].class},
                new Object[]{arr},
                String.valueOf(Arrays.stream(arr).sum()),
                Arrays.toString(arr)
            ));
        }
        return tests;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Test suites — Medium
    // ─────────────────────────────────────────────────────────────────────────

    // caesarCipherTests | line 202 | Returns 8 test cases for Challenge M1: wrapping, shift-0, ROT13, and non-letter preservation
    public static List<TestCase> caesarCipherTests() {
        Object[][] cases = {
            {"Hello",              3,  "Khoor"},
            {"xyz",                3,  "abc"},
            {"Hello, World!",     13,  "Uryyb, Jbeyq!"},
            {"ABC",                0,  "ABC"},
            {"abc",               25,  "zab"},
            {"The Quick Brown Fox", 5, "Ymj Vznhp Gwtbs Ktc"},
            {"ZzAa",               1,  "AaBb"},
            {"Stay! 123",          4,  "Wxec! 123"}
        };
        List<TestCase> tests = new ArrayList<>();
        for (Object[] c : cases) {
            String text  = (String) c[0];
            int    shift = (int)    c[1];
            String exp   = (String) c[2];
            tests.add(new TestCase(
                new Class<?>[]{String.class, int.class},
                new Object[]{text, shift},
                exp,
                "\"" + text + "\", shift=" + shift
            ));
        }
        return tests;
    }

    // integerReversalTests | line 229 | Returns 8 test cases for Challenge M2: positive, negative, trailing zeros, and overflow cases
    public static List<TestCase> integerReversalTests() {
        int[][] cases = {
            {123,         321},
            {-456,       -654},
            {1200,         21},
            {0,             0},
            {-120,        -21},
            {1534236469,    0},   // overflow → return 0
            {-2147483648,   0},   // overflow → return 0
            {9,             9}
        };
        List<TestCase> tests = new ArrayList<>();
        for (int[] c : cases) {
            tests.add(new TestCase(
                new Class<?>[]{int.class},
                new Object[]{c[0]},
                String.valueOf(c[1]),
                String.valueOf(c[0])
            ));
        }
        return tests;
    }

    // pascalTriangleTests | line 253 | Returns 8 test cases for Challenge M3: row counts from 1 to 10, including full 10-row expansion
    public static List<TestCase> pascalTriangleTests() {
        int[]    rows = {1, 2, 3, 4, 5, 6, 7, 10};
        String[] exp  = {
            "[[1]]",
            "[[1], [1, 1]]",
            "[[1], [1, 1], [1, 2, 1]]",
            "[[1], [1, 1], [1, 2, 1], [1, 3, 3, 1]]",
            "[[1], [1, 1], [1, 2, 1], [1, 3, 3, 1], [1, 4, 6, 4, 1]]",
            "[[1], [1, 1], [1, 2, 1], [1, 3, 3, 1], [1, 4, 6, 4, 1], [1, 5, 10, 10, 5, 1]]",
            "[[1], [1, 1], [1, 2, 1], [1, 3, 3, 1], [1, 4, 6, 4, 1], [1, 5, 10, 10, 5, 1], [1, 6, 15, 20, 15, 6, 1]]",
            "[[1], [1, 1], [1, 2, 1], [1, 3, 3, 1], [1, 4, 6, 4, 1], [1, 5, 10, 10, 5, 1],"
                + " [1, 6, 15, 20, 15, 6, 1], [1, 7, 21, 35, 35, 21, 7, 1],"
                + " [1, 8, 28, 56, 70, 56, 28, 8, 1], [1, 9, 36, 84, 126, 126, 84, 36, 9, 1]]"
        };
        List<TestCase> tests = new ArrayList<>();
        for (int i = 0; i < rows.length; i++) {
            tests.add(new TestCase(
                new Class<?>[]{int.class},
                new Object[]{rows[i]},
                exp[i],
                "numRows=" + rows[i]
            ));
        }
        return tests;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Test suites — Hard
    // ─────────────────────────────────────────────────────────────────────────

    // medianSortedArrayTests | line 285 | Returns 8 test cases for Challenge H1: odd/even combined lengths, empty arrays, all-zero arrays
    public static List<TestCase> medianSortedArrayTests() {
        Object[][] cases = {
            {new int[]{1, 3},           new int[]{2},             "2.0"},
            {new int[]{1, 2},           new int[]{3, 4},          "2.5"},
            {new int[]{},               new int[]{1},             "1.0"},
            {new int[]{2},              new int[]{},              "2.0"},
            {new int[]{0, 0},           new int[]{0, 0},          "0.0"},
            {new int[]{1, 3, 5, 7},     new int[]{2, 4, 6, 8},   "4.5"},
            {new int[]{1, 2},           new int[]{-1, 3},         "1.5"},
            {new int[]{1, 2, 3, 4, 5},  new int[]{6, 7, 8, 9, 10}, "5.5"},
        };
        List<TestCase> tests = new ArrayList<>();
        for (Object[] c : cases) {
            int[]  n1  = (int[]) c[0];
            int[]  n2  = (int[]) c[1];
            String exp = (String) c[2];
            tests.add(new TestCase(
                new Class<?>[]{int[].class, int[].class},
                new Object[]{n1, n2},
                exp,
                Arrays.toString(n1) + " & " + Arrays.toString(n2)
            ));
        }
        return tests;
    }

    // stringToIntegerTests | line 315 | Returns 10 test cases for Challenge H2: ones, teens, tens, hundreds, thousands, and compound phrases
    public static List<TestCase> stringToIntegerTests() {
        String[][] cases = {
            {"Thirty Three",                         "33"},
            {"One Hundred Five",                     "105"},
            {"Two Thousand Four Hundred Twelve",     "2412"},
            {"Twenty",                               "20"},
            {"Zero",                                 "0"},
            {"Fifteen",                              "15"},
            {"Nine Hundred Ninety Nine",             "999"},
            {"One Thousand",                         "1000"},
            {"Twelve Thousand Three Hundred Forty Five", "12345"},
            {"Five Hundred",                         "500"},
        };
        List<TestCase> tests = new ArrayList<>();
        for (String[] c : cases) {
            tests.add(new TestCase(
                new Class<?>[]{String.class},
                new Object[]{c[0]},
                c[1],
                "\"" + c[0] + "\""
            ));
        }
        return tests;
    }

    // spiralMatrixTests | line 340 | Returns 8 test cases for Challenge H3: square, rectangular, 1x1, 1-row, 1-col, and large 5x5 matrices
    public static List<TestCase> spiralMatrixTests() {
        Object[][] cases = {
            {new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}},
                "1 2 3 6 9 8 7 4 5"},
            {new int[][]{{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}},
                "1 2 3 4 8 12 11 10 9 5 6 7"},
            {new int[][]{{7}},
                "7"},
            {new int[][]{{1, 2}, {3, 4}},
                "1 2 4 3"},
            {new int[][]{{1, 2, 3}},
                "1 2 3"},
            {new int[][]{{1}, {2}, {3}},
                "1 2 3"},
            {new int[][]{{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14,15},{16,17,18,19,20},{21,22,23,24,25}},
                "1 2 3 4 5 10 15 20 25 24 23 22 21 16 11 6 7 8 9 14 19 18 17 12 13"},
            {new int[][]{{1, 2, 3}, {4, 5, 6}},
                "1 2 3 6 5 4"},
        };
        List<TestCase> tests = new ArrayList<>();
        for (Object[] c : cases) {
            int[][]       mat     = (int[][]) c[0];
            String        exp     = (String)  c[1];
            // Build a readable display string like [[1,2],[3,4]]
            StringBuilder display = new StringBuilder("[");
            for (int i = 0; i < mat.length; i++) {
                display.append(Arrays.toString(mat[i]));
                if (i < mat.length - 1) display.append(",");
            }
            display.append("]");
            tests.add(new TestCase(
                new Class<?>[]{int[][].class},
                new Object[]{mat},
                exp,
                display.toString()
            ));
        }
        return tests;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Core judge engine
    // ─────────────────────────────────────────────────────────────────────────

    /** Maximum seconds a submission is allowed to run before TIMEOUT is returned. */
    private static final int    TIMEOUT_SECONDS = 5;

    /** Name used for the wrapper class written around the student's code. */
    private static final String CLASS_NAME      = "UserSolution";

    /** Temporary directory where the .java and .class files are written and then deleted. */
    private static final File   TEMP_DIR        = new File(System.getProperty("java.io.tmpdir"), "logiclab-judge");

    /**
     * Compiles and executes user-submitted Java code against a list of test cases.
     *
     * Steps:
     *  1. Wrap user code in a public class named {@value #CLASS_NAME} and write to disk.
     *  2. Compile using the system JavaCompiler; return COMPILE_ERROR on failure.
     *  3. Load the compiled class via URLClassLoader and resolve the target method.
     *  4. Execute each test case in a timed thread; compare actual vs. expected output.
     *  5. Clean up temp files in the finally block.
     *
     * @param userCode    The student's method body (will be wrapped in a class).
     * @param methodName  Name of the static method to invoke (always {@code "solve"}).
     * @param testCases   The ordered list of test cases to run.
     * @return            A {@link TestResult} with status and per-case detail.
     */
    // judge | line 395 | Core engine: wraps code in a class, compiles it, runs each test case in a timed thread, and returns aggregate results
    public static TestResult judge(String userCode, String methodName,
                                   List<TestCase> testCases) {

        File           sourceFile  = new File(TEMP_DIR, CLASS_NAME + ".java");
        File           classFile   = new File(TEMP_DIR, CLASS_NAME + ".class");
        URLClassLoader classLoader = null;

        try {
            // ── Step 1: Write wrapped source to disk ──────────────────────────
            TEMP_DIR.mkdirs();
            String fullCode = "public class " + CLASS_NAME + " {\n" + userCode + "\n}";
            try (FileWriter fw = new FileWriter(sourceFile)) { fw.write(fullCode); }

            // ── Step 2: Compile the source file ───────────────────────────────
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                return new TestResult(TestResult.Status.SYSTEM_ERROR, List.of(),
                    "JDK not found — run on a JDK, not a JRE.");
            }

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            try (StandardJavaFileManager fm =
                     compiler.getStandardFileManager(diagnostics, null, null)) {
                boolean compiled = compiler.getTask(
                    null, fm, diagnostics, null, null,
                    fm.getJavaFileObjectsFromFiles(List.of(sourceFile))
                ).call();

                if (!compiled) {
                    // Collect only ERROR-level diagnostics; adjust line number for the wrapper class offset
                    StringBuilder sb = new StringBuilder();
                    for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                        if (d.getKind() == Diagnostic.Kind.ERROR) {
                            sb.append("Line ").append(Math.max(1, d.getLineNumber() - 1))
                              .append(": ").append(d.getMessage(null)).append("\n");
                        }
                    }
                    return new TestResult(TestResult.Status.COMPILE_ERROR,
                        List.of(), sb.toString().trim());
                }
            }

            // ── Step 3: Load the compiled class and resolve the method ────────
            classLoader = URLClassLoader.newInstance(new URL[]{ TEMP_DIR.toURI().toURL() });
            Class<?> cls    = Class.forName(CLASS_NAME, true, classLoader);
            Method   method = cls.getMethod(methodName, testCases.get(0).paramTypes);

            // ── Step 4: Run each test case in a timed executor ────────────────
            List<CaseResult> results   = new ArrayList<>();
            boolean          allPassed = true;
            ExecutorService  exec      = Executors.newSingleThreadExecutor();

            for (TestCase tc : testCases) {
                final Object[] args   = tc.args;
                Future<Object> future = exec.submit(() -> method.invoke(null, args));
                String actual;

                try {
                    actual = String.valueOf(future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
                } catch (TimeoutException ex) {
                    future.cancel(true);
                    exec.shutdownNow();
                    return new TestResult(TestResult.Status.TIMEOUT, results,
                        "Execution exceeded " + TIMEOUT_SECONDS + " seconds.");
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    exec.shutdownNow();
                    return new TestResult(TestResult.Status.RUNTIME_ERROR, results,
                        cause != null ? cause.toString() : ex.toString());
                }

                boolean passed = actual.equals(tc.expected);
                if (!passed) allPassed = false;
                results.add(new CaseResult(passed, tc.displayInput, tc.expected, actual));
            }
            exec.shutdown();

            return new TestResult(
                allPassed ? TestResult.Status.PASSED : TestResult.Status.FAILED,
                results, null
            );

        } catch (Exception e) {
            return new TestResult(TestResult.Status.SYSTEM_ERROR, List.of(),
                e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            // ── Step 5: Always clean up temp files ───────────────────────────
            sourceFile.delete();
            classFile.delete();
            if (classLoader != null) {
                try { classLoader.close(); } catch (IOException ignored) {}
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Convenience entry points — Easy
    // ─────────────────────────────────────────────────────────────────────────

    // judgeHelloWorld | line 479 | Runs the Hello World test suite (Challenge 1 Easy) against user code
    public static TestResult judgeHelloWorld(String code)    { return judge(code, "solve", helloWorldTests()); }

    // judgeEvenOdd | line 482 | Runs the Even or Odd test suite (Challenge 2 Easy) against user code
    public static TestResult judgeEvenOdd(String code)       { return judge(code, "solve", evenOddTests()); }

    // judgeLargestNumber | line 485 | Runs the Largest Number test suite (Challenge 3 Easy) against user code
    public static TestResult judgeLargestNumber(String code) { return judge(code, "solve", largestNumberTests()); }

    // judgeReverseString | line 488 | Runs the Reverse String test suite (Challenge 4 Easy) against user code
    public static TestResult judgeReverseString(String code) { return judge(code, "solve", reverseStringTests()); }

    // judgeSumArray | line 491 | Runs the Sum of Array test suite (Challenge 5 Easy) against user code
    public static TestResult judgeSumArray(String code)      { return judge(code, "solve", sumArrayTests()); }

    // ─────────────────────────────────────────────────────────────────────────
    //  Convenience entry points — Medium
    // ─────────────────────────────────────────────────────────────────────────

    // judgeCaesarCipher | line 498 | Runs the Caesar Cipher test suite (Challenge 1 Medium) against user code
    public static TestResult judgeCaesarCipher(String code)    { return judge(code, "solve", caesarCipherTests()); }

    // judgeIntegerReversal | line 501 | Runs the Integer Reversal test suite (Challenge 2 Medium) against user code
    public static TestResult judgeIntegerReversal(String code) { return judge(code, "solve", integerReversalTests()); }

    // judgePascalTriangle | line 504 | Runs the Pascal Triangle test suite (Challenge 3 Medium) against user code
    public static TestResult judgePascalTriangle(String code)  { return judge(code, "solve", pascalTriangleTests()); }

    // ─────────────────────────────────────────────────────────────────────────
    //  Convenience entry points — Hard
    // ─────────────────────────────────────────────────────────────────────────

    // judgeMedianSortedArray | line 511 | Runs the Median Sorted Array test suite (Challenge 1 Hard) against user code
    public static TestResult judgeMedianSortedArray(String code) { return judge(code, "solve", medianSortedArrayTests()); }

    // judgeStringToInteger | line 514 | Runs the String to Integer test suite (Challenge 2 Hard) against user code
    public static TestResult judgeStringToInteger(String code)   { return judge(code, "solve", stringToIntegerTests()); }

    // judgeSpiralMatrix | line 517 | Runs the Spiral Matrix test suite (Challenge 3 Hard) against user code
    public static TestResult judgeSpiralMatrix(String code)      { return judge(code, "solve", spiralMatrixTests()); }
}
