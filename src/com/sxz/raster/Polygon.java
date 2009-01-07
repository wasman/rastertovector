package com.sxz.raster;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.awt.geom.Point2D;
import com.sxz.parser.Rectangle;

public final class Polygon extends Shape {

	public Polygon() {
		super();
	}

	public Polygon(ArrayList locations) {
		super();
		addLocations(locations);
	}

	public boolean isRectangle() {
		return size() == 4;
	}

    public HashSet getPoints() {
        final HashSet result = new HashSet();
        //first add in all the edges because no one else will
        Iterator iterator = iterator();
        Point2D point = (Point2D)iterator.next();
        final Point2D startPoint = point;
        while (true) {
            Point2D nextPoint = null;
            if (iterator.hasNext()) {
                nextPoint = (Point2D)iterator.next(); 
            } else {
                nextPoint = startPoint;
            }
            //System.out.println("next point is " + nextPoint);
            //determine if horizontal or vertical
            if (point.getX() == nextPoint.getX()) {
                final double y = nextPoint.getY() - point.getY();
                int diff = 1;
                if (y < 0) {
                    diff = -1;
                }
                //System.out.println("vertical going from point " + point + " to " + nextPoint);
                for (double i = point.getY(); i != nextPoint.getY(); i = i + diff) {
                    final Point2D otherPoint = new Point2D.Double(point.getX(), i);
                    result.add(otherPoint);
                }
            } else if (point.getY() == nextPoint.getY()) {
                //System.out.println("horizontal going from point " + point + " to " + nextPoint);
                final double x = nextPoint.getX() - point.getX();
                int diff = 1;
                if (x < 0) {
                    diff = -1;
                }
                for (double i = point.getX(); i != nextPoint.getX(); i = i + diff) {
                    final Point2D otherPoint = new Point2D.Double(i, point.getY());
                    //System.out.println("adding horizontal point " + otherPoint);
                    result.add(otherPoint);
                }
            } else {
                //must be diagonal
                final double y = nextPoint.getY() - point.getY();
                int ydiff = 1;
                if (y < 0) {
                    ydiff = -1;
                }
                final double x = nextPoint.getX() - point.getX();
                int xdiff = 1;
                if (x < 0) {
                    xdiff = -1;
                }
                for (double i = point.getX(); i != nextPoint.getX(); i = i + xdiff) {
                    for (double j = point.getY(); j != nextPoint.getY(); j = j + ydiff) {
                        final Point2D otherPoint = new Point2D.Double(i, j);
                        result.add(otherPoint);
                    }
                }
            }
            point = nextPoint;
            if (nextPoint == startPoint) {
                break;
            }
        }

        final Rectangle boundingBox = getBoundingBox();

        final int startX = (int)boundingBox.getX1(); 
        final int endX = (int)boundingBox.getX2(); 
        final int startY = (int)boundingBox.getY1(); 
        final int endY = (int)boundingBox.getY2(); 

        final java.awt.Polygon aPolygon = new java.awt.Polygon();
        iterator = iterator();
        while (iterator.hasNext()) {
            final Point2D insidePoint = (Point2D)iterator.next();
            aPolygon.addPoint((int)insidePoint.getX(), (int)insidePoint.getY());
        }

        for (int j = startX; j < endX; j++) {
            for (int i = startY; i < endY; i++) {
                if (aPolygon.contains(j, i)) {
                    final Point2D aPoint = new Point2D.Double(j, i);
                    if (!result.contains(aPoint)) {
                        result.add(aPoint);
                    }
                }
            }
        }
        return result;
    }
}
