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

import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.io.File;
import java.io.IOException;
import com.sxz.parser.format.Format;
import com.sxz.parser.*;

public final class TestRegion {

	public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("No valid input file!");
            return;
        }
        final Format format = new Format();
		for (int i = 0; i < args.length; i++) {
            //final Region region = new Region();
            Region region = null;
            final ArrayList list = readFile(args[i]);
            final Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                final Location location = (Location)iterator.next();
				if (region == null) {
					region = new Region(location);
				} else {
					System.out.println("testing " + location);
					System.out.println("with error 10: " + region.findError(location, 10.0));
					region.add(location);
				}
            }
            format.initialize();
            format.format(region);
            System.out.println(format.toString());
		}
	}

    //return two dimensional arraylist
    private static ArrayList readFile(String filename) {
        final File file = new File(filename);        
        if (!file.exists()) {
            System.err.println("File does not exist: " + file.getPath());
            return null;
        }

        if (!file.canRead()) {
            System.err.println("File can not be read: " + file.getPath());
            return null;
        }
        final ArrayList result = new ArrayList();
        LineNumberReader reader = null;
        try {
            reader = new LineNumberReader(new BufferedReader(new FileReader(file)));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                final StringTokenizer tokens = new StringTokenizer(line, " ");
                if (!tokens.hasMoreTokens()) {
                    System.err.println("Bad line " + line);
                    continue;
                }
                final int x = Integer.parseInt(tokens.nextToken()); 
                final int y = Integer.parseInt(tokens.nextToken()); 
                final Location location = new Location(x, y);

                final ArrayList lineArray = new ArrayList();
                while (tokens.hasMoreTokens()) {
                    final StringTokenizer components = new StringTokenizer(tokens.nextToken(), ",");
                    final int red = Integer.parseInt(components.nextToken()); 
                    final int green = Integer.parseInt(components.nextToken()); 
                    final int blue = Integer.parseInt(components.nextToken()); 
                    final Color color = new Color(red, green, blue);
                    location.setColor(color);
                }
                final Location up = getLocation(result, x, y - 1);
                if (up != null) {
                    up.setDown(location);
                    location.setUp(up);
                }
                final Location down = getLocation(result, x, y + 1);
                if (down != null) {
                    down.setUp(location);
                    location.setDown(down);
                }
                final Location right = getLocation(result, x + 1, y);
                if (right != null) {
                    right.setLeft(location);
                    location.setRight(right);
                }
                final Location left = getLocation(result, x - 1, y);
                if (left != null) {
                    left.setRight(location);
                    location.setLeft(left);
                }
                result.add(location);
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

        return result;
    }

    private static Location getLocation(ArrayList locations, int x, int y) {
        final Iterator iterator = locations.iterator();
        while (iterator.hasNext()) {
            final Location location = (Location)iterator.next();
            if (location.getX() == x && location.getY() == y) {
                return location;
            }
        }
        return null;
    }
}
