package com.sxz.raster;

import com.sxz.parser.Color;

public final class FillColor extends Paint {

	private Color color;

    public FillColor(Color color) {
        super();
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor(double x, double y) {
        return color;
    }

    public boolean isMonoColor() {
        return true;
    }
}
