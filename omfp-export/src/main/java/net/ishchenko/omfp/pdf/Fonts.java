package net.ishchenko.omfp.pdf;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 08.03.2010
 * Time: 14:53:13
 */
public class Fonts {

    private Font font;

    private PdfSettings settings;

    public Fonts(PdfSettings settings) throws IOException, DocumentException {

        this.settings = settings;

        String fontPath;
        if (new File(settings.getBaseFontPath()).isAbsolute()) {
            fontPath = settings.getBaseFontPath();
        } else {
            //todo: deal with paths
            fontPath = System.getProperty("basedir") + File.separator + "styles" + File.separator + settings.getBaseFontPath();
        }

        BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        font = new Font(baseFont, settings.getSize());
    }

    public class FontBuilder {

        private Font builderFont = new Font(font);

        public Font get() {
            return builderFont;
        }

        public float getLeading() {
            return builderFont.getSize() * 1.2f;
        }

        public float getFirstLineIndent() {
            return builderFont.getSize() * 1.5f;
        }

        public FontBuilder normal() {
            builderFont.setStyle(Font.NORMAL);
            return this;
        }

        public FontBuilder italic() {
            builderFont.setStyle(Font.ITALIC);
            return this;
        }

        public FontBuilder bold() {
            builderFont.setStyle(Font.BOLD);
            return this;
        }

        public FontBuilder toosmall() {
            builderFont.setSize(settings.getSizeVerysmall());
            return this;
        }

        public FontBuilder small() {
            builderFont.setSize(settings.getSizeSmall());
            return this;
        }

        public FontBuilder regular() {
            builderFont.setSize(settings.getSize());
            return this;
        }

        public FontBuilder huge() {
            builderFont.setSize(settings.getSize() * 1.8f);
            return this;
        }

    }

}
