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

import java.awt.image.BufferedImage;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;

public final class ImageDiff {

	private ImageDiff() {
		//nothing to see here people, move along
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Must provide two parameters knucklehead");
			return;
		}
		File file1 = new File(args[0]);
		if (!file1.exists() || !file1.canRead()) {
			System.err.println("Cannot open file " + file1.getAbsolutePath());
			return;
		}
		File file2 = new File(args[1]);
		if (!file2.exists() || !file2.canRead()) {
			System.err.println("Cannot open file " + file2.getAbsolutePath());
			return;
		}
		BufferedImage image1 = getImage(file1);
		BufferedImage image2 = getImage(file2);
		if (image1.getWidth() != image2.getWidth()) {
			System.err.println("Those two file do not have the same width");
			return;
		}
		if (image1.getHeight() != image2.getHeight()) {
			System.err.println("Those two file do not have the same height");
			return;
		}
		final int size = image1.getHeight() * image1.getWidth();
		double totalHorizontalDifference = 0.0;
		double totalVerticalDifference = 0.0;

		for (int i = 0; i < image1.getWidth(); i++) {
			for (int j = 0; j < image1.getHeight(); j++) {
				final Color color1 = new Color(image1.getRGB(i, j));
				final Color color2 = new Color(image2.getRGB(i, j));
				if (i < image1.getWidth() - 1) {
					final Color rightColor1 = new Color(image1.getRGB(i + 1, j));
					final double sourceHorizontalDiff = getColorDistance(color1, rightColor1); 
					final Color rightColor2 = new Color(image2.getRGB(i + 1, j));
					final double targetHorizontalDiff = getColorDistance(color2, rightColor2); 
					final double difference = sourceHorizontalDiff - targetHorizontalDiff;
					totalHorizontalDifference += (difference * difference);
				}
				
				if (j < image1.getHeight() - 1) {
					final Color downColor1 = new Color(image1.getRGB(i, j + 1));
					final double sourceVerticalDiff = getColorDistance(color1, downColor1); 
					final Color downColor2 = new Color(image2.getRGB(i, j + 1));
					final double targetVerticalDiff = getColorDistance(color2, downColor2); 
					final double difference = sourceVerticalDiff - targetVerticalDiff;
					totalVerticalDifference += (difference * difference);
				}
			}
		}
		System.out.println("total horizontal difference is " + totalHorizontalDifference);	
		System.out.println("total vertical difference is " + totalVerticalDifference);
	}

	private static BufferedImage getImage(File file) {
		try {
			return ImageIO.read(file);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	private static double getColorDistance(Color source, Color target) {
		if (source.equals(target)) {
			return 0.0d;
		}
		final double red = source.getRed() - target.getRed();
		final double green = source.getGreen() - target.getGreen();
		final double blue = source.getBlue() - target.getBlue();
		return Math.sqrt(red * red + blue * blue + green * green);
	}

}
