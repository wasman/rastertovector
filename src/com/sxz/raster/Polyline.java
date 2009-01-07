package com.sxz.raster;

import java.util.HashSet;
import java.util.Iterator;
import java.awt.geom.Point2D;

public final class Polyline extends Shape {

	public Polyline() {
		super();
	}

	public boolean isPoint() {
		return size() == 1;
	}

    public HashSet getPoints() {
        final HashSet result = new HashSet();
        if (isPoint()) {
            result.addAll(getLocations());
            return result;
        }
        final Iterator iterator = iterator();
        Point2D point = (Point2D)iterator.next();
        while (iterator.hasNext()) {
            result.add(point);
            if (!iterator.hasNext()) {
                break;
            }
            final Point2D nextPoint = (Point2D)iterator.next();
            //determine if horizontal or vertical
            if (point.getX() == nextPoint.getX()) {
                final double y = nextPoint.getY() - point.getY();
                int diff = 1;
                if (y < 0) {
                    diff = -1;
                }
                for (double i = point.getY(); i != nextPoint.getY(); i = i + diff) {
                    final Point2D otherPoint = new Point2D.Double(point.getX(), i);
                    result.add(otherPoint);
                }
            } else {
                final double x = nextPoint.getX() - point.getX();
                int diff = 1;
                if (x < 0) {
                    diff = -1;
                }
                for (double i = point.getX(); i != nextPoint.getX(); i = i + diff) {
                    final Point2D otherPoint = new Point2D.Double(i, point.getY());
                    result.add(otherPoint);
                }
            }
            result.add(nextPoint);
            point = nextPoint;
        }
        return result;
    }
}
