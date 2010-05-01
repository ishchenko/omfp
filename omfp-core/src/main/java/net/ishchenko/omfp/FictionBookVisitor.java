package net.ishchenko.omfp;

import net.ishchenko.omfp.model.*;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 26.02.2010
 * Time: 12:40:56
 */
public interface FictionBookVisitor {

    void visit(AlignType element);
    void visit(AnnotationType element);
    void visit(AuthorType element);
    void visit(CiteType element);
    void visit(DateType element);
    void visit(EpigraphType element);
    void visit(FictionBook element);
    void visit(FictionBook.Binary element);
    void visit(FictionBook.Body element);
    void visit(FictionBook.Description element);
    void visit(FictionBook.Stylesheet element);
    void visit(ImageType element);
    void visit(LinkType element);
    void visit(NamedStyleType element);
    void visit(PoemType element);
    void visit(PoemType.Stanza element);
    void visit(PType element);
    void visit(SectionType element);
    void visit(SequenceType element);
    void visit(StyleLinkType element);
    void visit(StyleType element);
    void visit(TableType element);
    void visit(TableType.Tr element);
    void visit(TdType element);
    void visit(TextFieldType element);
    void visit(TitleType element);
    void visit(String s);

    void visitEmptyLine();

    void init(ProcessingContext context);
    void commit();

    void visit(InlineImageType inlineImageType);

    void leaveTable(TableType tableType);
    void leaveTr(TableType.Tr tableType);
    void leaveTd(TdType tableType);
}
