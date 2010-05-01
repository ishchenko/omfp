package net.ishchenko.omfp.pdf;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 13.04.2010
 * Time: 4:08:01
 */
public class SettingsBuilderTest {

    @Test
    public void testFileHandling() throws IOException, InvalidConfigurationException {

        InputStream input = SettingsBuilderTest.class.getResourceAsStream("/test-settings-builder.properties");
        PdfSettings settings = new PdfSettings.Builder(input).build();

        assertEquals(53.6f, settings.getPageWidth(), .001);
        assertEquals(77.2f, settings.getPageHeight(), .001);
        assertEquals(15f, settings.getMargin(), .001);
        assertEquals(false, settings.isMultiLevelOutline());
        assertEquals("ipad", settings.getDevice());
        assertEquals("asdqwe.ttf", settings.getBaseFontPath());
        assertEquals(20, settings.getSize());
        assertEquals(30, settings.getSizeSmall());
        assertEquals(40, settings.getSizeVerysmall());

        InputStream input2 = SettingsBuilderTest.class.getResourceAsStream("/test-settings-builder2.properties");
        PdfSettings settings2 = new PdfSettings.Builder(input2).build();
        assertEquals(true, settings2.isMultiLevelOutline());

        InputStream input3 = SettingsBuilderTest.class.getResourceAsStream("/test-settings-builder-bad.properties");
        boolean thrown = false;
        try {
            new PdfSettings.Builder(input3).build();
        } catch (InvalidConfigurationException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

    @Test
    public void testAssignments() {

        PdfSettings settings = new PdfSettings.Builder().
                margin(23.4f).
                pageWidth(52.3f).
                pageHeight(36.6f).
                baseFontPath("asd/qwe").
                device("qwerty").
                multiLevelOutline(false).
                size(10).
                sizeSmall(20).
                sizeVerysmall(30).
                build();

        assertEquals(23.4f, settings.getMargin(), .001);
        assertEquals(52.3f, settings.getPageWidth(), .001);
        assertEquals(36.6f, settings.getPageHeight(), .001);
        assertEquals("asd/qwe", settings.getBaseFontPath());
        assertEquals("qwerty", settings.getDevice());
        assertEquals(10, settings.getSize());
        assertEquals(20, settings.getSizeSmall());
        assertEquals(30, settings.getSizeVerysmall());
        assertEquals(false, settings.isMultiLevelOutline());

    }

    @Test
    public void testPatterns() throws InvalidConfigurationException {

        doTestPatternMatch("12.3 x 94.7", 12.3f, 94.7f);
        doTestPatternMatch("5 x 6.4", 5f, 6.4f);
        doTestPatternMatch("5.1 x 8", 5.1f, 8);
        doTestPatternMatch("10 x 15", 10, 15);

        doTestPatternFailure("a x 17");
        doTestPatternFailure("12.3x45");
        doTestPatternFailure("1x 6");
        doTestPatternFailure("10 x0");

    }

    private void doTestPatternMatch(String dimensions, float width, float height) throws InvalidConfigurationException {
        PdfSettings settings2 = new PdfSettings.Builder().
                dimensions(dimensions).
                build();

        assertEquals(width, settings2.getPageWidth(), .001);
        assertEquals(height, settings2.getPageHeight(), .001);
    }

    private void doTestPatternFailure(String dimensions) {
        PdfSettings.Builder builder = new PdfSettings.Builder();
        boolean thrown = false;
        try {
            builder.dimensions(dimensions);
        } catch (InvalidConfigurationException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }


}
