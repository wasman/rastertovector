package com.sxz.raster;

import com.sxz.parser.Color;

public abstract class Paint {

    public Paint() {
        super();
    }

    public abstract Color getColor(double x, double y);

    public boolean isMonoColor() {
        return false;
    }
}
