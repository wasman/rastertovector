/*
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
Author: DarkLilac email:contact@darklilac.com
*/
package com.sxz.math;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;

public final class Util {
	public final static double LIMIT = 1e-5;
	public final static double INFINITY = 1e10;
	public final static DecimalFormat FORMATTER = new DecimalFormat(); 
	static {
		FORMATTER.setMaximumFractionDigits(2);
	}

	public static String format(double value) {
		final Double doubleValue = new Double(value);
		final int intValue = doubleValue.intValue();
		if (intValue == value) {
			return Integer.toString(intValue);
		}
		return FORMATTER.format(value);
	}

	//returns point of intersection of two rays or null if there is no
	//intersection. Assumes direction of rays based on ordering of points
	public static Point2D getIntersectionOfRays(Line2D line1, Line2D line2) {
		//first we check for line segments
		final Point2D result = new Point2D.Double();
		final int returnValue = findLineSegmentIntersection(line1, line2,
				result);
		if (returnValue == -1) {
			//parallel
			return null;
		} else if (returnValue == 1 || returnValue == -2) {
			//if segments intersect or coincident
			return result;
		}

		//first check against line1
		//determine if result x is to the correct side of line1.x1
		double xDirection = line1.getX2() - line1.getX1();
		if (xDirection < 0) {
			if (result.getX() > line1.getX2()) {
				return null;
			}
		} else if (xDirection > 0) {
			if (result.getX() < line1.getX2()) {
				return null;
			}
		}
		double yDirection = line1.getY2() - line1.getY1();
		if (yDirection < 0) {
			if (result.getY() > line1.getY2()) {
				return null;
			}
		} else if (yDirection > 0) {
			if (result.getY() < line1.getY2()) {
				return null;
			}
		}

		xDirection = line2.getX2() - line2.getX1();
		if (xDirection < 0) {
			if (result.getX() > line2.getX2()) {
				return null;
			}
		} else if (xDirection > 0) {
			if (result.getX() < line2.getX2()) {
				return null;
			}
		}

		yDirection = line2.getY2() - line2.getY1();
		if (yDirection < 0) {
			if (result.getY() > line2.getY2()) {
				return null;
			}
		} else if (yDirection > 0) {
			if (result.getY() < line2.getY2()) {
				return null;
			}
		}

		//determine if result violates constraints
		return result;
	}

	//does not check for parallel or coincident lines
	//gets the intersection of lines, NOT line segments
	public static Point2D getIntersection(Line2D line1, Line2D line2) {
		final Point2D intersection = new Point2D.Double();
		final int result = findLineSegmentIntersection(line1, line2,
				intersection);
		return intersection;
	}

