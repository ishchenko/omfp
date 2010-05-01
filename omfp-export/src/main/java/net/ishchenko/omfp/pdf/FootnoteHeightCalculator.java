package net.ishchenko.omfp.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.OutputStream;

public class FootnoteHeightCalculator {

    private Style style;
    private Document heightCalculationDocument;
    private PdfContentByte cb;

    public FootnoteHeightCalculator(Style style) throws DocumentException {

        this.style = style;

        heightCalculationDocument = style.getNewStyledDocument();
        
        PdfWriter marginCalculationWriter = PdfWriter.getInstance(heightCalculationDocument, new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        });

        heightCalculationDocument.open();
        heightCalculationDocument.newPage();
                                                                                
        cb = marginCalculationWriter.getDirectContent();

    }

    public float calculateFootnoteHeight(Paragraph footnote) throws DocumentException {

        ColumnText ct = new ColumnText(cb);
        style.applyToMainColumn(ct, heightCalculationDocument);
        
        ct.addElement(footnote);

        float paragraphStart = ct.getYLine();
        if ((ct.go() & ColumnText.NO_MORE_COLUMN) != 0) {
            throw new RuntimeException("Too big footnote! " + footnote.getContent().substring(0, 50) + "...");
        }
        float paragraphHeight = paragraphStart - ct.getYLine();

        drawHint(ct, paragraphHeight);
        drawRectangle(paragraphStart, paragraphHeight);

        heightCalculationDocument.newPage();

        return paragraphHeight;
    }

    private void drawHint(ColumnText ct, float paragraphHeight) throws DocumentException {
        ct.addElement(new Paragraph("Height: " + paragraphHeight));
        ct.go();
    }

    private void drawRectangle(float paragraphStart, float paragraphHeight) {
        Rectangle rectangle = new Rectangle(10, paragraphStart - paragraphHeight, 20, paragraphStart);
        rectangle.setBorder(Rectangle.BOTTOM | Rectangle.RIGHT | Rectangle.LEFT | Rectangle.TOP);
        rectangle.setBorderWidth(.2f);
        cb.rectangle(rectangle);
    }

    public void cleanup() throws DocumentException {

        heightCalculationDocument.newPage();
        heightCalculationDocument.add(new Chunk("Yay!"));
        heightCalculationDocument.close();

    }
}