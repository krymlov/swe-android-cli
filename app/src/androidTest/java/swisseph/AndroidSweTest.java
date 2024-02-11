package swisseph;

import static android.os.Environment.DIRECTORY_MOVIES;

import android.Manifest;
import android.os.Environment;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Yura Krymlov
 * @version 1.0, 2024-02
 */
@RunWith(AndroidJUnit4.class)
public class AndroidSweTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule
            .grant(Manifest.permission.READ_MEDIA_VIDEO);

    @BeforeClass
    public static void setUp() {
        System.out.println("setUp()...");
        System.loadLibrary("swe-2.10.03");
    }

    @Test
    public void swe_test_main() {
        String epheFolders = Environment.getExternalStoragePublicDirectory
                (DIRECTORY_MOVIES).getAbsolutePath() + "/ephe";

        StringBuilder sout = new StringBuilder();
        SwephExp.swe_test_main("TEST -testaa97 -edir" + epheFolders, sout);

        System.out.println(sout);
    }

    @AfterClass
    public static void tearDown() {
        System.out.println("tearDown()...");
    }
}
