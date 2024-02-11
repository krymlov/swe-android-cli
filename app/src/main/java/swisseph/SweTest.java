package swisseph;

import java.io.PrintStream;

/**
 * @author Yura Krymlov
 * @version 1.0, 2024-02
 */
public class SweTest {
    private static final SweTest sweTest = new SweTest("swetest-2.10.03");

    private SweTest(String libraryName) {
        System.loadLibrary(libraryName);
    }

    public static native int swe_test_main(String jargs, int argc, StringBuilder sout);
}
