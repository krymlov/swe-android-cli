package swisseph;

/**
 * @author Yura Krymlov
 * @version 1.0, 2024-02
 */
public class SweTest {
    private static final SweTest sweTest = new SweTest("swetest-2.10.03");

    private SweTest(String libraryName) {
        System.loadLibrary(libraryName);
    }

    // JNIEXPORT jint JNICALL Java_swisseph_SweTest_swe_1test_1main(JNIEnv *, jclass, jobjectArray);
    public static native int swe_test_main(String[] jargv);
}
