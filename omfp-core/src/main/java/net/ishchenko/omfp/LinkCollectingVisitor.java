package net.ishchenko.omfp;

import net.ishchenko.omfp.model.FictionBook;
import net.ishchenko.omfp.model.SectionType;


/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 26.02.2010
 * Time: 12:48:53
 */
public class LinkCollectingVisitor extends FictionBookVisitorAdapter {

    private ProcessingContext context;

    public LinkCollectingVisitor(ProcessingContext context) {
        this.context = context;
    }

    @Override
    public void visit(SectionType sectionType) {
        if (sectionType.getId() != null) {
            context.getLinks().put(sectionType.getId(), sectionType);
        }
    }

    @Override
    public void visit(FictionBook.Binary element) {
        context.getBinaries().put(element.getId(), element);
    }

}
