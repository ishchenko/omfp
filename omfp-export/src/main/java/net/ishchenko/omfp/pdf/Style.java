package net.ishchenko.omfp.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.HyphenationAuto;
import com.itextpdf.text.pdf.HyphenationEvent;
import com.itextpdf.text.pdf.PdfContentByte;
import net.ishchenko.omfp.FictionBookWalker;
import net.ishchenko.omfp.ProcessingContext;
import net.ishchenko.omfp.model.AnnotationType;
import net.ishchenko.omfp.model.CiteType;
import net.ishchenko.omfp.model.EpigraphType;
import net.ishchenko.omfp.model.TitleType;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 09.03.2010
 * Time: 15:58:11
 */
public class Style {

    private Fonts fonts;
    private HyphenationEvent hyphenationAuto;
    private PdfSettings settings;

    public Style(PdfSettings settings) throws IOException, DocumentException {

        this.settings = settings;

        fonts = new Fonts(settings);
        hyphenationAuto = new HyphenationAuto("ru", "none", 2, 2);

    }

    void applyToParagraph(Paragraph paragraph, ProcessingContext context) {

        Fonts.FontBuilder fontBuilder = fonts.new FontBuilder();
        paragraph.setLeading(fontBuilder.getLeading());
        paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
        paragraph.setFirstLineIndent(fontBuilder.getFirstLineIndent());

        if (isInsideTitle(context)) {

            paragraph.setLeading(fontBuilder.huge().getLeading());
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.setFirstLineIndent(0);
            paragraph.setKeepTogether(true); //todo

        } else if (isInsideEpigraph(context)) {

            paragraph.setLeading(fontBuilder.italic().small().getLeading());
            paragraph.setFirstLineIndent(0);
            paragraph.setIndentationLeft(settings.getPageHeightMM() / 4);
            if (context.stackHas(FictionBookWalker.Hints.AUTHOR.getClass())) {
                paragraph.setAlignment(Element.ALIGN_RIGHT);
            }

        } else if (isInsideAnnotation(context)) {

            paragraph.setLeading(fontBuilder.italic().small().getLeading());

        } else if (isInsideSubtitle(context)) {

            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.setFirstLineIndent(0);

        } else if (isInsideCite(context)) {

            paragraph.setIndentationLeft(fontBuilder.getLeading());
            paragraph.setIndentationRight(fontBuilder.getLeading());

            if (context.getIteration().isLast()) {
                paragraph.setSpacingAfter(fontBuilder.getLeading() * .5f);
            }

            if (context.stackHas(FictionBookWalker.Hints.AUTHOR.getClass())) {
                paragraph.setAlignment(Element.ALIGN_RIGHT);
            }
        }

    }

    private boolean isInsideSubtitle(ProcessingContext context) {
        FictionBookWalker.PolyType type = context.getTopmost(FictionBookWalker.PolyType.class);
        return type != null && type.getName().equals("subtitle");
    }


    public Chunk getNewChunk(ProcessingContext context) {

        Chunk chunk = new Chunk();

        Fonts.FontBuilder fontBuilder = fonts.new FontBuilder();

        chunk.setHyphenation(hyphenationAuto);

        if (isInsideTitle(context)) {
            fontBuilder.huge();
            chunk.setHyphenation(null);
        } else if (isInsideEpigraph(context) || isInsideAnnotation(context)) {
            fontBuilder.italic().small();
        } else if (isInsideCite(context)) {
            fontBuilder.italic();
        }

        if (isInsideSubtitle(context)) {
            chunk.setHyphenation(null);
        }

        FictionBookWalker.PolyType type = context.getTopmost(FictionBookWalker.PolyType.class);
//        <xs:element name="strong" type="styleType"/>
//        <xs:element name="emphasis" type="styleType"/>
//        <xs:element name="strikethrough" type="styleType"/>
//        <xs:element name="sub" type="styleType"/>
//        <xs:element name="sup" type="styleType"/>
//        <xs:element name="code" type="styleType"/>
        if (type != null) {
            if (type.getName().equals("strong")) {
                fontBuilder.bold();
            } else if (type.getName().equals("emphasis")) {
                fontBuilder.italic();
            } else if (type.getName().equals("sub")) {
                chunk.setTextRise(-fontBuilder.get().getSize() / 3);
            } else if (type.getName().equals("sup")) {
                chunk.setTextRise(fontBuilder.get().getSize() / 3);
            } else if (type.getName().equals("code")) {
                //todo
            }

        }

        chunk.setFont(fontBuilder.get());

        return chunk;

    }

    public Document getNewStyledDocument() {
        Document document = new Document();
        applyToDocument(document);
        return document;
    }

    public void applyToMainColumn(ColumnText ct, Document document) {
        applyToMainColumn(ct, document, 0);
    }

