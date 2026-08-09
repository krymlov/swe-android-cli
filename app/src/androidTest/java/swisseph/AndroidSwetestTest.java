/*
 * Copyright (C) By the Author
 * Author    Yura Krymlov
 * Created   2026-08
 */

package swisseph;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exercises the Android port of swetest itself - <code>jni/swetest.c</code> with
 * <code>main()</code> renamed to <code>swe_test_main()</code>, output collected into
 * <code>SWE_OUT</code> and handed back through a StringBuilder by
 * <code>jni/swetest_jni.c</code>.
 * <p>
 * Two things are checked: that the numbers are the same ones upstream swetest prints on
 * the desktop, and that the structural risks of the port hold up - the static state being
 * reset between runs, the command line and the output buffer being bounded.
 * <p>
 * Reference values come from the desktop reference program with the same arguments and the
 * same ephemeris files the app ships in assets/ephe:
 * <pre>
 * swetest64.exe -b4.4.2000 -ut12:20:40 -eswe -true -sid27 -fPl -p0123456789mt \
 *               -house81.133333,16.166667,P -edir&lt;ephe&gt;
 * </pre>
 * assets/ephe carries sepl_18/semo_18, which cover 1800..2399 - hence the year 2000.
 *
 * @author Yura Krymlov
 * @version 1.0, 2026-08
 */
@RunWith(AndroidJUnit4.class)
public class AndroidSwetestTest {

    static final double DELTA = 1e-6;

    /** the fixture shared with swe-java-lib's SwetestRefChartsTest */
    static final String DATE = "-b4.4.2000";
    static final String TIME = "-ut12:20:40";
    static final String PLACE = "-house81.133333,16.166667,P";

    static String ephe;

    /** sidereal True Citra, true positions - exactly what the desktop run printed */
    static final Map<String, Double> EXPECTED = new LinkedHashMap<>();

    static {
        EXPECTED.put("Sun", 351.1984226);
        EXPECTED.put("Moon", 348.0661286);
        EXPECTED.put("Mercury", 324.3720069);
        EXPECTED.put("Venus", 333.3794791);
        EXPECTED.put("Mars", 15.2649731);
        EXPECTED.put("Jupiter", 16.1384917);
        EXPECTED.put("Saturn", 22.0727328);
        EXPECTED.put("Uranus", 295.9623746);
        EXPECTED.put("Neptune", 282.4218973);
        EXPECTED.put("Pluto", 228.9485161);
        EXPECTED.put("mean Node", 96.2211495);
        EXPECTED.put("true Node", 96.8851660);
        EXPECTED.put("Ascendant", 165.2732618);
    }

    @BeforeClass
    public static void setUp() {
        AppConfig appConfig = new AppConfig(getInstrumentation().getTargetContext());
        File epheFolder = appConfig.appEpheFolder();
        appConfig.extractAssets(AppConfig.EPHE_PATH, epheFolder);
        ephe = "-edir" + epheFolder.getAbsolutePath();
        System.loadLibrary("swe-2.10.03");
    }

    private static String run(String args) {
        StringBuilder sout = new StringBuilder();
        SwissephTest.swe_test_main(args, sout);
        return sout.toString();
    }

    /** "Sun   351.1984226" style lines, keyed by the name in front of the number */
    private static Map<String, Double> parse(String output) {
        Map<String, Double> values = new LinkedHashMap<>();
        for (String line : output.split("\r?\n")) {
            int sp = line.lastIndexOf(' ');
            if (sp <= 0) continue;
            try {
                values.put(line.substring(0, sp).trim(),
                        Double.parseDouble(line.substring(sp + 1).trim()));
            } catch (NumberFormatException notANumericLine) {
                // header or text line
            }
        }
        return values;
    }

    private static String chartCommand() {
        return "swetest " + DATE + " " + TIME + " -p0123456789mt -eswe -true -sid27 -fPl "
                + PLACE + " " + ephe;
    }

    // ============================================================== it runs

    @Test
    public void test_help_is_produced() {
        String out = SwissephTest.cli().help().toString();
        assertNotNull(out);
        assertTrue("help should be a few kilobytes, got " + out.length(), out.length() > 5000);
        assertTrue("help should mention swetest", out.contains("swetest"));
    }

    @Test
    public void test_ephemeris_files_are_actually_used() {
        String out = run(chartCommand());

        // a missing .se1 makes swetest fall back to the analytic Moshier theory and only
        // warn about it - every number after that is off by ~0.02" and worthless as a check
        assertFalse("fell back to Moshier:\n" + out, out.contains("Moshier"));
        assertFalse("ephemeris file not found:\n" + out, out.contains("not found"));
    }

    // ============================================== the numbers are the same

