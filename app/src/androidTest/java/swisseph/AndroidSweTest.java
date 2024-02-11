package swisseph;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Yura Krymlov
 * @version 1.0, 2024-02
 */
@RunWith(AndroidJUnit4.class)
public class AndroidSweTest {

    @BeforeClass
    public static void setUp() {
        System.out.println("setUp()...");
        System.loadLibrary("swe-2.10.03");
    }

    @Test
    public void swe_test_main() {
        StringBuilder sout = new StringBuilder();
        int res = SwephExp.swe_test_main("TEST -testaa97", 2, sout);
        System.out.println("swe_test_main - done! => " + res + " => " + sout);
    }

    @AfterClass
    public static void tearDown() {
        System.out.println("tearDown()...");
    }
}
