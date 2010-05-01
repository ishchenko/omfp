package net.ishchenko.omfp.pdf;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 08.03.2010
 * Time: 15:10:59
 */
public class Dimensions {

    public static final float MM_TO_POINTS = 72.0f / 25.4f;

    private PdfSettings settings;

    public Dimensions(PdfSettings settings) {
        this.settings = settings;
    }

    public float getMargin() {
        return settings.getMargin() * MM_TO_POINTS;
    }

    public float getPageHeight() {
        return settings.getPageHeight() * MM_TO_POINTS;
    }

    public float getPageWidth() {
        return settings.getPageWidth() * MM_TO_POINTS;
    }
}
