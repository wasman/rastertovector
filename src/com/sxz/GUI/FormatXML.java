package com.sxz.GUI;

import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import com.sxz.parser.*;
import com.sxz.math.*;

public final class FormatXML {

    private StringBuffer stringBuffer;

    public FormatXML() {
        super();
        stringBuffer = new StringBuffer();
    }

    public void initialize() {
        stringBuffer.setLength(0);
        //capacity?
    }

    public String format(Frame frame) {
        //regions = frame.getRegions();
		List regions = frame.getReversedSortedList();
        stringBuffer.append("<frame>\n");
        /*while (regions.size() > 0) {
            final Region region = (Region)regions.iterator().next();
            regions.remove(region);
            format(region);
        }
		*/
		format((Region)regions.get(0), new HashSet());

        stringBuffer.append("</frame>\n");
        return stringBuffer.toString();
    }

    public void format(Region region, HashSet regions) {
        if (region.isParentSameColor()) {
            return;
        }
        final HashSet parents = region.getParents();
        if (parents.size() > 0) {
            final Iterator iterator = parents.iterator();
            while (iterator.hasNext()) {
                final Region parent = (Region)iterator.next();
                if (regions.contains(parent)) {
                    regions.remove(parent);
                    format(parent, regions);
                }
            }
        }
        stringBuffer.append("<region id=\"" + region.hashCode() + "\">\n");
		final HashSet edges = region.getSortedEdges();
		final Iterator iterator = edges.iterator();
		while (iterator.hasNext()) {
			final Shape shape = (Shape)iterator.next();
            format(shape);
		}
        //put color gradient stuff here
        if (region.getFillColor() != null) {
            format(region.getFillColor());
        } else {
            final ColorGradient colorGradient = PaintGenerator.generate(region.getLocations());
            if (colorGradient == null) {
                //something has gone very wrong
                System.err.println("ColorGradient is null!");
            }
            format(colorGradient);
        }
        if (region.neighborSize() > 0) {
            final Iterator neighbors = region.neighbors();
            stringBuffer.append("<neighbors>");
            while (neighbors.hasNext()) {
                final Region neighbor = (Region)neighbors.next();
                stringBuffer.append(neighbor.hashCode());
                if (neighbors.hasNext()) {
                    stringBuffer.append(" ");
                }
            }
            stringBuffer.append("</neighbors>\n");
        }
        final HashSet children = region.getChildren();
        if (children.size() > 0) {
            final Iterator childIterator = children.iterator();
            stringBuffer.append("<children>");
            while (childIterator.hasNext()) {
                final Region child = (Region)childIterator.next();
                stringBuffer.append(child.hashCode());
                if (childIterator.hasNext()) {
                    stringBuffer.append(" ");
                }
            }
            stringBuffer.append("</children>\n");
        }
        stringBuffer.append("</region>\n");
    }

    public void format(Gradient gradient, String type) {
		format((LinearGradient)gradient, type);
    }

    public void format(FillColor fillColor) {
        stringBuffer.append("<paint>");
        stringBuffer.append(fillColor);
        stringBuffer.append("</paint>\n");
    }

    public void format(ColorGradient colorGradient) {
        //need some code here to compress output for identical gradients
        stringBuffer.append("<paint>");
        if (colorGradient == null) {
            System.out.println("colorGradient is null!");
            return;
        }
        if (colorGradient.compareTo()) {
            format(colorGradient.getRed(), "a");
        } else {
            format(colorGradient.getRed(), "r");
            format(colorGradient.getGreen(), "g");
            format(colorGradient.getBlue(), "b");
        }
        stringBuffer.append("\n</paint>\n");
    }

