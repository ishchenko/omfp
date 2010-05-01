package net.ishchenko.omfp.txt;

import net.ishchenko.omfp.FictionBookVisitorAdapter;
import net.ishchenko.omfp.FictionBookWalker;
import net.ishchenko.omfp.ProcessingContext;
import net.ishchenko.omfp.model.*;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 10.03.2010
 * Time: 12:52:17
 */
public class TxtPrintingVisitor extends FictionBookVisitorAdapter {

    private ProcessingContext context;

    private OutputStream output;

    private String separator = System.getProperty("line.separator");

    private Set<SectionType> usedSections = new HashSet<SectionType>();

    public TxtPrintingVisitor(OutputStream output) {
        this.output = output;
    }

    @Override
    public void init(ProcessingContext context) {
        this.context = context;
    }

    @Override
    public void commit() {

        try {
            this.output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void visit(String s) {

        if (context.stackHas(LinkType.class)) {
            //this is a link body, like [1] or something. let's skip it.
            return;
        }

        easyWrite(s);

    }

    @Override
    public void visit(PType paragraph) {
        easyWrite(separator + "  ");
    }

    @Override
    public void visitEmptyLine() {
        easyWrite(separator);
    }

    @Override
    public void visit(SectionType element) {
        easyWrite(separator);
    }

    @Override
    public void visit(FictionBook.Body element) {
        easyWrite(separator);
    }

    @Override
    public void visit(LinkType link) {
        String href = link.getHref();
        if (href.length() > 0) {

            SectionType linkedSection = context.getLinks().get(href.substring(1));
            if (linkedSection != null) {

                final StringBuilder linkBody = new StringBuilder("[[");
                ProcessingContext miniContext = new ProcessingContext();
                LinkedSectionContentGrabber sectionGrabber = new LinkedSectionContentGrabber(linkBody, miniContext);
                new FictionBookWalker(sectionGrabber, miniContext).visitSection(linkedSection);
                linkBody.append("]]");

                easyWrite(linkBody.toString());
                usedSections.add(linkedSection);

            }

        }
    }


    private boolean isInSkippedSection() {
        SectionType section = context.getTopmost(SectionType.class);
        return section != null && usedSections.contains(section);
    }


    private void easyWrite(String s) {
        if (!isInSkippedSection()) {
            try {
                output.write(s.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class LinkedSectionContentGrabber extends FictionBookVisitorAdapter {

        private ProcessingContext context;
        private StringBuilder buffer;

        private LinkedSectionContentGrabber(StringBuilder buffer, ProcessingContext context) {
            this.buffer = buffer;
            this.context = context;
        }

        @Override
        public void visit(String s) {
            if (!context.stackHas(TitleType.class)) {
                buffer.append(s);
            } else {
                buffer.append(s).append("* ");
            }
        }

    }


}