	/**
	 * Compute the intersection between two line segments, or two lines
	 * of infinite length.
	 * @return -1 if lines are parallel (x,y unset),
	 *		 -2 if lines are parallel and overlapping (x, y center)
	 *		  0 if intesrection outside segments (x,y set)
	 *		 +1 if segments intersect (x,y set)
	*/
	public static int findLineSegmentIntersection(Line2D one, Line2D two,
		 	Point2D intersection) {
		final double x0 = one.getX1();
		final double y0 = one.getY1();
		final double x1 = one.getX2();
		final double y1 = one.getY2();
		final double x2 = two.getX1();
		final double y2 = two.getY1();
		final double x3 = two.getX2();
		final double y3 = two.getY2();

		double x, y;

		final double a0 = equals(x0, x1) ? INFINITY : (y0 - y1) / (x0 - x1);
		final double a1 = equals(x2, x3) ? INFINITY : (y2 - y3) / (x2 - x3);

		final double b0 = y0 - a0 * x0;
		final double b1 = y2 - a1 * x2;

		// Check if lines are parallel
		if (equals(a0, a1)) {
			if (!equals(b0, b1)) {
				return -1; // Parallell non-overlapping
			} else {
				if (equals (x0, x1)) {
					if (Math.min (y0, y1) < Math.max (y2, y3) ||
							Math.max (y0, y1) > Math.min (y2, y3)) {
						double twoMiddle = y0 + y1 + y2 + y3 - min (y0, y1, y2, y3) - max (y0, y1, y2, y3);
						y = (twoMiddle) / 2.0;
						x = (y - b0) / a0;
					} else {
						return -1; // Parallell non-overlapping
					}
				} else {
					if (Math.min (x0, x1) < Math.max (x2, x3) ||
							Math.max (x0, x1) > Math.min (x2, x3)) {
						double twoMiddle = x0 + x1 + x2 + x3 - min (x0, x1, x2, x3) - max (x0, x1, x2, x3);
						x = (twoMiddle) / 2.0;
						y = a0 * x + b0;
					} else {
						return -1;
					}
				}
				intersection.setLocation(x, y);
				return -2;
			}
		}

		// Find correct intersection point
		if (equals(a0, INFINITY)) {
			x = x0;
			y = a1 * x + b1;
		} else if (equals(a1, INFINITY)) {
			x = x2;
			y = a0 * x + b0;
		} else {
			x = - (b0 - b1) / (a0 - a1);
			y = a0 * x + b0;
		}
		intersection.setLocation(x, y);

		// Then check if intersection is within line segments
		double distanceFrom1 = 0.0;
		if (equals(x0, x1)) {
			if (y0 < y1) {
				distanceFrom1 = y < y0 ? length (x, y, x0, y0) : y > y1 ? length (x, y, x1, y1) : 0.0;
			} else {
				distanceFrom1 = y < y1 ? length (x, y, x1, y1) : y > y0 ? length (x, y, x0, y0) : 0.0;
			}
		} else {
			if (x0 < x1) {
				distanceFrom1 = x < x0 ? length (x, y, x0, y0) : x > x1 ? length (x, y, x1, y1) : 0.0;
			} else {
				distanceFrom1 = x < x1 ? length (x, y, x1, y1) : x > x0 ? length (x, y, x0, y0) : 0.0;
			}
		}

		double distanceFrom2 = 0.0;
		if (equals(x2, x3)) {
			if (y2 < y3) {
			distanceFrom2 = y < y2 ? length (x, y, x2, y2) : y > y3 ? length (x, y, x3, y3) : 0.0;
			} else {
				distanceFrom2 = y < y3 ? length (x, y, x3, y3) : y > y2 ? length (x, y, x2, y2) : 0.0;
			}
		} else {
			if (x2 < x3) {
				distanceFrom2 = x < x2 ? length (x, y, x2, y2) : x > x3 ? length (x, y, x3, y3) : 0.0;
			} else {
				distanceFrom2 = x < x3 ? length (x, y, x3, y3) : x > x2 ? length (x, y, x2, y2) : 0.0;
			}
		}

		return equals(distanceFrom1, 0.0) && equals(distanceFrom2, 0.0) ? 1 : 0;
	}

	public static double length(double x0, double y0, double x1, double y1) {
		final double dx = x1 - x0;
		final double dy = y1 - y0;

		return Math.sqrt(dx * dx + dy * dy);
	}

	public static double length(Line2D line) {
		final double x0 = line.getP1().getX();
		final double y0 = line.getP1().getY();
		final double x1 = line.getP2().getX();
		final double y1 = line.getP2().getY();
		return length(x0, y0, x1, y1);
	}

	public static double length(Point2D p1, Point2D p2) {
		final double x0 = p1.getX();
		final double y0 = p1.getY();
		final double x1 = p2.getX();
		final double y1 = p2.getY();
		return length(x0, y0, x1, y1);
	}

	public static boolean equals(double a, double b) {
		return Math.abs (a - b) < LIMIT;
	}

	private static double min(double a, double b, double c, double d) {
		return Math.min (Math.min (a, b), Math.min (c, d));
	}

	private static double max(double a, double b, double c, double d) {
		return Math.max (Math.max (a, b), Math.max (c, d));
	}

	public static double max(double a, double b, double c) {
		return Math.max (a, Math.max (b, c));
	}

	//checks against line and not line segment
	public static Point2D getNearestPointOnLine(Point2D end, Line2D line) {
		final Point2D point = line.getP1();
		final Point2D start = line.getP2();
		double a = (end.getX() - point.getX())*(start.getX() - point.getX()) + (end.getY() - point.getY())*(start.getY() - point.getY());
		/*
		if (a <= 0.0) {
			return point;
		}
		*/
		double b = (end.getX() - start.getX())*(point.getX() - start.getX()) + (end.getY() - start.getY())*(point.getY() - start.getY());
		/*
		if (b <= 0.0) {
			return start;
		}
		*/
		final double x = point.getX() + ((start.getX() - point.getX()) * a)/(a + b);
		final double y = point.getY() + ((start.getY() - point.getY()) * a)/(a + b);
		final Point2D result = new Point2D.Double(x, y);
		return result;
	}