    public void format(LinearGradient linearGradient, String type) {
        final Line2D gradientLine = linearGradient.getGradientLine();
        //fill this in
        stringBuffer.append("\n<linearGradient x1=\"");
        stringBuffer.append(Util.format(gradientLine.getP1().getX()));
        stringBuffer.append("\" y1=\"");
        stringBuffer.append(Util.format(gradientLine.getP1().getY()));
        stringBuffer.append("\" x2=\"");
        stringBuffer.append(Util.format(gradientLine.getP2().getX()));
        stringBuffer.append("\" y2=\"");
        stringBuffer.append(Util.format(gradientLine.getP2().getY()));
        stringBuffer.append("\" type=\"");
        stringBuffer.append(type);
        stringBuffer.append("\">");
        format(linearGradient.getCurve());
        //stringBuffer.append(" valueAtOrigin:");
        //stringBuffer.append(Util.format(linearGradient.getValueAtOrigin()));
        //stringBuffer.append(" error:");
        //stringBuffer.append(Util.format(linearGradient.getError()));
        stringBuffer.append("</linearGradient>");
    }

    public void format(NonLinearCurve2D curve) {
    /*
        if (curve.isSinglePoint() || curve.isLinear()) {
            final Point2D point = curve.getStartPoint();
            stringBuffer.append(Util.format(point.getY()));
        } else {
    */
            final ReduceError reduceError = curve.getReduceError();
            final Point2D start = reduceError.start;
            stringBuffer.append(Util.format(start.getY()));
            stringBuffer.append(" ");
            final Point2D control = reduceError.control;
            stringBuffer.append(Util.format(control.getX()));
            stringBuffer.append(",");
            stringBuffer.append(Util.format(control.getY()));
            stringBuffer.append(" ");
            final Point2D end = reduceError.end;
            stringBuffer.append(Util.format(end.getY()));
        //}
    }

    public void format(Shape shape) {
        if (shape instanceof Polyline) {
            format((Polyline)shape);
        } else {
            format((Polygon)shape);
        }
    }

	public void format(Polyline polyline) {
		if (polyline.size() == 1) {
            stringBuffer.append("<point>");
		} else {
			stringBuffer.append("<line>");
		}
		final Iterator iterator = polyline.iterator();
		Location previousLocation = null;
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
			if (previousLocation != null) {
				if (previousLocation.getX() == location.getX()) {
					stringBuffer.append("v");
					stringBuffer.append(location.getY() - previousLocation.getY());
				} else {
					stringBuffer.append("h");
					stringBuffer.append(location.getX() - previousLocation.getX());
				}

			} else {
				//otherwise just write the coordinates
				stringBuffer.append(location.getX());
				stringBuffer.append(",");
				stringBuffer.append(location.getY());
			}
			previousLocation = location;
			if (iterator.hasNext()) {
				stringBuffer.append(" ");
			}
		}
		if (polyline.size() == 1) {
            stringBuffer.append("</point>\n");
		} else {
			stringBuffer.append("</line>\n");
		}
	}

	public void format(Polygon polygon) {
		//TODO: move rectangle into its own class
		if (polygon.isRectangle()) {
            stringBuffer.append("<rectangle>");
			final ArrayList locations = polygon.getLocations();
			final Location upperLeft = (Location)locations.get(0);
			final Location lowerRight = (Location)locations.get(2);
			stringBuffer.append(upperLeft.getX());
			stringBuffer.append(",");
			stringBuffer.append(upperLeft.getY());
			stringBuffer.append(" ");
			stringBuffer.append(lowerRight.getX());
			stringBuffer.append(",");
			stringBuffer.append(lowerRight.getY());
            stringBuffer.append("</rectangle>\n");
			return;
		}
		stringBuffer.append("<polygon>");
		final Iterator iterator = polygon.iterator();
		Location previousLocation = null;
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
			if (previousLocation != null) {
				if (previousLocation.getX() == location.getX()) {
					stringBuffer.append("v");
					stringBuffer.append(location.getY() - previousLocation.getY());
				} else if (previousLocation.getY() == location.getY()) {
					stringBuffer.append("h");
					stringBuffer.append(location.getX() - previousLocation.getX());
				} else {
                    stringBuffer.append(location.getX());
                    stringBuffer.append(",");
                    stringBuffer.append(location.getY());
                }
			} else {
				//otherwise just write the coordinates
				stringBuffer.append(location.getX());
				stringBuffer.append(",");
				stringBuffer.append(location.getY());
			}
			previousLocation = location;
			if (iterator.hasNext()) {
				stringBuffer.append(" ");
			}
		}
		stringBuffer.append("</polygon>\n");
	}

    public String toString() {
        return stringBuffer.toString();
    }

}
