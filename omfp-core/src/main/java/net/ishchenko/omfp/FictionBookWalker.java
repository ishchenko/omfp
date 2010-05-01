package net.ishchenko.omfp;

import net.ishchenko.omfp.model.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 02.03.2010
 * Time: 15:23:55
 */
public class FictionBookWalker {

    private FictionBookVisitor visitor;

    private ProcessingContext context;

    public static enum Hints {
        SUBTITLE, AUTHOR
    }

    public static class PolyType {

        private String name;

        public PolyType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    public FictionBookWalker(FictionBookVisitor visitor, ProcessingContext context) {
        this.visitor = visitor;
        this.context = context;
    }

    public void visitBook(FictionBook book) {

        context.pushToStack(book);

        visitor.visit(book);

        context.getIteration().iterate(book.getStylesheets(), new Procedure<FictionBook.Stylesheet>() {
            public void go(FictionBook.Stylesheet element) {
                context.pushToStack(element);
                visitor.visit(element);
                context.popFromStack();
            }
        });

        visitDescription(book.getDescription());

        context.getIteration().iterate(book.getBodies(), new Procedure<FictionBook.Body>() {
            public void go(FictionBook.Body element) {
                visitBody(element);
            }
        });

        context.getIteration().iterate(book.getBinaries(), new Procedure<FictionBook.Binary>() {
            public void go(FictionBook.Binary element) {
                context.pushToStack(element);
                visitor.visit(element);
                context.popFromStack();
            }
        });

        context.popFromStack();

    }

    private void visitDescription(FictionBook.Description description) {

        context.pushToStack(description);

        visitor.visit(description);

        TitleInfoType titleInfo = description.getTitleInfo();
        if (titleInfo != null) {
            if (titleInfo.getAnnotation() != null) {
                visitAnnotation(titleInfo.getAnnotation());
            }
        }

        context.popFromStack();
        
    }


    private void visitBody(FictionBook.Body body) {

        context.pushToStack(body);

        visitor.visit(body);

        if (body.getTitle() != null) {
            visitTitle(body.getTitle());
        }

        context.getIteration().iterate(body.getEpigraphs(), new Procedure<EpigraphType>() {
            public void go(EpigraphType element) {
                visitEpigraph(element);
            }
        });

        context.getIteration().iterate(body.getSections(), new Procedure<SectionType>() {
            public void go(SectionType element) {
                visitSection(element);
            }
        });

        context.popFromStack();

    }

    private void visitTitle(TitleType title) {

        context.pushToStack(title);

        visitor.visit(title);

        context.getIteration().iterate(title.getPSAndEmptyLines(), new Procedure<Object>() {
            public void go(Object element) {
                if (element instanceof PType) {
                    visitPtype((PType) element);
                } else {
                    visitObject(element);
                }
            }
        });

        context.popFromStack();

    }

    private void visitEpigraph(EpigraphType epigraph) {

        context.pushToStack(epigraph);

        visitor.visit(epigraph);

        context.getIteration().iterate(epigraph.getPSAndPoemsAndCites(), new Procedure<Object>() {
            public void go(Object element) {
                //PoemType CiteType PType Object
                if (element.getClass().equals(PoemType.class)) {
                    visitPoem((PoemType) element);
                } else if (element.getClass().equals(CiteType.class)) {
                    visitCite((CiteType) element);
                } else if (element.getClass().equals(PType.class)) {
                    visitPtype((PType) element);
                } else {
                    visitObject(element);
                }
            }
        });

        context.getIteration().iterate(epigraph.getTextAuthors(), new Procedure<PType>() {
            public void go(PType element) {
                context.pushToStack(Hints.AUTHOR);
                visitPtype(element);
                context.popFromStack();
            }
        });

        context.popFromStack();

    }

    public void visitSection(SectionType section) {

        context.pushToStack(section);

        visitor.visit(section);

        if (section.getTitle() != null) {
            visitTitle(section.getTitle());
        }

        context.getIteration().iterate(section.getEpigraphs(), new Procedure<EpigraphType>() {
            public void go(EpigraphType element) {
                visitEpigraph(element);
            }
        });

        context.getIteration().iterate(section.getImagesAndAnnotationsAndPS(), new Procedure<JAXBElement<?>>() {
            public void go(JAXBElement<?> element) {
                //     * {@link JAXBElement }{@code <}{@link PType }{@code >}
                //     * {@link JAXBElement }{@code <}{@link PoemType }{@code >}
                //     * {@link JAXBElement }{@code <}{@link AnnotationType }{@code >}
                //     * {@link JAXBElement }{@code <}{@link TableType }{@code >}
                //     * {@link JAXBElement }{@code <}{@link CiteType }{@code >}
                //     * {@link JAXBElement }{@code <}{@link ImageType }{@code >}
                //     * {@link JAXBElement }{@code <}{@link Object }{@code >}

                Object value = element.getValue();
                Class type = element.getDeclaredType();

                context.pushToStack(new PolyType(element.getName().getLocalPart()));

                if (type == PType.class) {

                    visitPtype((PType) value);

                } else if (type == PoemType.class) {

                    visitPoem((PoemType) value);

                } else if (type == AnnotationType.class) {

                    visitAnnotation((AnnotationType) value);

                } else if (type == TableType.class) {

                    visitTable((TableType) value);

                } else if (type == CiteType.class) {

                    visitCite((CiteType) value);

                } else if (type == ImageType.class) {

                    visitImage((ImageType) value);

                } else {

                    visitObject(value);

                }

                context.popFromStack();

            }
        });

        context.getIteration().iterate(section.getSections(), new Procedure<SectionType>() {
            public void go(SectionType element) {
                visitSection(element);
            }
        });

        context.popFromStack();

    }

