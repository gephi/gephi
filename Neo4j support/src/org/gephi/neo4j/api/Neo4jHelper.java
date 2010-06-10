package org.gephi.neo4j.api;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;


public class Neo4jHelper {
    private static final Set<String> NEO4J_REQUIRED_FILE_NAMES = new LinkedHashSet<String>();
    private static final String ICON_FILE_PATH = "resources/logo.png";


    static {
        NEO4J_REQUIRED_FILE_NAMES.add("neostore");
    }


    private Neo4jHelper() {}


    public static boolean isValidNeo4jDirectory(File inputDirectory) {
        if (!inputDirectory.isDirectory())
            return false;

        int existingRequiredFiles = 0;
        for (File file : inputDirectory.listFiles()) {
            if (NEO4J_REQUIRED_FILE_NAMES.contains(file.getName()))
                existingRequiredFiles++;
        }

        return existingRequiredFiles == NEO4J_REQUIRED_FILE_NAMES.size();
    }

    public static Icon getDirectoryChooserIcon() {
        InputStream imageInputStream =
                Neo4jHelper.class.getClassLoader().getResourceAsStream(ICON_FILE_PATH);

        try {
            byte[] imageFileData = new byte [imageInputStream.available()];
            imageInputStream.read(imageFileData);

            return new ImageIcon(imageFileData);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (imageInputStream != null)
                    imageInputStream.close();
            }
            catch (IOException e) {}
        }

        return null;
    }
}
