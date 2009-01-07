package com.sxz.raster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.awt.geom.Point2D;
import com.sxz.parser.Location;
import com.sxz.parser.Rectangle;

public abstract class Shape {

	private ArrayList locations;

	public Shape() {
		locations = new ArrayList();
	}

	public void add(Point2D point) {
		locations.add(point);
	}

	public void addLocations(ArrayList locations) {
		this.locations.addAll(locations);
	}

	public ArrayList getLocations() {
		return locations;
	}

	public int size() {
		return locations.size();
	}

	public boolean contains(Point2D point) {
        return locations.contains(point);
	}

	public Iterator iterator() {
		return locations.iterator();
	}

    public abstract HashSet getPoints();

    public Rectangle getBoundingBox() {
        Rectangle result = new Rectangle();
		final Iterator iterator = locations.iterator();
		while (iterator.hasNext()) {
			final Point2D point = (Point2D)iterator.next();
            result.add(point.getX(), point.getY());
		}
		return result;
    }

	public boolean isRectangle() {
        return false;
	}
}
