package net.ishchenko.omfp;

import net.ishchenko.omfp.pdf.PdfPrintingVisitor;
import net.ishchenko.omfp.pdf.PdfSettings;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.util.Properties;
import java.util.zip.ZipInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 23.02.2010
 * Time: 4:31:11
 */
public class OmfpCli {

    static Options options = new Options();

    static {
        options.addOption("s", "schema", true, "use given schema for validation");
        options.addOption("d", "directory", true, "use given firectory for output");
        options.addOption("o", "output", true, "use given filename for output");
        options.addOption("g", "gui", false, "show result in embedded viewer after conversion");
        options.addOption("s", "style", true, "style file");
        options.addOption("p", "preview", true, "preview device image");
    }

    public static void main(String[] args) throws Exception {

        sanityChecks();

        CommandLine line = parseCommandLine(args, options);
        String[] inputFilenames = parseInputFilenames(options, line);

        File inFile = new File(inputFilenames[0]);

        String outPath = line.getOptionValue("o", inFile.getName() + ".pdf");
        File outParent = new File(line.getOptionValue("d", inFile.getCanonicalFile().getParent()));
        if (!outParent.isDirectory()) {
            throw new IOException(outParent.getCanonicalPath() + " should be a directory");
        }
        File outFile = new File(outParent, outPath);


        PdfSettings pdfSettings = new PdfSettings.Builder(new FileInputStream(getStylesFile(line))).build();

        FileOutputStream outputStream = new FileOutputStream(outFile);
        OmfpConverter.convert(getSchema(line), getInputStream(inFile), new FictionBookVisitor[]{
                new PdfPrintingVisitor(outputStream, pdfSettings)
        });
        outputStream.close();

        //jafb-with-preview is the application name in appassembler plugin configuration.
        //the problem is that appassembler does not allow for setting extra parameters per application
        //that's why app.name parameter is checked here 
        if (line.hasOption("g") || "omfp-with-preview".equals(System.getProperty("app.name"))) {

            String device;
            if (line.getOptionValue("p") != null) {
                device = line.getOptionValue("p");
            } else {
                //the same as config base name
                Properties settings = new Properties();
                settings.load(new FileInputStream(getStylesFile(line)));
                device = settings.getProperty("device"); //can be null. no device background in that case.
            }

            new OmfpGui().show(outFile, device);
        }

    }

    /**
     * Choosing a style file. $basedir/styles/default reference is used if no style specified
     */
    private static File getStylesFile(CommandLine line) throws IOException {

        String styleOptionValue = line.getOptionValue("s");

        if (styleOptionValue != null) {

            return new File(styleOptionValue).isAbsolute() ? new File(styleOptionValue) : new File(getBasedirPath(), styleOptionValue);

        } else {

            //using default style referenced by <basedir>/styles/default
            final String defaultStyleReference = FileUtils.readFileToString(new File(getStyleDirPath(), "default"));

            File[] referenceMatches = new File(getStyleDirPath()).listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.matches(defaultStyleReference + ".properties") || name.matches(defaultStyleReference);
                }
            });
            if (referenceMatches.length > 0) {
                return referenceMatches[0];
            } else {
                throw new IOException("Cannot find style \"" + defaultStyleReference + ")\" referenced in styles/default");
            }

        }
    }

    private static String getBasedirPath() {
        return System.getProperty("basedir");
    }

    private static String getStyleDirPath() {
        return getBasedirPath() + File.separator + "styles";
    }

    private static void sanityChecks() {
        if (getBasedirPath() == null) {
            throw new RuntimeException("Please, set 'basedir' environment variable (path to directory with styles, devices, lib etc)");
        }
    }

    private static InputStream getInputStream(File inFile) throws IOException {
        InputStream inputStream;
        if (inFile.getName().toLowerCase().endsWith(".zip")) {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(inFile));
            zis.getNextEntry();
            inputStream = zis;
        } else {
            inputStream = new FileInputStream(inFile);
        }
        return inputStream;
    }

    private static String[] parseInputFilenames(Options options, CommandLine line) {
        String[] parameters = line.getArgs();
        if (parameters.length != 1) {
            printHelpAndExit(options);
        }
        return parameters;
    }

    private static CommandLine parseCommandLine(String[] args, Options options) {
        GnuParser parser = new GnuParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args, true);
        } catch (ParseException e) {
            printHelpAndExit(options);
        }
        return line;
    }

    private static void printHelpAndExit(Options options) {
        new HelpFormatter().printHelp("omfp [options] input", options);
        System.exit(1);
    }


    private static Schema getSchema(CommandLine line) throws SAXException {

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        String schemaOption = line.getOptionValue("f");
        if (schemaOption != null) {
            return sf.newSchema(new File(schemaOption));
        } else {
            return sf.newSchema(OmfpConverter.class.getResource("/FictionBook-2.21.xsd"));
        }

    }

}