    private void visitImage(ImageType imageType) {
        context.pushToStack(imageType);
        visitor.visit(imageType);
        context.popFromStack();
    }

    private void visitTable(TableType tableType) {

        context.pushToStack(tableType);

        visitor.visit(tableType);

        context.getIteration().iterate(tableType.getTrs(), new Procedure<TableType.Tr>() {
            public void go(TableType.Tr element) {
                visitTr(element);
            }
        });

        visitor.leaveTable(tableType);

        context.popFromStack();


    }

    public void visitTr(TableType.Tr element) {

        context.pushToStack(element);

        visitor.visit(element);

        context.getIteration().iterate(element.getThsAndTds(), new Procedure<JAXBElement<TdType>>() {
            public void go(JAXBElement<TdType> element) {
                visitTd(element.getValue());
            }
        });

        visitor.leaveTr(element);

        context.popFromStack();

    }

    private void visitTd(TdType element) {

        context.pushToStack(element);

        visitor.visit(element);

        visitCommonContent(element.getContent());

        visitor.leaveTd(element);

        context.popFromStack();
    }

    private void visitAnnotation(AnnotationType annotationType) {

        context.pushToStack(annotationType);

        visitor.visit(annotationType);

        context.getIteration().iterate(annotationType.getPSAndPoemsAndCites(), new Procedure<JAXBElement<?>>() {
            public void go(JAXBElement<?> element) {
                // JAXBElement <PoemType >
                // JAXBElement <CiteType >
                // JAXBElement <PType >
                // JAXBElement <TableType >
                // JAXBElement <Object >
                context.pushToStack(new PolyType(element.getName().getLocalPart()));

                if (element.getDeclaredType() == PType.class) {
                    visitPtype((PType) element.getValue());
                } else if (element.getDeclaredType() == CiteType.class) {
                    visitCite((CiteType) element.getValue());
                } else if (element.getDeclaredType() == PoemType.class) {
                    visitPoem((PoemType) element.getValue());
                } else if (element.getDeclaredType() == TableType.class) {
                    visitTable((TableType) element.getValue());
                } else {
                    visitObject(element.getValue());
                }

                context.popFromStack();

            }
        });

        context.popFromStack();
    }

    private void visitPoem(PoemType poemType) {

        context.pushToStack(poemType);

        visitor.visit(poemType);

        if (poemType.getTitle() != null) {
            visitTitle(poemType.getTitle());
        }

        context.getIteration().iterate(poemType.getEpigraphs(), new Procedure<EpigraphType>() {
            public void go(EpigraphType element) {
                visitEpigraph(element);
            }
        });

        context.getIteration().iterate(poemType.getTextAuthors(), new Procedure<PType>() {
            public void go(PType element) {
                visitPtype(element);
            }
        });

        context.getIteration().iterate(poemType.getSubtitlesAndStanzas(), new Procedure<Object>() {
            public void go(Object element) {
                //PoemType.Stanza PType
                if (element instanceof PType) {
                    visitPtype((PType) element);
                } else if (element instanceof PoemType.Stanza) {
                    visitStanza((PoemType.Stanza) element);
                } else {
                    throw new RuntimeException(element.toString());
                }
            }
        });

        context.popFromStack();
    }

    private void visitStanza(PoemType.Stanza element) {

        context.pushToStack(element);

        visitor.visit(element);

        if (element.getTitle() != null) {
            visitTitle(element.getTitle());
        }

        if (element.getSubtitle() != null) {
            context.pushToStack(Hints.SUBTITLE);
            visitPtype(element.getSubtitle());
            context.popFromStack();
        }

        context.getIteration().iterate(element.getVS(), new Procedure<PType>() {
            public void go(PType element) {
                visitPtype(element);
            }
        });

        context.popFromStack();

    }


    private void visitPtype(PType pType) {

        context.pushToStack(pType);

        visitor.visit(pType);

        visitCommonContent(pType.getContent());

        context.popFromStack();

    }

