package swisseph;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * @author Yura Krymlov
 * @version 1.0, 2024-02
 */
@RunWith(AndroidJUnit4.class)
public class AndroidSweTest {

    @BeforeClass
    public static void setUp() {
        AppConfig appConfig = new AppConfig(getInstrumentation().getTargetContext());
        System.out.println("setUp()... EPHE files to: " + appConfig.appEpheFolder());
        appConfig.extractAssets(AppConfig.EPHE_PATH, appConfig.appEpheFolder());
        System.loadLibrary("swe-2.10.03");
    }

    @Test
    public void swe_test_main() {
        File epheFolder = new AppConfig(getInstrumentation()
                .getTargetContext()).appEpheFolder();

        StringBuilder sout = new StringBuilder();
        SwissephTest.swe_test_main("TEST -testaa97 -edir"
                + epheFolder.getAbsolutePath(), sout);

        System.out.println(sout);
    }

    @AfterClass
    public static void tearDown() {
        System.out.println("tearDown()...");
    }

}
