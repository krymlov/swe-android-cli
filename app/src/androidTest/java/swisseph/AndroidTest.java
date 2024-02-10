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
        int res = SweTest.swe_test_main("-b23.11.1933 -ut23:21 -house-70.072,73.148,P -p0", 4);
        System.out.println("swe_test_main - done! => " + res);
    }


    @AfterClass
    public static void tearDown() {
        System.out.println("AndroidTest - tearDown()...");
    }
}
