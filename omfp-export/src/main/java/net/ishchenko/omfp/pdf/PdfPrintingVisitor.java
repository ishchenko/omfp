package net.ishchenko.omfp.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import net.ishchenko.omfp.FictionBookVisitor;
import net.ishchenko.omfp.FictionBookVisitorAdapter;
import net.ishchenko.omfp.FictionBookWalker;
import net.ishchenko.omfp.ProcessingContext;
import net.ishchenko.omfp.model.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 26.02.2010
 * Time: 12:50:55
 */
public class PdfPrintingVisitor extends FictionBookVisitorAdapter {

    public static final String GENERIC_TAG_LINK = "link://";

    private PdfSettings settings;

    private Style style;
    private OutputStream output;
    private ProcessingContext context;

    private Paragraph pendingParagraph;
    private PdfPTable currentTable;
    private PdfPCell currentTd;

    java.util.List<Element> elements = new ArrayList<Element>();
    private Map<String, SectionType> linkSections = new HashMap<String, SectionType>();


    public PdfPrintingVisitor(OutputStream output, PdfSettings settings) {
        this.output = output;
        this.settings = settings;
    }

    @Override
    public void init(ProcessingContext context) {

        this.context = context;

        try {

            style = new Style(settings);

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commit() {

        commitPendingParagraph();

        try {

            new ColumnTextWriter(linkSections, elements, style, settings).writeRealDocument(output);

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void visit(FictionBook.Description description) {

        TitleInfoType titleInfoType = description.getTitleInfo();
        if (titleInfoType != null) {
            TitleInfoType.Coverpage coverpage = titleInfoType.getCoverpage();
            if (coverpage != null) {
                for (InlineImageType imageType : coverpage.getImages()) {
                    drawFullPageImage(imageType);
                }
            }
        }

    }


    @Override
    public void visit(SectionType section) {

        if (isInSkippedSection()) {
            return;
        }

        if (context.countNestingOnStack(SectionType.class) == 1) {
            commitPendingParagraph();
            elements.add(Chunk.NEXTPAGE);
            startNewParagraph();
        }

        startNewParagraph();

    }

    @Override
    public void visit(ImageType element) {
        try {
            Image image = getImageFromHref(element.getHref());

            float dpi = 182;

            float imgWidth = image.getWidth() / dpi * 72;
            float imgHeight = image.getHeight() / dpi * 72;
            image.scaleToFit(imgWidth, imgHeight);
            image.setAlignment(Image.MIDDLE);

            commitPendingParagraph();
            elements.add(image);
            startNewParagraph();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (BadElementException e) {
            throw new RuntimeException(e);
        }

    }

    private void drawFullPageImage(InlineImageType imageType) {

        try {
            Image image = getImageFromHref(imageType.getHref());
            style.applyToFullPageImage(image);

            commitPendingParagraph();
            elements.add(image);
            elements.add(Chunk.NEXTPAGE);
            startNewParagraph();

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Image getImageFromHref(String href) throws IOException, BadElementException {
        if (href == null) {
            throw new IllegalArgumentException("href must not be null");
        }
        if (href.toLowerCase().endsWith(".jpg") || href.toLowerCase().endsWith(".jpeg")){
            return new Jpeg(context.getBinaries().get(href.substring(1)).getValue());
        }else if (href.toLowerCase().endsWith(".png")) {
            return Image.getInstance(context.getBinaries().get(href.substring(1)).getValue());
        }

        throw new IllegalArgumentException("Bad image extension " + href);
    }

    @Override
    public void visit(PType paragraph) {

        if (isInSkippedSection()) {
            return;
        }

        startNewParagraph();

        if (context.stackHas(CiteType.class) && context.getIteration().isFirst()) {
            //fix for AWFUL getSpacingBefore behavior
            //http://www.mail-archive.com/itext-questions@lists.sourceforge.net/msg31154.html
            style.applyToDummyParagraph(pendingParagraph, context);
            startNewParagraph();
        }
        style.applyToParagraph(pendingParagraph, context);

        FictionBookWalker.PolyType type = context.getTopmost(FictionBookWalker.PolyType.class);
        if (type != null && type.getName().equals("subtitle")) {
            pendingParagraph.add(Chunk.NEWLINE);
        }

    }

    @Override
    public void visit(EpigraphType element) {

        startNewParagraph();
        pendingParagraph.add(Chunk.NEWLINE);

    }


    @Override
    public void visit(String s) {

        if (isInSkippedSection() || context.stackHas(LinkType.class)) {
            //this is a link body, like [1] or something. let's skip it.
            //todo: handle non-internal links
            return;
        }

        Chunk chunk = style.getNewChunk(context);

        chunk.append(s);

        int level = context.countNestingOnStack(SectionType.class);
        if (context.stackHas(TitleType.class) && level > 0) {

            Chunk bookmarkChunk = style.getNewChunk(context);
            bookmarkChunk.append(" ");
            bookmarkChunk.setGenericTag(OutlineGenerator.encode(level, s));
            pendingParagraph.add(bookmarkChunk);

            pendingParagraph.add(chunk);

            //some justification
            Chunk bookmarkChunk2 = style.getNewChunk(context);
            bookmarkChunk2.append(" ");
            pendingParagraph.add(bookmarkChunk2);

        } else {

            pendingParagraph.add(chunk);

        }

    }

    @Override
    public void visit(LinkType link) {

        String fullHref = link.getHref();
        if (fullHref.length() > 0) {

            String href = fullHref.substring(1);

            SectionType linkedSection = context.getLinks().get(href);
            if (linkedSection != null) {

                //link section title
                final StringBuilder linkTitle = new StringBuilder();
                final ProcessingContext miniContext = new ProcessingContext();
                FictionBookVisitor linkTitleGrabber = new FictionBookVisitorAdapter() {
                    @Override
                    public void visit(String s) {
                        if (miniContext.stackHas(TitleType.class)) {
                            linkTitle.append(s);
                        }
                    }
                };
                new FictionBookWalker(linkTitleGrabber, miniContext).visitSection(linkedSection);

                Chunk footnoteChunk = style.getNewFootnoteLabelChunk(linkTitle.toString());
                footnoteChunk.setGenericTag(GENERIC_TAG_LINK + href);
                pendingParagraph.add(footnoteChunk);

                linkSections.put(href, linkedSection);

            }
        }

    }

    @Override
    public void visit(TableType element) {

        commitPendingParagraph();

        if (currentTable != null) {
            //todo
            throw new IllegalStateException("Tables inside tables are not supported :(");
        }
        if (element.getTrs().size() == 0) {
            throw new IllegalArgumentException("Table has 0 rows");
        }

        TableType.Tr firstRow = element.getTrs().get(0);
        final int[] columns = new int[1];
        final ProcessingContext miniContext = new ProcessingContext();
        FictionBookVisitor columnCounter = new FictionBookVisitorAdapter() {
            @Override
            public void visit(TdType element) {
                if (miniContext.countNestingOnStack(TableType.Tr.class) == 1) { //not counting inner tables tr
                    columns[0]++;
                }
            }
        };
        new FictionBookWalker(columnCounter, miniContext).visitTr(firstRow);

        currentTable = new PdfPTable(columns[0]);

    }

    @Override
    public void visit(TdType element) {
        if (currentTd != null) {
            throw new IllegalStateException();
        }
        currentTd = new PdfPCell();
        startNewParagraph();
        style.applyToParagraph(pendingParagraph, context);
    }

    @Override
    public void leaveTable(TableType tableType) {
        elements.add(currentTable);
        currentTable = null;
        startNewParagraph();
    }

    @Override
    public void leaveTd(TdType tableType) {
        commitPendingParagraph();
        currentTable.addCell(currentTd);
        currentTd = null;
    }

    @Override
    public void visitEmptyLine() {

        if (isInSkippedSection()) {
            return;
        }

        startNewParagraph();
        style.applyToParagraph(pendingParagraph, context);
        pendingParagraph.add(Chunk.NEWLINE);

    }

    private void startNewParagraph() {
        commitPendingParagraph();
        pendingParagraph = new Paragraph();
    }

    private void commitPendingParagraph() {
        if (pendingParagraph != null) {
            if (currentTd != null) {
                currentTd.addElement(pendingParagraph);
            } else {
                elements.add(pendingParagraph);
            }
            pendingParagraph = null;
        }
    }

    private boolean isInSkippedSection() {
        SectionType section = context.getTopmost(SectionType.class);
        return section != null && linkSections.containsValue(section);
    }


}





