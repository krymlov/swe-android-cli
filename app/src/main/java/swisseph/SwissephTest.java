package swisseph;

/**
 * @author Yura Krymlov
 * @version 2.10.03
 */
public final class SwissephTest {
    private static final String SWISSEPH_LIBRARY_NAME = "swe-2.10.03";
    private static final SwissephTest swissephTest = new SwissephTest();

    private SwissephTest() {
        System.loadLibrary(SWISSEPH_LIBRARY_NAME);
    }

    public static SwissephTest cli() {
        return swissephTest;
    }

    public StringBuilder help() {
        StringBuilder sout = new StringBuilder();
        swe_test_main("swetest -h", sout);
        return sout;
    }

    public static native int swe_test_main(String args, StringBuilder sout);
}
