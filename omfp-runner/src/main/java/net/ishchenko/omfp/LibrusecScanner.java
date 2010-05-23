package net.ishchenko.omfp;

import net.ishchenko.omfp.model.FictionBook;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 11.05.2010
 * Time: 23:00:27
 */
public class LibrusecScanner {
    public static void main(String[] args) throws IOException, SAXException, JAXBException {

        Schema schema = getSchema();
        Unmarshaller unmarshaller = JAXBContext.newInstance(FictionBook.class).createUnmarshaller();

        File[] files = getFiles(args[0]);

        int ok = 0;
        int notOk = 0;
        int fail = 0;

        for (File file : files) {

            System.out.println("Parsing " + file.getName());

            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {

                ZipEntry entry = entries.nextElement();
                InputStream is = zipFile.getInputStream(entry);

                unmarshaller.setSchema(schema);

                FictionBook book;
                try {
                    book = (FictionBook) unmarshaller.unmarshal(is);
                    ok++;
                } catch (JAXBException e) {
                    notOk++;
                } catch (Exception e) {
                    fail++;
                } finally {
                    is.close();
                }

            }

            System.out.println(ok + " " + notOk + " " + fail);

        }

    }

    private static File[] getFiles(String arg) {

        File file = new File(arg);
        File[] result;
        if (file.isDirectory()) {
            result = file.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.matches("fb2.*");
                }
            });
        } else {
            result = new File[]{file};
        }
        return result;
    }


    private static Schema getSchema() throws SAXException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return sf.newSchema(new File("omfp-model\\src\\main\\resources\\FictionBook-2.21.xsd"));
    }

}
