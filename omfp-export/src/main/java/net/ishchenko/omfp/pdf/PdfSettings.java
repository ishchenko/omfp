package net.ishchenko.omfp.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 20.03.2010
 * Time: 21:09:23
 */
public class PdfSettings {

    public static final float MM_TO_POINTS = 72.0f / 25.4f;

    private boolean multiLevelOutline = true;

    private String device;
    private String baseFontPath = "LiberationSerif-Regular.ttf";
    private float size = 9;
    private float sizeSmall = 7;
    private float sizeVerysmall = 5;

    //defaults are for prs-505
    private float pageWidth = 88.2f;
    private float pageHeight = 113.9f;

    private float marginLeft = 2;
    private float marginRight = 2;
    private float marginTop = 2;
    private float marginBottom = 2;

    private PdfSettings() {
    }

    public boolean isMultiLevelOutline() {
        return multiLevelOutline;
    }

    public String getBaseFontPath() {
        return baseFontPath;
    }

    public float getPageHeightMM() {
        return pageHeight;
    }

    public float getPageWidthMM() {
        return pageWidth;
    }

    public float getMarginBottomMM() {
        return marginBottom;
    }

    public float getMarginLeftMM() {
        return marginLeft;
    }

    public float getMarginRightMM() {
        return marginRight;
    }

    public float getMarginTopMM() {
        return marginTop;
    }

    public float getPageHeightPoints() {
        return pageHeight * MM_TO_POINTS;
    }

    public float getPageWidthPoints() {
        return pageWidth * MM_TO_POINTS;
    }

    public float getMarginBottomPoints() {
        return marginBottom * MM_TO_POINTS;
    }

    public float getMarginLeftPoints() {
        return marginLeft * MM_TO_POINTS;
    }

    public float getMarginRightPoints() {
        return marginRight * MM_TO_POINTS;
    }

    public float getMarginTopPoints() {
        return marginTop * MM_TO_POINTS;
    }

    public float getSize() {
        return size;
    }

    public float getSizeSmall() {
        return sizeSmall;
    }

    public float getSizeVerysmall() {
        return sizeVerysmall;
    }

    public String getDevice() {
        return device;
    }

    public static class Builder {

        private static final Pattern DIMENSIONS_PATTERN = Pattern.compile("\\A([\\d\\.,]+)\\sx\\s([\\d\\.,]+)\\Z");
        private static final Pattern MARGINS_PATTERN = Pattern.compile("\\A([\\d\\.,]+)(?:\\s([\\d\\.,]+)(?:\\s([\\d\\.,]+)(?:\\s([\\d\\.,]+))?)?)?\\Z");

        private PdfSettings instance;

        public static enum StyleType {
            NATIVE, FB2PDF
        }

        public Builder(InputStream settings, StyleType styleType) throws IOException, InvalidConfigurationException {
            this();
            switch (styleType) {
                case NATIVE:
                    SettingsUtils.loadFromProperties(settings, this);
                    break;
                case FB2PDF:
                    SettingsUtils.loadFromJson(settings, this);
            }

        }

        public Builder() {
            instance = new PdfSettings();
        }

        public Builder baseFontPath(String path) {
            instance.baseFontPath = path;
            return this;
        }

        public Builder multiLevelOutline(boolean multiLevelOutline) {
            instance.multiLevelOutline = multiLevelOutline;
            return this;
        }

        /**
         * @param dimensions something like 25.4 x 36.6
         */
        public Builder dimensions(String dimensions) throws InvalidConfigurationException {
            Matcher matcher = DIMENSIONS_PATTERN.matcher(dimensions);
            if (matcher.find()) {
                pageWidth(Float.parseFloat(matcher.group(1).replace(",", ".")));
                pageHeight(Float.parseFloat(matcher.group(2).replace(",", ".")));
                return this;
            } else {
                throw new InvalidConfigurationException("Invalid dimensions format (" + dimensions + "). Should be like \"12.3 x 45\"");
            }
        }

        /**
         * @param margins Something like 10 21.2 30 36.6
         */
        public Builder margins(String margins) throws InvalidConfigurationException, NumberFormatException {
            Matcher matcher = MARGINS_PATTERN.matcher(margins);
            if (matcher.find()) {

                String first = matcher.group(1);
                String second = matcher.group(2);
                String third = matcher.group(3);
                String fourth = matcher.group(4);

                String marginTop = first;
                String marginRight = first;
                String marginBottom = first;
                String marginLeft = first;

                if (second != null) {
                    marginRight = second;
                    marginLeft = second;
                }

                if (third != null && fourth != null) {
                    marginBottom = third;
                    marginLeft = fourth;
                }

                if (third != null && fourth == null) {
                    throw new InvalidConfigurationException("Invalid margins count. 1, 2 or 4 expected.");
                }

                marginTop(Float.parseFloat(marginTop.replace(",", ".")));
                marginRight(Float.parseFloat(marginRight.replace(",", ".")));
                marginBottom(Float.parseFloat(marginBottom.replace(",", ".")));
                marginLeft(Float.parseFloat(marginLeft.replace(",", ".")));

            } else {
                throw new InvalidConfigurationException("Invalid margins format (" + margins + "). Should be like \"12.3 45 15.7 18\"");
            }
            return this;
        }

        public Builder pageWidth(float pageWidth) {
            instance.pageWidth = pageWidth;
            return this;
        }

        public Builder pageHeight(float pageHeight) {
            instance.pageHeight = pageHeight;
            return this;
        }

        public Builder marginLeft(float marginLeft) {
            instance.marginLeft = marginLeft;
            return this;
        }

        public Builder marginRight(float marginRight) {
            instance.marginRight = marginRight;
            return this;
        }

        public Builder marginTop(float marginTop) {
            instance.marginTop = marginTop;
            return this;
        }

        public Builder marginBottom(float marginBottom) {
            instance.marginBottom = marginBottom;
            return this;
        }

        public Builder size(float size) {
            instance.size = size;
            return this;
        }

        public Builder sizeSmall(float sizeSmall) {
            instance.sizeSmall = sizeSmall;
            return this;
        }

        public Builder sizeVerysmall(float sizeVerysmall) {
            instance.sizeVerysmall = sizeVerysmall;
            return this;
        }

        public Builder device(String device) {
            instance.device = device;
            return this;
        }

        public PdfSettings build() {
            return instance;
        }

    }
}
