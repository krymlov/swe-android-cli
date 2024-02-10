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

    @AfterClass
    public static void tearDown() {
        System.out.println("AndroidTest - tearDown()...");
    }

    @Test
    public void swe_test_main() {
        SweTest.swe_test_main(new String[]{"-hcmd"});
        System.out.println("swe_test_main - done!");
    }
}
