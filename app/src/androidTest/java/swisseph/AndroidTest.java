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
public class AndroidTest {

    @BeforeClass
    public static void setUp() {
        System.out.println("AndroidTest - setUp()...");
    }

    @Test
    public void swe_test_main() {
        int res = SweTest.swe_test_main("-testaa95 -testaa95", 2);
        System.out.println("swe_test_main - done! => " + res);
    }


    @AfterClass
    public static void tearDown() {
        System.out.println("AndroidTest - tearDown()...");
    }
}