	public static Point2D getNearestPointOnLineSegment(Point2D end, Line2D line) {
		final Point2D point = line.getP1();
		final Point2D start = line.getP2();
		double a = (end.getX() - point.getX())*(start.getX() - point.getX()) + (end.getY() - point.getY())*(start.getY() - point.getY());
		/*
		if (a < 0 && Math.abs(a) < LIMIT) {
			return point;
		} else if (a < 0.0) {
			//System.out.println("a failed with " + a);
			return null;
		}
		*/
		if (a < 0) {
			return null;
		}
		
		double b = (end.getX() - start.getX())*(point.getX() - start.getX()) + (end.getY() - start.getY())*(point.getY() - start.getY());
		/*
		if (b < 0 && Math.abs(b) < LIMIT) {
			return start;
		} else if (b < 0.0) {
			//System.out.println("b is " + b);
			return null;
		}
		*/
		if (b < 0) {
			return null;
		}
		
		final double x = point.getX() + ((start.getX() - point.getX()) * a)/(a + b);
		final double y = point.getY() + ((start.getY() - point.getY()) * a)/(a + b);
		final Point2D result = new Point2D.Double(x, y);
		return result;
	}

	public static Point2D getNearestPointOnLineSegmentSafely(Point2D end, Line2D line) {
		final Point2D point = line.getP1();
		final Point2D start = line.getP2();
		double a = (end.getX() - point.getX())*(start.getX() - point.getX()) + (end.getY() - point.getY())*(start.getY() - point.getY());
		if (a < 0) {
			return point;
		}
		
		double b = (end.getX() - start.getX())*(point.getX() - start.getX()) + (end.getY() - start.getY())*(point.getY() - start.getY());
		if (b < 0) {
			return start;
		}
		
		final double x = point.getX() + ((start.getX() - point.getX()) * a)/(a + b);
		final double y = point.getY() + ((start.getY() - point.getY()) * a)/(a + b);
		final Point2D result = new Point2D.Double(x, y);
		return result;
	}


	public static void extendLine(Point2D p0, Point2D p1, double toLength) {
		final double oldLength = p0.distance(p1);
		final double lengthFraction =
		 		oldLength != 0.0 ? toLength / oldLength : 0.0;
		p1.setLocation(p0.getX() + (p1.getX() - p0.getX()) * lengthFraction,
			p0.getY() + (p1.getY() - p0.getY()) * lengthFraction);
	}

	public static double pointLineDistance(Point2D point, Line2D line) {
		return line.ptSegDist(point);
	}

	public static QuadCurve2D getCurve(Point2D start, Point2D control,
		 	Point2D end) {
		final QuadCurve2D curve = new QuadCurve2D.Double(start.getX(),
			 	start.getY(), control.getX(), control.getY(), end.getX(),
			 	end.getY());
		return curve;
	}

	//y = mx + b
	//b = y - mx
	//m is rise / run = gradient
	//width and height of bounding box
	//for a box 10x10 then width and height are 9,9
	public static Line2D getGradientLine(double run, double rise, double width, double height, double x, double y) {
		if (run == 0 && rise == 0) {
			return new Line2D.Double(x, y, x + width, y + height);
		}

		//calculate hypotenuse
		//check for a vertical line
		if (run == 0) {
			return new Line2D.Double(x, y, x, y + height);
		}
		//check for a horizontal line
		if (rise == 0) {
			return new Line2D.Double(x, y, x + width, y);
		}
		//calculate gradient
		double m = rise / run;
		Point2D start;
		Point2D opposite;
		if (m < 0) {
			//lower left
			start = new Point2D.Double(x, y + height); 
			opposite = new Point2D.Double(x + width, y); 

		} else {
			//upper left 
			start = new Point2D.Double(x, y);
			opposite = new Point2D.Double(x + width, y + height); 
		}
		//System.out.println("m is " + m);
		//System.out.println("start is " + start);
		//now calculate the slope intercept b
		double b = start.getY() - (m * start.getX());
		//System.out.println("b is " + b);

		//now calculate another point along the slope
		Point2D next = null;
		if (m > 0) {
			next = new Point2D.Double(start.getX() + Math.abs(run), start.getY() + Math.abs(rise));
		} else {
			if (rise < 0) {
				next = new Point2D.Double(start.getX() + run, start.getY() + rise);
			} else {
				next = new Point2D.Double(start.getX() - run, start.getY() - rise);
			}
		}
		final double a = Math.sqrt((width * width) + (height * height));
		Util.extendLine(start, next, a);
		//now trim to appropriate length
		Line2D gradientLine = new Line2D.Double(start, next);

		final Point2D nearestPoint = Util.getNearestPointOnLineSegment(opposite,
				gradientLine);
		if (nearestPoint == null) {
			//this is bad
			System.err.println("getGradientLine has an issue");
		}
		gradientLine = new Line2D.Double(start, nearestPoint);
		return gradientLine;

	}
}
