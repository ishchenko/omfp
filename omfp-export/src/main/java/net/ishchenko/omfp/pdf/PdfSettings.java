package net.ishchenko.omfp.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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
    private int size = 9;
    private int sizeSmall = 7;
    private int sizeVerysmall = 5;

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

    public int getSize() {
        return size;
    }

    public int getSizeSmall() {
        return sizeSmall;
    }

    public int getSizeVerysmall() {
        return sizeVerysmall;
    }

    public String getDevice() {
        return device;
    }

    public static class Builder {

        private static final Pattern DIMENSIONS_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\sx\\s(\\d+(?:\\.\\d+)?)");

        public String device;
        private String baseFontPath = "LiberationSerif-Regular.ttf";
        private int size = 9;
        private int sizeSmall = 7;
        private int sizeVerysmall = 5;

        private boolean multiLevelOutline = true;

        //defaults are for prs-505
        private float pageWidth = 88.2f;
        private float pageHeight = 113.9f;

        private float margin = 2;

        public Builder(InputStream settings) throws IOException, InvalidConfigurationException {

            Properties props = new Properties();
            props.load(settings);

            String dimensions = props.getProperty("dimensions");
            if (dimensions != null) {
                dimensions(dimensions);
            }

            String margin = props.getProperty("margin");
            if (margin != null) {
                margin(Float.parseFloat(margin));
            }

            String outline = props.getProperty("outline");
            if (outline != null) {
                if (outline.equals("flat")) {
                    multiLevelOutline(false);
                } else if (outline.equals("tree")) {
                    multiLevelOutline(true);
                } else {
                    throw new InvalidConfigurationException("invalid outline value (" + outline + ")");
                }
            }

            device = props.getProperty("device");

            String font = props.getProperty("font");
            if (font != null) {
                baseFontPath(font);
            }

            String _size = props.getProperty("size");
            if (font != null) {
                size(Integer.parseInt(_size));
            }

            String _sizeSmall = props.getProperty("size.small");
            if (font != null) {
                sizeSmall(Integer.parseInt(_sizeSmall));
            }

            String _sizeVerysmall = props.getProperty("size.verysmall");
            if (font != null) {
                sizeVerysmall(Integer.parseInt(_sizeVerysmall));
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

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder sizeSmall(int sizeSmall) {
            this.sizeSmall = sizeSmall;
            return this;
        }

        public Builder sizeVerysmall(int sizeVerysmall) {
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
