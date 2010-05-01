package net.ishchenko.omfp;

import net.ishchenko.omfp.model.*;


/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 26.02.2010
 * Time: 12:47:08
 */
public abstract class FictionBookVisitorAdapter implements FictionBookVisitor {
    public void visit(AlignType element) {
    }

    public void visit(AnnotationType element) {
    }

    public void visit(AuthorType element) {
    }

    public void visit(CiteType element) {
    }

    public void visit(DateType element) {
    }

    public void visit(EpigraphType element) {
    }

    public void visit(FictionBook element) {
    }

    public void visit(FictionBook.Binary element) {
    }

    public void visit(FictionBook.Body element) {
    }

    public void visit(FictionBook.Description element) {
    }

    public void visit(FictionBook.Stylesheet element) {
    }

    public void visit(ImageType element) {
    }

    public void visit(LinkType element) {
    }

    public void visit(NamedStyleType element) {
    }

    public void visit(PoemType element) {
    }

    public void visit(PoemType.Stanza element) {
    }

    public void visit(PType element) {
    }

    public void visit(SectionType element) {
    }

    public void visit(SequenceType element) {
    }

    public void visit(StyleLinkType element) {
    }

    public void visit(StyleType element) {
    }

    public void visit(TableType element) {
    }

    public void visit(TableType.Tr element) {
    }

    public void visit(TdType element) {
    }

    public void visit(TextFieldType element) {
    }

    public void visit(TitleType element) {
    }

    public void visit(String s) {
    }

    public void visitEmptyLine() {
    }

    public void visit(InlineImageType inlineImageType) {
    }

    public void init(ProcessingContext context) {
    }

    public void commit() {
    }

    public void leaveTable(TableType tableType) {
    }

    public void leaveTr(TableType.Tr tableType) {
    }

    public void leaveTd(TdType tableType) {
    }
}
