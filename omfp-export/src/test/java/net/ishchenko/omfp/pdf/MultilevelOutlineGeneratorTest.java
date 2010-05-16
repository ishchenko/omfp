package net.ishchenko.omfp.pdf;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfOutline;
import com.itextpdf.text.pdf.PdfWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 05.04.2010
 * Time: 16:07:41
 */

public class MultilevelOutlineGeneratorTest {

    private OutlineGenerator generator;

    private Chapter[] chapters = new Chapter[]{
            new Chapter("1.1"),
            new Chapter("1.2", new Chapter[]{
                    new Chapter("1.2.1")
            }),
            new Chapter("1.3", new Chapter[]{
                    new Chapter("1.3.1"),
                    new Chapter("1.3.2", new Chapter[]{
                            new Chapter("1.3.2.1"),
                            new Chapter("1.3.2.2")
                    })
            }),
            new Chapter("1.4", new Chapter[]{
                    new Chapter("1.4.1")
            })

    };

    @Test
    public void testMultilevelOutlineGeneration() throws IOException, DocumentException, NoSuchFieldException, IllegalAccessException {

        final PdfSettings settings = new PdfSettings.Builder().
                multiLevelOutline(true).
                //test basedir is module root
                baseFontPath("../target/resources/styles/LiberationSerif-Regular.ttf").
                build();


        writeDocument(settings);

        PdfOutline root = generator.getOutlines().get(0);
        ArrayList<PdfOutline> rootKids = root.getKids();

        Assert.assertEquals(rootKids.size(), 4);

        PdfOutline kid_11 = rootKids.get(0);
        Assert.assertEquals(kid_11.getTitle(), "1.1");
        Assert.assertEquals(kid_11.getKids().size(), 0);

        PdfOutline kid_12 = rootKids.get(1);
        Assert.assertEquals(kid_12.getTitle(), "1.2");
        Assert.assertEquals(kid_12.getKids().size(), 1);
        Assert.assertEquals(kid_12.getKids().get(0).getTitle(), "1.2.1");


        PdfOutline kid_13 = rootKids.get(2);
        Assert.assertEquals(kid_13.getTitle(), "1.3");
        Assert.assertEquals(kid_13.getKids().size(), 2);
        Assert.assertEquals(kid_13.getKids().get(0).getTitle(), "1.3.1");
        Assert.assertEquals(kid_13.getKids().get(1).getTitle(), "1.3.2");
        Assert.assertEquals(kid_13.getKids().get(1).getKids().size(), 2);
        Assert.assertEquals(kid_13.getKids().get(1).getKids().get(0).getTitle(), "1.3.2.1");
        Assert.assertEquals(kid_13.getKids().get(1).getKids().get(1).getTitle(), "1.3.2.2");

        PdfOutline kid_14 = rootKids.get(3);
        Assert.assertEquals(kid_14.getTitle(), "1.4");
        Assert.assertEquals(kid_14.getKids().size(), 1);
        Assert.assertEquals(kid_14.getKids().get(0).getTitle(), "1.4.1");

    }

    private void writeDocument(final PdfSettings settings) throws IOException, DocumentException {

        DummyPdfHelper.invokeInDocument(new DummyPdfHelper.DummyPdfRunnable() {
            public void run(Document doc, PdfWriter writer, Style style) throws DocumentException {

                generator = new OutlineGenerator(writer.getRootOutline(), settings);
                writer.setPageEvent(generator);

                doc.add(new Paragraph("Test Document"));

                for (Chapter chapter : chapters) {
                    insertChapterPointer(chapter, doc, 1);
                }

            }
        }, new Style(settings));
    }

    private void insertChapterPointer(Chapter chapter, Document doc, int level) throws DocumentException {

        doc.add(getBookmarkChunk("Bookmark chunk + " + chapter.title, level, chapter.title));
        doc.add(Chunk.NEXTPAGE);
        for (Chapter childChapter : chapter.chapters) {
            insertChapterPointer(childChapter, doc, level + 1);
        }

    }


    private Chunk getBookmarkChunk(String text, int level, String title) {
        Chunk chunk = new Chunk(text);
        chunk.setGenericTag(OutlineGenerator.encode(level, title));
        return chunk;
    }

    private static class Chapter {
        String title;
        Chapter[] chapters;

        private Chapter(String title) {
            this(title, new Chapter[0]);
        }

        private Chapter(String title, Chapter[] chapters) {
            this.chapters = chapters;
            this.title = title;
        }
    }


}




