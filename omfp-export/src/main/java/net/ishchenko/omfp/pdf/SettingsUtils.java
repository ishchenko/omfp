package net.ishchenko.omfp.pdf;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 05.05.2010
 * Time: 22:08:01
 */
public class SettingsUtils {

    public static void loadFromProperties(InputStream is, PdfSettings.Builder builder) throws IOException, InvalidConfigurationException {

        Properties props = new Properties();
        props.load(is);

        String dimensions = props.getProperty("dimensions");
        if (dimensions != null) {
            builder.dimensions(dimensions);
        }

        String margin = props.getProperty("margin");
        if (margin != null) {
            builder.margin(Float.parseFloat(margin));
        }

        String margins = props.getProperty("margins");
        if (margins != null) {
            builder.margins(margins);
        }

        String outline = props.getProperty("outline");
        if ("flat".equals(outline)) {
            builder.multiLevelOutline(false);
        } else if ("tree".equals(outline)) {
            builder.multiLevelOutline(true);
        } else {
            throw new InvalidConfigurationException("invalid outline value (" + outline + ")");
        }

        builder.device(props.getProperty("device"));

        String font = props.getProperty("font");
        if (font != null) {
            builder.baseFontPath(font);
        }

        String _size = props.getProperty("size");
        if (font != null) {
            builder.size(Integer.parseInt(_size));
        }

        String _sizeSmall = props.getProperty("size.small");
        if (font != null) {
            builder.sizeSmall(Integer.parseInt(_sizeSmall));
        }

        String _sizeVerysmall = props.getProperty("size.verysmall");
        if (font != null) {
            builder.sizeVerysmall(Integer.parseInt(_sizeVerysmall));
        }

    }


    public static void loadFromJson(InputStream settings, PdfSettings.Builder builder) {

        JsonParser parser = new JsonParser();
        InputStreamReader isr = new InputStreamReader(settings);
        JsonObject jsonObject = parser.parse(isr).getAsJsonObject();

        float fontSize = 9f;
        JsonArray paragraphStyles = jsonObject.getAsJsonArray("paragraphStyles");
        for (JsonElement style : paragraphStyles) {
            if ("default".equals(style.getAsJsonObject().getAsJsonPrimitive("name").getAsString())) {
                fontSize = getDimensionPoints(style.getAsJsonObject().getAsJsonPrimitive("fontSize").getAsString(), 1);
                break;
            }
        }

        builder.size(fontSize);
        builder.sizeSmall(fontSize * .777f);
        builder.sizeVerysmall(fontSize * .555f);

        JsonObject pageStyle = jsonObject.getAsJsonObject("pageStyle");

        String widthString = pageStyle.getAsJsonPrimitive("pageWidth").getAsString();
        String heightString = pageStyle.getAsJsonPrimitive("pageHeight").getAsString();
        builder.pageWidth(getDimensionMM(widthString, fontSize));
        builder.pageHeight(getDimensionMM(heightString, fontSize));

        builder.marginTop(getDimensionMM(pageStyle.getAsJsonPrimitive("marginTop").getAsString(), fontSize));
        builder.marginRight(getDimensionMM(pageStyle.getAsJsonPrimitive("marginRight").getAsString(), fontSize));
        builder.marginBottom(getDimensionMM(pageStyle.getAsJsonPrimitive("marginBottom").getAsString(), fontSize));
        builder.marginLeft(getDimensionMM(pageStyle.getAsJsonPrimitive("marginLeft").getAsString(), fontSize));

    }

    private static float getDimensionPoints(String dimension, float fontSize) {
        return getDimensionMM(dimension, fontSize) * PdfSettings.MM_TO_POINTS;
    }

    private static float getDimensionMM(String dimension, float fontSize) {
        float value = Float.parseFloat(dimension.substring(0, dimension.length() - 2));
        if (dimension.endsWith("pt")) {
            return value / PdfSettings.MM_TO_POINTS;
        } else if (dimension.endsWith("mm")) {
            return value;
        } else if (dimension.endsWith("em")) {
            return fontSize * value / PdfSettings.MM_TO_POINTS;
        } else {
            throw new IllegalArgumentException("Bad dimension (" + dimension + ")");
        }
    }

}
