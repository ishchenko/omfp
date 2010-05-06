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

    private boolean multiLevelOutline;

    private String device;
    private String baseFontPath;
    private float size;
    private float sizeSmall;
    private float sizeVerysmall;

    private float pageWidth;
    private float pageHeight;
    private float margin;

    private PdfSettings(Builder builder) {
        this.baseFontPath = builder.baseFontPath;
        this.multiLevelOutline = builder.multiLevelOutline;
        this.pageHeight = builder.pageHeight;
        this.pageWidth = builder.pageWidth;
        this.margin = builder.margin;
        this.size = builder.size;
        this.sizeSmall = builder.sizeSmall;
        this.sizeVerysmall = builder.sizeVerysmall;
        this.device = builder.device;
    }

    public boolean isMultiLevelOutline() {
        return multiLevelOutline;
    }

    public String getBaseFontPath() {
        return baseFontPath;
    }

    public float getPageHeight() {
        return pageHeight;
    }

    public float getPageWidth() {
        return pageWidth;
    }

    public float getMargin() {
        return margin;
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

        private static final Pattern DIMENSIONS_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\sx\\s(\\d+(?:\\.\\d+)?)");

        private String device;
        private String baseFontPath = "LiberationSerif-Regular.ttf";
        private float size = 9;
        private float sizeSmall = 7;
        private float sizeVerysmall = 5;

        private boolean multiLevelOutline = true;

        //defaults are for prs-505
        private float pageWidth = 88.2f;
        private float pageHeight = 113.9f;

        private float margin = 2;

        public static enum StyleType {
            NATIVE, FB2PDF
        }

        public Builder(InputStream settings, StyleType styleType) throws IOException, InvalidConfigurationException {

            switch (styleType) {
                case NATIVE:
                    SettingsUtils.loadFromProperties(settings, this);
                    break;
                case FB2PDF:
                    SettingsUtils.loadFromJson(settings, this);
            }

        }

        public Builder() {
        }

        public Builder baseFontPath(String path) {
            this.baseFontPath = path;
            return this;
        }

        public Builder multiLevelOutline(boolean multiLevelOutline) {
            this.multiLevelOutline = multiLevelOutline;
            return this;
        }

        /**
         * @param dimensions something like 25.4 x 36.6
         */
        public Builder dimensions(String dimensions) throws InvalidConfigurationException {
            Matcher matcher = DIMENSIONS_PATTERN.matcher(dimensions);
            if (matcher.find()) {
                pageWidth(Float.parseFloat(matcher.group(1)));
                pageHeight(Float.parseFloat(matcher.group(2)));
                return this;
            } else {
                throw new InvalidConfigurationException("Invalid dimensions format (" + dimensions + "). Should be like \"12.3 x 45\"");
            }
        }

        public Builder pageWidth(float pageWidth) {
            this.pageWidth = pageWidth;
            return this;
        }

        public Builder pageHeight(float pageHeight) {
            this.pageHeight = pageHeight;
            return this;
        }

        public Builder margin(float margin) {
            this.margin = margin;
            return this;
        }

        public Builder size(float size) {
            this.size = size;
            return this;
        }

        public Builder sizeSmall(float sizeSmall) {
            this.sizeSmall = sizeSmall;
            return this;
        }

        public Builder sizeVerysmall(float sizeVerysmall) {
            this.sizeVerysmall = sizeVerysmall;
            return this;
        }

        public Builder device(String device) {
            this.device = device;
            return this;
        }

        public PdfSettings build() {
            return new PdfSettings(this);
        }

    }
}
