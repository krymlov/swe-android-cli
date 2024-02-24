package swisseph;

/**
 * @author Yura Krymlov
 * @version 2.10.03
 */
public class SwissephTest {
    public static final String SWISSEPH_LIBRARY_NAME = "swe-2.10.03";
    protected static boolean sweLibraryLoaded;

    private SwissephTest() {
    }

    public static void loadSweLibrary() {
        if (!sweLibraryLoaded) {
            System.loadLibrary(SWISSEPH_LIBRARY_NAME);
            sweLibraryLoaded = true;
        }
    }

    public static native int swe_test_main(String args, StringBuilder sout);
}
