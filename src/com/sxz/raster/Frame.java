package com.sxz.raster;

import java.util.ArrayList;
import java.util.Iterator;
import com.sxz.parser.Rectangle;

public final class Frame {
	private ArrayList regions;

	public Frame() {
		super();
		regions = new ArrayList();
	}

	public ArrayList getRegions() {
		return regions;
	}

	public void remove(Region region) {
		regions.remove(region);
	}

	public void add(Region region) {
    /*
		if (regions.contains(region)) {
			System.err.println("Error: duplicate region");
		}
    */
		regions.add(region);
	}

	public int size() {
		return regions.size();
	}

	public Iterator iterator() {
		return regions.iterator();
	}

	public void clear() {
		regions.clear();
	}

    public Rectangle getBoundingBox() {
        final Rectangle rectangle = new Rectangle();
        final Iterator iterator = iterator();
        while (iterator.hasNext()) {
            final Region region = (Region)iterator.next();
            rectangle.add(region.getBoundingBox());
        }
        return rectangle;
    }

}
