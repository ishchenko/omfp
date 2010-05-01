package net.ishchenko.omfp;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 27.03.2010
 * Time: 23:20:31
 */
public class OmfpGui {

    public void show(final File output, final String deviceName) {

        File device = null;
        if (deviceName != null) {
            //todo: unify paths construction
            File deviceDir = new File(System.getProperty("basedir") + File.separator + "devices");
            File[] matchingDevices = deviceDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.matches(deviceName + "--.+");
                }
            });

            if (matchingDevices.length > 0) {
                device = matchingDevices[0];
            }
        }

        final File finalDevice = device;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                JFrame frame = new JFrame("PDF Test");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                try {

                    frame.getContentPane().add(new PreviewPanel(output, finalDevice), BorderLayout.CENTER);
                    frame.pack();
                    frame.setVisible(true);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        });

    }

}
