package net.ishchenko.omfp.pdf;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 19.03.2010
 * Time: 20:55:54
 */
public class DummyPdfHelper {

    /**
     * Invokes <code>runnable</code> callback inside a document with <code>style</code> with no output
     *
     * @param runnable callback
     * @param style    style for dummy document
     */
    public static void invokeInDocument(DummyPdfRunnable runnable, Style style) {
        invokeInDocument(runnable, style, null);
    }

    /**
     * Invokes <code>runnable</code> callback inside a document with <code>style</code>
     *
     * @param runnable callback
     * @param style    style for dummy document
     * @param filename name for output file or null if no output needed
     */
    public static void invokeInDocument(DummyPdfRunnable runnable, Style style, String filename) {

        Document document = style.getNewStyledDocument();
        try {
            //todo: figure out why test breaks when filename is set
            OutputStream outputStream = filename != null ?
                    new FileOutputStream(filename) :
                    new OutputStream() {
                        @Override
                        public void write(int b) throws IOException {
                        }
                    };

            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            runnable.run(document, writer, style);
            document.add(new Chunk("!!!")); //just to have some data inside 

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            document.close();
        }
    }


    public static interface DummyPdfRunnable {
        void run(Document document, PdfWriter writer, Style style) throws DocumentException;
    }
}