    @Test
    public void test_planets_nodes_and_lagna_match_the_desktop_reference() {
        Map<String, Double> actual = parse(run(chartCommand()));

        for (Map.Entry<String, Double> e : EXPECTED.entrySet()) {
            Double got = actual.get(e.getKey());
            assertNotNull(e.getKey() + " missing from output", got);
            assertEquals(e.getKey(), e.getValue(), got, DELTA);
        }
    }

    @Test
    public void test_house_cusps_match_the_desktop_reference() {
        Map<String, Double> actual = parse(run(chartCommand()));

        // Placidus cusps for the same chart, from the desktop run
        double[] cusps = {165.2732618, 195.1065439, 225.0725281, 254.8159428,
                284.9599758, 315.5737607, 345.2732618, 15.1065439, 45.0725281,
                74.8159428, 104.9599758, 135.5737607};

        for (int h = 1; h <= 12; h++) {
            Double got = actual.get("house " + (h < 10 ? " " : "") + h);
            assertNotNull("house " + h + " missing", got);
            assertEquals("house " + h, cusps[h - 1], got, DELTA);
        }
    }

    @Test
    public void test_julian_day_matches_the_desktop_reference() {
        String out = run("swetest " + DATE + " " + TIME + " -p0 -fPl " + ephe);

        String ut = null;
        for (String line : out.split("\r?\n")) {
            if (line.startsWith("UT:")) ut = line;
        }
        assertNotNull("no UT: line in:\n" + out, ut);
        assertTrue("unexpected julian day: " + ut, ut.contains("2451639"));
    }

    // ================================ the port's static state must be reset

    /**
     * swetest was written as a one-shot program: everything lives in file scope statics.
     * The port renames main() and calls swe_test_init() after every run to put those back.
     * If it misses one, a run leaks into the next - so the same command has to give the
     * same answer whatever ran in between.
     */
    @Test
    public void test_state_is_reset_between_runs() {
        String first = run(chartCommand());

        // something quite different in between: another date, tropical, other format,
        // other house system, and a run that fails
        run("swetest -b1.1.1900 -ut0:00 -p0 -fPLBRS " + ephe);
        run("swetest -b4.4.2000 -ut12:20:40 -p0 -house13.4,52.5,K -fPl " + ephe);
        run("swetest -nonsense-option");
        run("swetest " + DATE + " " + TIME + " -p0 -emos -fPl " + ephe);

        String again = run(chartCommand());
        assertEquals("the same command must give the same output", first, again);
    }

    @Test
    public void test_output_does_not_carry_over_from_the_previous_run() {
        run(chartCommand());
        String second = run("swetest " + DATE + " " + TIME + " -p0 -fPl " + ephe);

        // -p0 asks for the Sun only, so nothing from the ten-planet run may still be there
        assertFalse("Pluto leaked from the previous run:\n" + second, second.contains("Pluto"));
        assertFalse("houses leaked from the previous run:\n" + second, second.contains("house "));
    }

    // ======================================================= bounded buffers

    /**
     * The command line is copied into a fixed buffer before being split. It used to be
     * AS_MAXCH (256) with a plain strcpy, which a long -edir path plus a few options
     * overflows; it is SWE_ARGS_SIZE (4096) with a bounded copy now.
     */
    @Test
    public void test_a_long_command_line_is_handled() {
        StringBuilder padded = new StringBuilder(chartCommand());
        // harmless repeated options to push the line well past 256 characters
        while (padded.length() < 900) padded.append(" -true");

        assertTrue(padded.length() > 256);
        Map<String, Double> actual = parse(run(padded.toString()));
        assertEquals("Sun", EXPECTED.get("Sun"), actual.get("Sun"), DELTA);
    }

    /**
     * Output is accumulated into a fixed 1 MB buffer. One step prints roughly 300 bytes,
     * so a few thousand steps reach the end of it: that has to truncate cleanly rather
     * than run past the buffer.
     */
    @Test
    public void test_a_large_output_is_truncated_not_corrupted() {
        String out = run("swetest " + DATE + " " + TIME + " -p0123456789 -n5000 -fPl " + ephe);

        assertNotNull(out);
        assertTrue("expected a large output, got " + out.length(), out.length() > 100000);
        assertTrue("must stay inside the 1 MB buffer, got " + out.length(), out.length() <= 1024 * 1024);

        if (out.length() >= 1024 * 1024 - 64) {
            assertTrue("a truncated output must say so", out.contains("output truncated"));
        }

        // and the library still works afterwards
        Map<String, Double> actual = parse(run(chartCommand()));
        assertEquals("Sun", EXPECTED.get("Sun"), actual.get("Sun"), DELTA);
    }

    @Test
    public void test_repeated_runs_do_not_grow_without_bound() {
        int length = 0;
        for (int i = 0; i < 20; i++) {
            String out = run(chartCommand());
            if (i == 0) length = out.length();
            else assertEquals("run " + i + " produced a different amount of output",
                    length, out.length());
        }
    }
}
