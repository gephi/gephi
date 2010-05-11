/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.utils;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Mathieu Bastian
 */
public class TempDirUtils {

    public static TempDir createTempDir() throws IOException {
        return new TempDir(createTempDirectory());
    }

    public static File createTempDirectory() throws IOException {
        final File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
        temp.deleteOnExit();

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }


        return (temp);
    }

    public static class TempDir {

        private File tempDir;

        private TempDir(File tempDir) {
            this.tempDir = tempDir;
        }

        public File createFile(String fileName) {
            File file =  new File(tempDir, fileName);
            file.deleteOnExit();
            return file;
        }
    }
}