    private void visitStyle(StyleType style) {

        context.pushToStack(style);

        visitor.visit(style);

        visitCommonContent(style.getContent());

        context.popFromStack();

    }

    private void visitCommonContent(List<Serializable> content) {

        context.getIteration().iterate(content, new Procedure<Serializable>() {
            public void go(Serializable serializable) {
                // String
                // JAXBElement <LinkType >
                // JAXBElement <StyleType >
                // JAXBElement <InlineImageType >
                // JAXBElement <NamedStyleType >
                if (serializable instanceof String) {

                    visitor.visit((String) serializable);

                } else if (serializable instanceof JAXBElement) {

                    JAXBElement element = (JAXBElement) serializable;
                    Object value = element.getValue();

                    context.pushToStack(new PolyType(element.getName().getLocalPart()));

                    if (element.getDeclaredType() == StyleType.class) {

                        visitStyle((StyleType) value);

                    } else if (element.getDeclaredType() == LinkType.class) {

                        visitLinkType((LinkType) value);

                    } else if (element.getDeclaredType() == InlineImageType.class) {

                        visitInlineImage((InlineImageType) element.getValue());

                    } else if (element.getDeclaredType() == NamedStyleType.class) {

                        visitNamedStyleType((NamedStyleType) value);

                    } else {
                        throw new RuntimeException(serializable.toString());
                    }

                    context.popFromStack();

                } else {
                    throw new RuntimeException(serializable.toString());
                }

            }
        });
    }

    private void visitNamedStyleType(NamedStyleType value) {
        context.pushToStack(value);
        visitor.visit(value);
        context.popFromStack();
    }

    private void visitLinkType(LinkType link) {

        context.pushToStack(link);

        visitor.visit(link);

        iterateStringOrStyleLinkOrInlineImage(link.getContent());

        context.popFromStack();
    }

    private void visitStyleLink(StyleLinkType styleLinkType) {

        context.pushToStack(styleLinkType);

        visitor.visit(styleLinkType);

        iterateStringOrStyleLinkOrInlineImage(styleLinkType.getContent());

        context.popFromStack();

    }

    private void visitInlineImage(InlineImageType value) {
        context.pushToStack(value);
        visitor.visit(value);
        context.popFromStack();
    }

    private void visitObject(Object o) {
        if (o instanceof Element) {
            Element emptyLine = (Element) o;
            if (emptyLine.getNodeType() == Node.ELEMENT_NODE && emptyLine.getNodeName().equals("empty-line")) {
                visitor.visitEmptyLine();
                return;
            }
        }
        throw new RuntimeException("Don't know what to do with " + o);
    }

    private void visitCite(CiteType citeType) {

        context.pushToStack(citeType);

        visitor.visit(citeType);

        context.getIteration().iterate(citeType.getPSAndPoemsAndEmptyLines(), new Procedure<JAXBElement<?>>() {
            public void go(JAXBElement<?> element) {
                context.pushToStack(new PolyType(element.getName().getLocalPart()));
                if (element.getDeclaredType() == PType.class) {
                    visitPtype((PType) element.getValue());
                } else if (element.getDeclaredType() == TableType.class) {
                    visitTable((TableType) element.getValue());
                } else if (element.getDeclaredType() == PoemType.class) {
                    visitPoem((PoemType) element.getValue());
                } else {
                    visitObject(element.getValue());
                }
                context.popFromStack();
            }
        });

        context.getIteration().iterate(citeType.getTextAuthors(), new Procedure<PType>() {
            public void go(PType element) {
                context.pushToStack(Hints.AUTHOR);
                visitPtype(element);
                context.popFromStack();
            }
        });

        context.popFromStack();

    }

    private void iterateStringOrStyleLinkOrInlineImage(List<Serializable> content) {
        context.getIteration().iterate(content, new Procedure<Serializable>() {
            public void go(Serializable element) {
                //String
                //JAXBElement <StyleLinkType>
                //JAXBElement <InlineImageType>
                if (element instanceof String) {

                    visitor.visit((String) element);

                } else if (element instanceof JAXBElement) {

                    JAXBElement jaxbelement = (JAXBElement) element;
                    Object value = jaxbelement.getValue();

                    context.pushToStack(new PolyType(jaxbelement.getName().getLocalPart()));

                    if (jaxbelement.getDeclaredType() == StyleLinkType.class) {
                        visitStyleLink((StyleLinkType) value);
                    } else if (jaxbelement.getDeclaredType() == InlineImageType.class) {
                        visitInlineImage((InlineImageType) value);
                    } else {
                        throw new RuntimeException(element.toString());
                    }

                    context.popFromStack();

                } else {
                    throw new RuntimeException(element.toString());
                }

            }
        });
    }


}
