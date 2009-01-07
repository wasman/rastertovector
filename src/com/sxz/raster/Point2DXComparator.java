package com.sxz.raster;

import java.util.Comparator;
import java.awt.geom.Point2D;

public final class Point2DXComparator implements Comparator {

    public Point2DXComparator() {
        super();
    }

    public int compare(Object o1, Object o2) {
        Point2D sample1 = (Point2D)o1;
        Point2D sample2 = (Point2D)o2;
        return (int)Math.round(sample1.getX() - sample2.getX());
    }
}