    /**
     * +---------------+
     * |    margin     |
     * +---------------+
     * |               |
     * |  mainColumn   |
     * |               |
     * |               |
     * +---------------+
     * |    margin     |
     * +---------------+
     * |   footnotes   |
     * |               |
     * +---------------+
     * |    margin     |
     * +---------------+
     *
     * @param ct
     * @param document
     * @param footnoteSpace
     */

    public void applyToMainColumn(ColumnText ct, Document document, float footnoteSpace) {
        if (footnoteSpace > 0) {
            //lets add some space between footnote and end of text. and +1 to fix approximation problems
            footnoteSpace += getMargin() + 1;
        }
        ct.setSimpleColumn(getMargin(), getMargin() + footnoteSpace, document.getPageSize().getRight(getMargin()), document.getPageSize().getTop(getMargin()));
    }

    public void applyToFootnoteColumn(ColumnText footnoteColumn, Document document, float reservedFootnoteSpace) {
        footnoteColumn.setSimpleColumn(getMargin(), getMargin(), document.getPageSize().getRight(getMargin()), getMargin() + reservedFootnoteSpace + 1);
        footnoteColumn.setSpaceCharRatio(30f);
    }

    public float getFootnotePositionLimit(float footnoteHeight) {
        return footnoteHeight + getMargin() * 2;
    }

    public void applyToDocument(Document pdf) {
        applyToDocument(pdf, settings.getMarginPoints());
    }

    public void applyToDocument(Document pdf, float footnoteSpace) {
        pdf.setPageSize(new Rectangle(settings.getPageWidthPoints(), settings.getPageHeightPoints()));
        pdf.setMargins(settings.getMarginPoints(), settings.getMarginPoints(), settings.getMarginPoints(), footnoteSpace);
    }

    public void applyToFullPageImage(Image image) {
        image.setAlignment(Element.ALIGN_CENTER);
        image.scaleToFit(settings.getPageWidthPoints() - settings.getMarginPoints() * 2, settings.getPageHeightPoints() - settings.getMarginPoints() * 2);
    }

    private boolean isInsideAnnotation(ProcessingContext context) {
        return context.stackHas(AnnotationType.class);
    }

    private boolean isInsideTitle(ProcessingContext context) {
        return context.stackHas(TitleType.class);
    }

    private boolean isInsideEpigraph(ProcessingContext context) {
        return context.stackHas(EpigraphType.class);
    }

    private boolean isInsideCite(ProcessingContext context) {
        return context.stackHas(CiteType.class);
    }


    public Paragraph getNewFootnoteParagraph() {
        Paragraph paragraph = new Paragraph();
        Fonts.FontBuilder fontBuilder = fonts.new FontBuilder();
        paragraph.setLeading(fontBuilder.small().get().getSize());
        paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
        paragraph.setFirstLineIndent(fontBuilder.getFirstLineIndent());
        return paragraph;
    }

    public Chunk getNewFootnoteChunk(String string) {
        Chunk chunk = new Chunk(string);
        Fonts.FontBuilder fontBuilder = fonts.new FontBuilder();
        chunk.setFont(fontBuilder.small().get());
        chunk.setHyphenation(hyphenationAuto);
        return chunk;
    }

    public Chunk getNewFootnoteLabelChunk(String text) {
        Chunk chunk = new Chunk(text);
        Fonts.FontBuilder fontBuilder = fonts.new FontBuilder();
        chunk.setTextRise(1);
        chunk.setFont(fontBuilder.toosmall().italic().get());
        return chunk;
    }

    public float getMargin() {
        return settings.getMarginPoints();
    }

    public void drawFootnotLine(PdfContentByte directContent, Document document, float yline) {
        directContent.setLineWidth(.2f);
        directContent.moveTo(getMargin(), yline);
        directContent.lineTo(document.getPageSize().getRight(getMargin()) / 3, yline);
        directContent.stroke();
    }

    public float getMinimumFootnoteTop(float reservedFootnoteSpace) {
        return getFootnotePositionLimit(reservedFootnoteSpace) + getFootnoteFont().getLeading() * 2;
    }

    private Fonts.FontBuilder getFootnoteFont() {
        return fonts.new FontBuilder().italic().small();
    }

    public float[] sliceFootnote(float bottom, float height, float reservedFootnoteSpace) {

        float first = bottom - reservedFootnoteSpace - getMargin() * 2 - 2; // todo: wtf?
        float second = height - first + (getFootnoteFont().getLeading());

        return new float[]{first, second};

    }

    public void applyToDummyParagraph(Paragraph pendingParagraph, ProcessingContext context) {

        pendingParagraph.setLeading(fonts.new FontBuilder().normal().getLeading() * .5f);
        pendingParagraph.add(new Chunk("\n"));

    }
}
