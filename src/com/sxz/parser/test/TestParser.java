/*
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
Author: DarkLilac email:contact@darklilac.com
*/
package com.sxz.parser.test;

import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import com.sxz.parser.format.Format;
import com.sxz.parser.*;

public final class TestParser {

    private static String FILENAME;

	public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("No valid input image file!");
            return;
        }
		final FrameParser parser = readFile(args[0]);
        System.out.println("running file " + args[0]);
		if (parser == null) {
			return;
		}
        System.out.println("start parsing");
        final long start = System.currentTimeMillis();
		parser.process();
        final long end = System.currentTimeMillis();
        System.out.println("done parsing with " + (end - start));
        writeXMLFile(parser);
        System.out.println("done writing file");
	}

    private static void writeXMLFile(FrameParser parser) {
        final String filename = FILENAME + ".xml";
        final File file = new File(filename);        
        /*
        if (!file.canWrite()) {
            System.err.println("File cannot be written: " + file.getPath());
            return;
        }
        */
		final Format format = new Format();
        format.initialize();
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(format.format(parser.getFrame()));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                writer = null;
            }
        }
    }

    private static FrameParser readFile(String filename) {
        final File file = new File(filename);        
        if (!file.exists()) {
            System.err.println("File does not exist: " + file.getPath());
            return null;
        }

        if (!file.canRead()) {
            System.err.println("File can not be read: " + file.getPath());
            return null;
        }
        LineNumberReader reader = null;
		FrameParser parser = null;
		double threshold = 1.0;
        try {
            reader = new LineNumberReader(new BufferedReader(new FileReader(file)));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
				if (line.startsWith("url:")) {
                    FILENAME = line.substring(4);
					BufferedImage image = FetchImage.getImage(FILENAME);
					if (image == null) {
						System.err.println("Invalid image path!");
						return null;
					}
					final LocationPool locationPool = new LocationPool(image);
					parser = new FrameParser(locationPool);
				} else if (line.startsWith("threshold:")) {
					try {
						threshold = Double.parseDouble(line.substring(10));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                reader = null;
            }
        }
		if (threshold >= 0) {
			parser.setThreshold(threshold);
		}

        return parser;
    }

}
