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
package com.sxz.math;

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

public final class TestCurve {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("No valid input file!");
			return;
		}
		for (int i = 0; i < args.length; i++) {
			NonLinearCurve2D curve = new NonLinearCurve2D();
			final ArrayList list = readFile(args[i]);
			final Iterator iterator = list.iterator();
			ReduceError reduceError = null;
			while (iterator.hasNext()) {
				final Point2D point = (Point2D)iterator.next();
				System.out.println("adding point " + point);
				curve.add(point);
				reduceError = curve.getReduceError();
				if (reduceError == null) {
					System.out.println("reduceError is null");
					continue;
				}
				System.out.println("has error " + reduceError.error);
				System.out.println("start " + reduceError.start + " control " + reduceError.control + " end " + reduceError.end);
			}
			System.out.println("done adding points");
			System.out.println("with final error: " + reduceError.error);
			final QuadCurve2D quadCurve = curve.getQuadCurve2D(reduceError);	
			if (quadCurve == null) {
				System.err.println("Failed to get the quadcurve");
				continue;
			}

			curve = new NonLinearCurve2D();
			curve.setQuadCurve2D(quadCurve);

			System.out.println("extrapolated points:");
			final Iterator points = curve.getPoints().iterator();
			while (points.hasNext()) {
				final Point2D point = (Point2D)points.next();
				System.out.println(point);
			}
		}
	}

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
				final StringTokenizer tokens = new StringTokenizer(line);
				if (!tokens.hasMoreTokens()) {
					System.err.println("Bad line " + line);
					continue;
				}
				double x = 0.0;
				try {
					x = Double.parseDouble(tokens.nextToken());
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					continue;
				}
				if (!tokens.hasMoreTokens()) {
					System.err.println("Bad line " + line);
					continue;
				}
				double y = 0.0;
				try {
					y = Double.parseDouble(tokens.nextToken());
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					continue;
				}
				final Point2D point = new Point2D.Double(x, y);
				result.add(point);
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

}
