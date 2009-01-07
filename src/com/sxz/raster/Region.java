package com.sxz.raster;

import java.util.HashSet;
import java.util.Iterator;
import java.awt.geom.Point2D;
import com.sxz.parser.Color;
import com.sxz.parser.Rectangle;
import com.sxz.parser.Location;

public final class Region {

	private Paint paint;
	private HashSet shapes;

	public Region() {
		super();
		shapes = new HashSet();
	}

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Paint getPaint() {
        return paint;
    }

    public Color getColor(Point2D point) {
        return paint.getColor(point.getX(), point.getY());
    }

	public HashSet getShapes() {
		return shapes;
	}

	public void remove(Shape shape) {
		shapes.remove(shape);
	}

	public void add(Shape shape) {
		if (shapes.contains(shape)) {
			System.err.println("Error: duplicate shape");
		}
		shapes.add(shape);
	}

	public int size() {
		return shapes.size();
	}

	public Iterator iterator() {
		return shapes.iterator();
	}

	public void clear() {
		shapes.clear();
	}

    public HashSet getPoints() {
        final HashSet result = new HashSet();
        final Iterator iterator = iterator();
        while (iterator.hasNext()) {
            final Shape shape = (Shape)iterator.next();
            result.addAll(shape.getPoints());
        }
        return result;
    }

    public Rectangle getBoundingBox() {
        Rectangle result = new Rectangle();
		final Iterator iterator = getPoints().iterator();
		while (iterator.hasNext()) {
			final Point2D point = (Point2D)iterator.next();
            result.add(point.getX(), point.getY());
		}
		return result;
    }

    public boolean isRectangle() {
        if (size() != 1) {
            return false;
        }
        final Shape shape = (Shape)shapes.iterator().next();
        return shape.isRectangle();
    }
}
