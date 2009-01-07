package com.sxz.raster;

import com.sxz.parser.Color;

public final class ColorGradient extends Paint {

	private Gradient red;
	private Gradient green;
	private Gradient blue;

    public ColorGradient() {
        super();
    }

    public Color getColor(double x, double y) {
        //System.out.println("getting red");
        final double redColor = red.getValue(x, y);
        //System.out.println("getting green");
        final double greenColor = green.getValue(x, y);
        //System.out.println("getting blue");
        final double blueColor = blue.getValue(x, y);

        return new Color(redColor, greenColor, blueColor);
    }

    public Gradient getRed() {
        return red;
    }

    public Gradient getGreen() {
        return green;
    }

    public Gradient getBlue() {
        return blue;
    }

    public void setRed(Gradient red) {
        this.red = red;
    }

    public void setGreen(Gradient green) {
        this.green = green;
    }

    public void setBlue(Gradient blue) {
        this.blue = blue;
    }

}
