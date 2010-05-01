package net.ishchenko.omfp.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfOutline;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 20.03.2010
 * Time: 18:39:27
 */
public class OutlineGenerator extends PdfPageEventHelper {

    public static final String GENERIC_TAG_SECTION = "section://";

    private List<PdfOutline> outlines = new ArrayList<PdfOutline>();
    private PdfSettings settings;

    public OutlineGenerator(PdfOutline rootOutline, PdfSettings settings) {
        this.outlines.add(rootOutline);
        this.settings = settings;
    }

    @Override
    public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {

        if (text.startsWith(GENERIC_TAG_SECTION)) {
            String title = text.substring(text.indexOf("@") + 1);
            int level = Integer.valueOf(text.substring(GENERIC_TAG_SECTION.length(), text.indexOf("@")));

            if (settings.isMultiLevelOutline()) {

                for (int i = level; i < outlines.size(); i++) {
                    outlines.set(i, null);
                }
                int j = level;
                PdfOutline parent;
                while ((parent = outlines.get(j - 1)) == null) {
                    j--;
                }

                outlines.add(level, new PdfOutline(parent, new PdfDestination(PdfDestination.FITH), title));

            } else {

                new PdfOutline(outlines.get(0), new PdfDestination(PdfDestination.FITH), title);

            }

        }

    }

    public static String encode(int level, String title) {
        return GENERIC_TAG_SECTION + level + "@" + title;
    }

    /**
     * For tests only!
     * @return
     */
    List<PdfOutline> getOutlines() {
         return outlines;
    }

}
