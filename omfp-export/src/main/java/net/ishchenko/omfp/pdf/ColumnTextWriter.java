package net.ishchenko.omfp.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import net.ishchenko.omfp.FictionBookVisitor;
import net.ishchenko.omfp.FictionBookWalker;
import net.ishchenko.omfp.ProcessingContext;

import net.ishchenko.omfp.FictionBookVisitorAdapter;
import net.ishchenko.omfp.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 12.03.2010
 * Time: 13:09:01
 */
public class ColumnTextWriter {

    private Map<String, SectionType> linkSections;
    private List<Element> elements;
    private Style style;
    private FootnoteHeightCalculator footnoteHeightCalculator;
    private PdfSettings settings;

    public ColumnTextWriter(Map<String, SectionType> linkSections, List<Element> elements, Style style, PdfSettings settings) throws DocumentException {
        this.linkSections = linkSections;
        this.elements = elements;
        this.style = style;
        this.settings = settings;

        footnoteHeightCalculator = new FootnoteHeightCalculator(style);

    }

    public void writeRealDocument(OutputStream out) throws DocumentException, FileNotFoundException {

        PageFootnotes pageFootnotes = new PageFootnotes();

        Document document = style.getNewStyledDocument();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        writer.setPageEvent(new OutlineGenerator(writer.getRootOutline(), settings));

        ColumnText mainColumn = new ColumnText(writer.getDirectContent());
        style.applyToMainColumn(mainColumn, document);

        ColumnText footnoteColumn = new ColumnText(writer.getDirectContent());

        for (Element element : elements) {

            if (element == Chunk.NEXTPAGE) {
                newPage(mainColumn, document, writer, pageFootnotes);
                continue;
            }

            mainColumn.addElement(element);

            if (element instanceof Paragraph) {
                handleFootnotesInParagraph((Paragraph) element, pageFootnotes, mainColumn.getYLine(), writer.getPageNumber());
                shrinkColumn(mainColumn, document, pageFootnotes, writer.getCurrentPageNumber());
            }

            int status = ColumnText.START_COLUMN;
            while (ColumnText.hasMoreText(status)) {

                status = mainColumn.go();

                if ((status & ColumnText.NO_MORE_COLUMN) != 0) {

                    printFootnotes(document, pageFootnotes.getForPage(writer.getCurrentPageNumber()), footnoteColumn, writer.getDirectContent());
                    newPage(mainColumn, document, writer, pageFootnotes);

                }
            }
        }

        document.close();

        footnoteHeightCalculator.cleanup();

    }

    private void newPage(ColumnText mainColumn, Document document, PdfWriter writer, PageFootnotes pageFootnotes) {
        document.newPage();
        style.applyToMainColumn(mainColumn, document, pageFootnotes.getForPage(writer.getCurrentPageNumber()).reservedFootnoteSpace);
    }


    private void shrinkColumn(ColumnText mainColumn, Document document, PageFootnotes pageFootnotes, int currentPageNumber) {
        float yline = mainColumn.getYLine();
        style.applyToMainColumn(mainColumn, document, pageFootnotes.getForPage(currentPageNumber).reservedFootnoteSpace);
        mainColumn.setYLine(yline);
    }

    private void printFootnotes(Document document, MarginsAndFootnotesForPage marginsAndFootnotesForPage, ColumnText footnoteColumn, PdfContentByte directContent) throws DocumentException {

        //all margins are correctly set already.

        style.applyToFootnoteColumn(footnoteColumn, document, marginsAndFootnotesForPage.reservedFootnoteSpace);

        for (Paragraph footnote : marginsAndFootnotesForPage.footnoteBodies) {
            footnoteColumn.addElement(footnote);
        }

        if (marginsAndFootnotesForPage.reservedFootnoteSpace > 0) {
            //flush the rest of previous footnote (if any) and flush what was added now (if any)
            float yline = footnoteColumn.getYLine();
            footnoteColumn.go();
            style.drawFootnotLine(directContent, document, yline);
        }

    }

    private void handleFootnotesInParagraph(Paragraph paragraph, PageFootnotes pageFootnotes, float yLine, int initialPageNumber) throws DocumentException, FileNotFoundException {

        List<Footnote> footnotesInParagraph = findFootnotesInParagraph(paragraph);

        for (int i = 0; i < footnotesInParagraph.size(); i++) {

            final Footnote footnote = footnotesInParagraph.get(i);
            MarginsAndFootnotesForPage footnoteForThisPage = pageFootnotes.getForPage(initialPageNumber);

            Document tempDocument = style.getNewStyledDocument();
            PdfWriter tempWriter = PdfWriter.getInstance(tempDocument, new DummyOutputStream());
            FootnoteCatcher footnoteCatcher = new FootnoteCatcher(i, pageFootnotes, initialPageNumber, footnote);
            tempWriter.setPageEvent(footnoteCatcher);

            tempDocument.open();
            ColumnText tempColumn = new ColumnText(tempWriter.getDirectContent());
            style.applyToMainColumn(tempColumn, tempDocument, footnoteForThisPage.reservedFootnoteSpace);
            tempColumn.setYLine(yLine);

            tempColumn.addElement(paragraph);

            int pageShift = 0;
            int status = ColumnText.START_COLUMN;
            while (ColumnText.hasMoreText(status)) {
                status = tempColumn.go();
                if ((status & ColumnText.NO_MORE_COLUMN) != 0) {
                    tempDocument.newPage();
                    pageShift++;
                    int currentPageNumber = initialPageNumber + pageShift;
                    footnoteCatcher.setCurrentPageNumber(currentPageNumber);
                    style.applyToMainColumn(tempColumn, tempDocument, pageFootnotes.getForPage(currentPageNumber).reservedFootnoteSpace);
                }
            }

        }


    }

