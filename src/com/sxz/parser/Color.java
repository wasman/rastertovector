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
package com.sxz.parser;

import java.awt.color.ColorSpace;
import com.sxz.math.Util;

public class Color {
    private static final int RED_MASK = 255 << 16;
    private static final int GREEN_MASK = 255 << 8;
    private static final int BLUE_MASK = 255;
    private static final double KB = 0.114;
    private static final double KR = 0.299;

    protected double red;
    protected double green;
    protected double blue;

    public Color() {
        super();
    }

    public Color(int red, int green, int blue) {
        super();
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Color(int rgb) {
        super();
        red = (rgb & RED_MASK) >> 16;
        green = (rgb & GREEN_MASK) >> 8;
        blue = (rgb & BLUE_MASK);
    }

    public Color(int rgb, int alpha) {
        super();
        final double alphaRate = alpha / 255.0;
        red = ((rgb & RED_MASK) >> 16) / 255.0;
        red = (alphaRate * red + (1 - alphaRate) * 1) * 255.0;;
        green = ((rgb & GREEN_MASK) >> 8) / 255.0;
        green = (alphaRate * green + (1 - alphaRate) * 1) * 255.0;;
        blue = (rgb & BLUE_MASK) / 255.0;
        blue = (alphaRate * blue + (1 - alphaRate) * 1) * 255.0;;
    }


    public Color(double red, double green, double blue) {
        super();
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public double getRed() {
        return red;
    }

    public double getGreen() {
        return green;
    }

    public double getBlue() {
        return blue;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    //implement a dumb gamut here
    public int getRGB() {
        int red = (int)this.red;
        if (red > 255) {
            red = 255;
        }
        if (red < 0) {
            red = 0;
        }
        int green = (int)this.green;
        if (green > 255) {
            green = 255;
        }
        if (green < 0) {
            green = 0;
        }
        int blue = (int)this.blue;
        if (blue > 255) {
            blue = 255;
        }
        if (blue < 0) {
            blue = 0;
        }
        return (255 << 24) | (red << 16) | (green << 8) | blue;

    }

    public boolean equals(Color color) {
        return red == color.getRed() && green == color.getGreen() &&
                blue == color.getBlue();
    }

    public String toString() {
        return Util.format(red) + " " + Util.format(green) + " " + Util.format(blue);
    }

    public static int getAlpha(int argb) {
        return (argb >> 24) & 0xFF;
    }
/*
    //http://www.jpeg.org/public/jfif.pdf
    public void convertToYCbCr() {
        final double newRed = 0.299 * red + 0.587 * green + 0.114 * blue;
        final double newGreen = -0.168736 * red - 0.331264 * green + 0.5 * blue + 128;
        final double newBlue = 0.5 * red - 0.418688 * green - 0.081312 * blue + 128;
        red = newRed;
        green = newGreen / 4;
        blue = newBlue / 4;
    }

    public Color convertToRGB() {
        final double newRed = red + 1.402 * (blue * 4 - 128);
        final double newGreen = red - 0.34414 * (green * 4 - 128) - 0.71414 * (blue * 4 - 128);
        final double newBlue = red + 1.772 * (green * 4 - 128);
        return new Color(newRed, newGreen, newBlue);
    }
*/
    public double getTotal() {
        return red + green + blue;
    }

	public static double getColorDistance(Color source, Color target) {
		if (source.equals(target)) {
			return 0.0d;
		}
		final double red = source.getRed() - target.getRed();
		final double green = source.getGreen() - target.getGreen();
		final double blue = source.getBlue() - target.getBlue();
		return Math.sqrt(red * red + blue * blue + green * green);
	}

}
