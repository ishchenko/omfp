package net.ishchenko.omfp;

import net.ishchenko.omfp.model.FictionBook;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 06.04.2010
 * Time: 19:05:42
 */
public class OmfpConverter {

    public static void convert(Schema schema, InputStream input, FictionBookVisitor[] visitors) throws JAXBException, SAXException {

        Unmarshaller unmarshaller = JAXBContext.newInstance(FictionBook.class).createUnmarshaller();
        unmarshaller.setSchema(schema);

        FictionBook book;
        try {
            book = (FictionBook) unmarshaller.unmarshal(input);
        } catch (JAXBException e) {
            e.printStackTrace();
            //todo: log
            System.out.println("Processing with no validation");
            unmarshaller.setSchema(schema);
            book = (FictionBook) unmarshaller.unmarshal(input);
        }

        ProcessingContext context = new ProcessingContext();

        FictionBookVisitor[] allVisitors = new FictionBookVisitor[visitors.length + 1];
        allVisitors[0] = new LinkCollectingVisitor(context);
        System.arraycopy(visitors, 0, allVisitors, 1, allVisitors.length - 1);

        for (FictionBookVisitor visitor : allVisitors) {
            visitor.init(context);
            new FictionBookWalker(visitor, context).visitBook(book);
            visitor.commit();
        }


    }


}