    private List<Footnote> findFootnotesInParagraph(Paragraph paragraph) throws DocumentException {
        List<Footnote> result = new ArrayList<Footnote>();
        for (Chunk chunk : paragraph.getChunks()) {
            if (chunk.getAttributes() != null) {
                String genericAttributeValue = (String) chunk.getAttributes().get(Chunk.GENERICTAG);
                if (genericAttributeValue != null && genericAttributeValue.startsWith(PdfPrintingVisitor.GENERIC_TAG_LINK)) {
                    String id = genericAttributeValue.substring(PdfPrintingVisitor.GENERIC_TAG_LINK.length());
                    result.add(new Footnote(id, footnoteHeightCalculator.calculateFootnoteHeight(getLinkBody(id))));
                }
            }
        }
        return result;
    }

    private Paragraph getLinkBody(String href) {

        final Paragraph footnoteParagraph = style.getNewFootnoteParagraph();

        final ProcessingContext miniContext = new ProcessingContext();
        FictionBookVisitor sectionGrabber = new FictionBookVisitorAdapter() {
            @Override
            public void visit(String string) {
                if (!miniContext.stackHas(TitleType.class)) {
                    Chunk chunk = style.getNewFootnoteChunk(string);
                    footnoteParagraph.add(chunk);
                } else {
                    footnoteParagraph.add(style.getNewFootnoteLabelChunk(string));
                    footnoteParagraph.add(style.getNewFootnoteChunk(" "));
                }
            }
        };
        new FictionBookWalker(sectionGrabber, miniContext).visitSection(linkSections.get(href));

        return footnoteParagraph;
    }


    private class Footnote {
        String id;
        float height;

        private Footnote(String id, float height) {
            this.id = id;
            this.height = height;
        }
    }

    private static class DummyOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
        }
    }

    private class MarginsAndFootnotesForPage {
        float reservedFootnoteSpace;
        List<Paragraph> footnoteBodies = new ArrayList<Paragraph>();
    }

    private class PageFootnotes {

        Map<Integer, MarginsAndFootnotesForPage> footnotes = new TreeMap<Integer, MarginsAndFootnotesForPage>();
        MarginsAndFootnotesForPage NO_FOOTNOTE = new MarginsAndFootnotesForPage();

        MarginsAndFootnotesForPage getForPage(int pageNumber) {
            if (footnotes.get(pageNumber) != null) {
                return footnotes.get(pageNumber);
            } else {
                return NO_FOOTNOTE;
            }
        }

        void addMarginAndFootnote(int pageNumber, float height, Paragraph body) {
            MarginsAndFootnotesForPage footnote = footnotes.get(pageNumber);
            if (footnote == null) {
                footnote = new MarginsAndFootnotesForPage();
            }
            footnote.reservedFootnoteSpace += height;
            footnote.footnoteBodies.add(body);
            footnotes.put(pageNumber, footnote);
        }

        void addMarginOnly(int pageNumber, float height) {
            MarginsAndFootnotesForPage footnote = footnotes.get(pageNumber);
            if (footnote == null) {
                footnote = new MarginsAndFootnotesForPage();
            }
            footnote.reservedFootnoteSpace += height;
            footnotes.put(pageNumber, footnote);
        }
    }

    private class FootnoteCatcher extends PdfPageEventHelper {

        private int numberOfFootnotes;
        private int footnoteIndex;
        private PageFootnotes pageFootnotes;

        private Footnote footnote;
        private int currentPageNumber;

        FootnoteCatcher(int footnoteIndex, PageFootnotes pageFootnotes, int currentPageNumber, Footnote footnote) {
            this.footnoteIndex = footnoteIndex;
            this.pageFootnotes = pageFootnotes;
            this.currentPageNumber = currentPageNumber;
            this.footnote = footnote;
        }

        public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, final String text) {

            if (!text.startsWith(PdfPrintingVisitor.GENERIC_TAG_LINK)) {
                return;
            }

            if (numberOfFootnotes == footnoteIndex) {

                final MarginsAndFootnotesForPage currentFootnoteForPage = pageFootnotes.getForPage(currentPageNumber);
                float theoreticalFootnoteTop = style.getFootnotePositionLimit(currentFootnoteForPage.reservedFootnoteSpace + footnote.height);

                final Paragraph footnoteBody = getLinkBody(footnote.id);
                if (rect.getBottom() > theoreticalFootnoteTop) {
                    //footnote fits! yay!
                    pageFootnotes.addMarginAndFootnote(currentPageNumber, footnote.height, footnoteBody);

                } else {

                    if (rect.getBottom() > style.getMinimumFootnoteTop(currentFootnoteForPage.reservedFootnoteSpace)) {

                        //footnote body is too high. splitting it.\
                        float[] heights = style.sliceFootnote(rect.getBottom(), footnote.height, currentFootnoteForPage.reservedFootnoteSpace);
                        float first = heights[0];
                        float second = heights[1];
                        pageFootnotes.addMarginAndFootnote(currentPageNumber, first, footnoteBody);
                        pageFootnotes.addMarginOnly(currentPageNumber + 1, second);

                    } else {
                        //footnote body is too high. moving to next page 
                        pageFootnotes.addMarginAndFootnote(currentPageNumber + 1, footnote.height, footnoteBody);
                    }

                }
            }

            numberOfFootnotes++;

        }

        public void setCurrentPageNumber(int currentPageNumber) {
            this.currentPageNumber = currentPageNumber;
        }
    }


}
