package net.ishchenko.omfp;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 20.05.2010
 * Time: 14:46:13
 */
public class Directories {

    private String baseDir;
    private String stylesDir;

    public Directories(String baseDir) {
        this.baseDir = baseDir;
        stylesDir = baseDir + File.separator + "styles";
    }

    public String getBaseDir() {
        return baseDir;
    }

    public String getStylesDir() {
        return stylesDir;
    }

    public File getDefaultStyleFile() {
        return new File(stylesDir, "default");
    }

    public static Directories newDefaultDirectories() {
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            throw new RuntimeException("Please, set 'basedir' environment variable (path to directory with styles, devices, lib etc)");
        }
        File basedirFile = new File(basedir);
        if (!basedirFile.exists()) {
            throw new RuntimeException("'basedir' (" + basedir + ") must exist");
        }
        if (!basedirFile.isDirectory()) {
            throw new RuntimeException("'basedir' (" + basedir + ") must be a directory");
        }
        return new Directories(basedir);
    }
}
