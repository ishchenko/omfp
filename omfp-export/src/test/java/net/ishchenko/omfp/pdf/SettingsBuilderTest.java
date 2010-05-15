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
        PdfSettings settings = new PdfSettings.Builder(input, PdfSettings.Builder.StyleType.NATIVE).build();

        assertEquals(53.6f, settings.getPageWidthMM(), .001);
        assertEquals(77.2f, settings.getPageHeightMM(), .001);
        assertEquals(15, settings.getMarginTopMM(), .001);
        assertEquals(16.7f, settings.getMarginRightMM(), .001);
        assertEquals(18.4f, settings.getMarginBottomMM(), .001);
        assertEquals(55, settings.getMarginLeftMM(), .001);
        assertEquals(false, settings.isMultiLevelOutline());
        assertEquals("ipad", settings.getDevice());
        assertEquals("asdqwe.ttf", settings.getBaseFontPath());
        assertEquals(20f, settings.getSize(), 0.001);
        assertEquals(30f, settings.getSizeSmall(), 0.001);
        assertEquals(40f, settings.getSizeVerysmall(), 0.001);

        InputStream input2 = SettingsBuilderTest.class.getResourceAsStream("/test-settings-builder2.properties");
        PdfSettings settings2 = new PdfSettings.Builder(input2, PdfSettings.Builder.StyleType.NATIVE).build();
        assertEquals(true, settings2.isMultiLevelOutline());

        InputStream input3 = SettingsBuilderTest.class.getResourceAsStream("/test-settings-builder-bad.properties");
        boolean thrown = false;
        try {
            new PdfSettings.Builder(input3, PdfSettings.Builder.StyleType.NATIVE).build();
        } catch (InvalidConfigurationException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

    @Test
    public void testAssignments() {

        PdfSettings settings = new PdfSettings.Builder().
                marginTop(12.3f).
                marginRight(45.6f).
                marginBottom(78.9f).
                marginLeft(5).
                pageWidth(52.3f).
                pageHeight(36.6f).
                baseFontPath("asd/qwe").
                device("qwerty").
                multiLevelOutline(false).
                size(10).
                sizeSmall(20).
                sizeVerysmall(30).
                build();

        assertEquals(12.3f, settings.getMarginTopMM(), .001);
        assertEquals(45.6, settings.getMarginRightMM(), .001);
        assertEquals(78.9f, settings.getMarginBottomMM(), .001);
        assertEquals(5, settings.getMarginLeftMM(), .001);

        assertEquals(52.3f, settings.getPageWidthMM(), .001);
        assertEquals(36.6f, settings.getPageHeightMM(), .001);
        assertEquals("asd/qwe", settings.getBaseFontPath());
        assertEquals("qwerty", settings.getDevice());
        assertEquals(10f, settings.getSize(), 0.001);
        assertEquals(20f, settings.getSizeSmall(), 0.001);
        assertEquals(30f, settings.getSizeVerysmall(), 0.001);
        assertEquals(false, settings.isMultiLevelOutline());

    }

    @Test
    public void testPatterns() throws InvalidConfigurationException {

        doTestDimensionsPatternMatch("12.3 x 94,7", 12.3f, 94.7f);
        doTestDimensionsPatternMatch("5 x 6.4", 5f, 6.4f);
        doTestDimensionsPatternMatch("5,1 x 8", 5.1f, 8);
        doTestDimensionsPatternMatch("10 x 15", 10, 15);

        doTestDimensionsPatternFailure("a x 17");
        doTestDimensionsPatternFailure("12.3x45");
        doTestDimensionsPatternFailure("1x 6");
        doTestDimensionsPatternFailure("10 x0");

        doTestMarginsPatternMatch("12.3", 12.3f, 12.3f, 12.3f, 12.3f);
        doTestMarginsPatternMatch("6", 6, 6, 6, 6);
        doTestMarginsPatternMatch("23.4 36", 23.4f, 36, 23.4f, 36);
        doTestMarginsPatternMatch("8 12,5", 8, 12.5f, 8, 12.5f);
        doTestMarginsPatternMatch("1 2 3,4 5.6", 1, 2, 3.4f, 5.6f);

        doTestMarginsPatternFailure("5 6 7 ");
        doTestMarginsPatternFailure("-10 6");
        doTestMarginsPatternFailure("1 2 3 4 5");

    }

    private void doTestDimensionsPatternMatch(String dimensions, float width, float height) throws InvalidConfigurationException {
        PdfSettings settings = new PdfSettings.Builder().
                dimensions(dimensions).
                build();

        assertEquals(width, settings.getPageWidthMM(), .001);
        assertEquals(height, settings.getPageHeightMM(), .001);
    }

    private void doTestDimensionsPatternFailure(String dimensions) {
        PdfSettings.Builder builder = new PdfSettings.Builder();
        boolean thrown = false;
        try {
            builder.dimensions(dimensions);
        } catch (InvalidConfigurationException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }


    private void doTestMarginsPatternMatch(String margins, float top, float right, float bottom, float left) throws InvalidConfigurationException {
        PdfSettings settings = new PdfSettings.Builder().
                margins(margins).
                build();

        assertEquals(top, settings.getMarginTopMM(), .001);
        assertEquals(right, settings.getMarginRightMM(), .001);
        assertEquals(bottom, settings.getMarginBottomMM(), .001);
        assertEquals(left, settings.getMarginLeftMM(), .001);
    }

    private void doTestMarginsPatternFailure(String margins) {
        PdfSettings.Builder builder = new PdfSettings.Builder();
        boolean thrown = false;
        try {
            builder.margins(margins);
        } catch (InvalidConfigurationException e) {
            thrown = true;
        } catch (NumberFormatException e) {
            thrown = true;
        }
        
        assertTrue(thrown);
    }


}
