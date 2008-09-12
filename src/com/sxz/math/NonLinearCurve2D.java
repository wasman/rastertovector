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

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * Regression analysis on a series of two dimensional coordinates to return
 * a quadratic bezier curve. Coincident so the first and last points do not
 * move.  Does not support multiple points on the y axis.  Just switch the
 * x and y axis if necessary.
 * 
 */
public final class NonLinearCurve2D {

	//adjust lineNumber to better match points # of lines = 2 ^ lineNumber
	private final static int LINE_NUMBER = 5;
	private final static int ITERATIONS = 100;
	private final static double ERROR_DIVISOR = 0.5;

	private TreeMap samples;

	public NonLinearCurve2D() {
		super();
		samples = new TreeMap(new DoubleComparator());
	}

	public NonLinearCurve2D(Point2D start, Point2D control, Point2D end) {
		this();
		final QuadCurve2D quadCurve = Util.getCurve(start, control, end);
		setQuadCurve2D(quadCurve);

	}

	public double getValue(double x) {
		final Double result = (Double)samples.get(new Double(x));
		if (result == null) {
			//this is a problem
			return Double.NaN;
		}
		return result.doubleValue();
	}

	public ArrayList getPoints() {
		final ArrayList result = new ArrayList(samples.size());
		final Iterator iterator = samples.keySet().iterator();
		while (iterator.hasNext()) {
			final Double key = (Double)iterator.next();
			result.add(new Point2D.Double(key.doubleValue(), getValue(key.doubleValue())));
		}

		return result;
	}

	public double width() {
		if (samples.size() < 2) {
			return 0.0;
		}
		final Double firstKey = (Double)samples.firstKey(); 
		final Double lastKey = (Double)samples.lastKey(); 
		return lastKey.doubleValue() - firstKey.doubleValue();
	}

	private ArrayList getInternalPoints() {
		if (samples.size() < 2) {
			return new ArrayList();
		}
		final ArrayList result = new ArrayList(samples.size() - 2);
		final Iterator iterator = samples.keySet().iterator();
		boolean first = true;
		while (iterator.hasNext()) {
			final Double key = (Double)iterator.next();
			//skip the first and last element
			if (first) {
				first = false;
				continue;
			}
			if (!iterator.hasNext()) {
				break;
			}
			result.add(new Point2D.Double(key.doubleValue(), getValue(key.doubleValue())));
		}

		return result;
	}

	public void add(Point2D point) {
		add(point.getX(), point.getY());
	}

	public void add(double x, double y) {
		samples.put(new Double(x), new Double(y));
	}

	public boolean remove(double x) {
		return samples.remove(new Double(x)) != null;
	}

	public double findSeparation(Point2D point) {
		return findSeparation(point.getX(), point.getY());
	}

	//this returns the maximum difference between any two neighbor y values
	//TODO: this may need further optimization
	public double findSeparation(double x, double y) {
		if (size() == 0) {
			return 0.0;
		}

		add(x, y);
		double maximumSeparation = 0.0;
		Double previousSample = null;
		final Iterator iterator = samples.values().iterator();
		while (iterator.hasNext()) {
			final Double sample = (Double)iterator.next();
			if (previousSample == null) {
				previousSample = sample;
				continue;
			}
			final double separation = Math.abs(previousSample.doubleValue() -
					sample.doubleValue());
			if (separation > maximumSeparation) {
				maximumSeparation = separation;
			}
			previousSample = sample;
		}

		remove(x);

		return maximumSeparation;
	}

	public double findError(Point2D point) {
		return findError(point.getX(), point.getY());
	}

	public double findError(double x, double y) {
		add(x, y);
		final double result = findError();
		remove(x);

		return result;
	}

	public double findError() {
		if (size() < 4) {
			return 0.0;
		}

		final ReduceError reduceError = getReduceError();
		if (reduceError == null) {
			return Double.MAX_VALUE;
		}

		return reduceError.error;
	}

	//tricky beast that takes an initial guess at an approximate curve and
	//grinds the error down like sanding a piece of wood to fit
	private ReduceError getReduceError(ArrayList points, Point2D start,
			Point2D control, Point2D end) {
		final Point2D midPoint = getMidpoint(start, end);

		//reuse the lines variable to hold the generated lines of the curve
		final ArrayList lines = new ArrayList(2 ^ LINE_NUMBER);

		int i = 1;
		//grind down on the most errorful point then switch to the next most
		//errorful point, etc. back and forth slowly grinding down
		ReduceError oldReduceError = null;

		while (true) {
			final QuadCurve2D initialCurve = Util.getCurve(start, control, end);
			getLines(lines, initialCurve, LINE_NUMBER);

			//first find most erroneous point
			final ErrorPoint[] errors = calculateErrors(points, lines,
					start, end);
			final int worstIndex = getWorstIndex(errors);
			final ErrorPoint errorPoint = errors[worstIndex];

			final double error = errorPoint.error; 
			//System.out.println("getReduceError has error " + error);
			if (oldReduceError != null && oldReduceError.error <= error) {
				//we're not getting any better so just give up and use the
				//previous result
				return oldReduceError;
			}

			final ReduceError reduceError = new ReduceError();
			reduceError.start = start;
			reduceError.end = end;
			reduceError.control = control;
			reduceError.error = error;
			reduceError.errorIndex = worstIndex;


			oldReduceError = reduceError;

			//any one of two possible ways to stop trying and exit method
			//is it good enough or are we tired of trying?
			if (error < Util.LIMIT || i > ITERATIONS) {
				return reduceError;
			}

			//the center point is the estimated top most location of the curve
			//find the current line to the center point
			final Point2D oldCenterPoint = getMidpoint(midPoint, control);
			final Line2D line = new Line2D.Double(midPoint, oldCenterPoint);

			final Point2D point = errorPoint.point;
			final Point2D closestPoint = errorPoint.closestPoint;

			//generate new control point by moving it in the y direction
			//of the error between the worst point and the control point
			//generate new control point and make it the current one
			final double moveX = (point.getX() - closestPoint.getX())
					* ERROR_DIVISOR;
			final double moveY = (point.getY() - closestPoint.getY())
					* ERROR_DIVISOR;
			control = new Point2D.Double(control.getX() + moveX,
					control.getY() + moveY);

			i++;
			lines.clear();
		}
	}

	public ReduceError getReduceError() {
		if (size() == 0) {
			return null;
		}
		final Point2D start = getStartPoint();
		final Point2D end = getEndPoint();

		ArrayList internalPoints = getInternalPoints();
		final int length = internalPoints.size() + 2;
		ReduceError lastReduceError = null;
		final Point2D intersection = searchForIntersection(start, end,
				internalPoints);

		if (intersection == null) {
			return null;
		}

		final Point2D projectedControl = getProjectedControlPoint(start,
				intersection, end);

		internalPoints = getInternalPoints();

		final ReduceError reduceError = getReduceError(internalPoints,
				start, projectedControl, end);

		return reduceError;
	}

	public double getStart() {
		if (samples.size() == 0) {
			return 0.0;
		}
		final Double firstKey = (Double)samples.firstKey();
		return firstKey.doubleValue();
	}

	public double getEnd() {
		if (samples.size() == 0) {
			return 0.0;
		}
		final Double lastKey = (Double)samples.lastKey();
		return lastKey.doubleValue();
	}

	public Point2D getStartPoint() {
		if (samples.size() == 0) {
			return null;
		}

		final Double x = (Double)samples.firstKey();
		final Double y = (Double)samples.get(x);
		return new Point2D.Double(x.doubleValue(), y.doubleValue());
	}

	public Point2D getEndPoint() {
		if (samples.size() == 0) {
			return null;
		}
		final Double x = (Double)samples.lastKey();
		final Double y = (Double)samples.get(x);
		return new Point2D.Double(x.doubleValue(), y.doubleValue());
	}

	public Point2D getControlPoint() {
		if (size() == 1) {
			return getStartPoint();
		}
		final ReduceError reduceError = getReduceError();
		if (reduceError == null) {
			return null;
		}
		return reduceError.control;
	}

	public int size() {
		return samples.size();
	}

	public Iterator getXValues() {
		return samples.keySet().iterator();
	}

	public Iterator getYValues() {
		return samples.values().iterator();
	}

	public QuadCurve2D getQuadCurve2D() {
		final ReduceError reduceError = getReduceError();
		if (reduceError == null) {
			return null;
		}
		return getQuadCurve2D(reduceError);
	}

	public QuadCurve2D getQuadCurve2D(ReduceError reduceError) {
		if (reduceError == null) {
			return null;
		}
		return Util.getCurve(reduceError.start, reduceError.control, reduceError.end);
	}

	public double getValue(QuadCurve2D curve, double x) {
		final ArrayList lines = new ArrayList(2 ^ LINE_NUMBER);
		getLines(lines, curve, LINE_NUMBER);
		//now slice lines into evenly spaced points from p1 to p2
		final Point2D start = curve.getP1();
		add(start);
		final Point2D end = curve.getP2();
		add(end);
		final Point2D point = new Point2D.Double(x, 0.0);
		final int lineIndex = findClosestVerticalLine(point, lines, 0);
		if (lineIndex >= lines.size() || lineIndex < 0) {
			System.out.println("Failure to find closestLine with index " +
					lineIndex);
			return Double.MAX_VALUE;
		}
		final Line2D closestLine = (Line2D)lines.get(lineIndex);
		if (closestLine == null) {
			System.err.println("Failure to find closestLine with index " +
					lineIndex);
			return Double.MAX_VALUE;
		}

		final Point2D closestPoint = findPointLineVerticalIntersection(point, closestLine);
		return closestPoint.getY();
	}

	//returns the amount of error if any
	public void setQuadCurve2D(QuadCurve2D curve) {
		final ArrayList lines = new ArrayList(2 ^ LINE_NUMBER);
		getLines(lines, curve, LINE_NUMBER);
		//now slice lines into evenly spaced points from p1 to p2
		final Point2D start = curve.getP1();
		add(start);
		final Point2D end = curve.getP2();
		add(end);

		int lineIndex = 0;
		for (int i = (int)start.getX() + 1; i < end.getX(); i++) {
			final Point2D point = new Point2D.Double(i, 0.0);

			lineIndex = findClosestVerticalLine(point, lines, lineIndex);
			if (lineIndex < 0) {
				System.err.println("Failure to findClosestVerticalLine");
				continue;
			}
			final Line2D closestLine = (Line2D)lines.get(lineIndex);

			if (closestLine == null) {
				System.err.println("Failure to find closestLine with index " +
						lineIndex);
				break;
			}

			final Point2D closestPoint = findPointLineVerticalIntersection(point, closestLine);
			add(closestPoint);
		}
	}

	//gets an intersection of the curve moving along the x axis
	//by intersecting the curve with a very long vertical line
	private Point2D getPoint(double distance, ArrayList lines) {
		final Point2D startPoint = new Point2D.Double(distance, 0);
		//iterate over the line segments of the curve and find the right one
		final Iterator iterator = lines.iterator();
		final Point2D intersection = new Point2D.Double();
		while (iterator.hasNext()) {
			//line here is a segment of the curve
			final Line2D.Double line = (Line2D.Double)iterator.next();
			final int result = Util.findLineSegmentIntersection(line,
					new Line2D.Double(distance, -1 * Double.MAX_VALUE,
					distance, Double.MAX_VALUE), intersection);
			if (result > 0) {
				//now we know which line segment is the one we want
				//find the point on the curve line that is the point of
				//intersection between them and return that as a y value
				return intersection;
			}
		}
		return null;
	}

	//is this a straight line?
	public boolean isLinear() {
		if (samples.size() < 3) {
			return true;
		}
		final Line2D line = new Line2D.Double(getStartPoint(), getEndPoint());
		final Iterator iterator = samples.keySet().iterator();
		while (iterator.hasNext()) {
			final Double key = (Double)iterator.next();
			final Double value = (Double)samples.get(key);
			final Point2D point = new Point2D.Double(key.doubleValue(), value.doubleValue()); 
			if (pointLineDistance(point, line) != 0) {
				return false;
			}
		}
		return true;
	}

	private static boolean isLinear(ArrayList points, Point2D start, Point2D end) {

		final Line2D line = new Line2D.Double(start, end);
		final Iterator iterator = points.iterator();
		while (iterator.hasNext()) {
			final Point2D sample = (Point2D)iterator.next();
			if (pointLineDistance(sample, line) != 0) {
				return false;
			}
		}
		return true;
	}

	public boolean isSinglePoint() {
		return samples.size() == 1;
	}

	public boolean isFlatLine() {
		if (size() < 3) {
			//bail out?
			return true;
		}
		final double start = getStartPoint().getY();
		final double end = getEndPoint().getY();
		final double control = getControlPoint().getY();
		return start == end && start == control && end == control;
	}

	public Point2D getMidpoint() {
		return getMidpoint(getStartPoint(), getEndPoint());
	}

	private static Point2D getMidpoint(Point2D start, Point2D end) {
		final Point2D.Double result = new Point2D.Double((start.getX() +
				 	end.getX()) / 2.0, (start.getY() + end.getY()) / 2.0);
		return result;
	}

	private static Point2D getProjectedControlPoint(Point2D lineStart,
		 	Point2D point, Point2D lineEnd) {

		final Point2D midpoint = getMidpoint(lineStart, lineEnd);
		if (midpoint == null) {
			return null;
		}

		Util.extendLine(midpoint, point, 2 * midpoint.distance(point));

		return midpoint;
	}

	//recursively search for control point by backing up from the end points
	//if there is no intersection
	private static Point2D searchForIntersection(Point2D start, Point2D end, ArrayList samples, boolean alternate) {
		if (isLinear(samples, start, end)) {
			return getMidpoint(start, end);
		}
		final Point2D b = (Point2D)samples.get(0);
		final Point2D d = (Point2D)samples.get(samples.size() - 1);
		if (b.equals(d)) {
			//it's a triangle!
			return b; //or d, whichever
		}

		final Line2D line1 = new Line2D.Double(start, b);
		final Line2D line2 = new Line2D.Double(end, d);

		final Point2D intersection = Util.getIntersectionOfRays(line1, line2);
		if (intersection == null) {
			if (alternate) {
				samples.remove(samples.size() - 1);
				end = (Point2D)samples.get(samples.size() - 1);
			} else {
				samples.remove(0);
				start = (Point2D)samples.get(0);
			}
			return searchForIntersection(start, end, samples, !alternate);
		}

		return intersection;
	}

	//simpler version that just takes the midpoint if there is no intersection
	private static Point2D searchForIntersection(Point2D start, Point2D end, ArrayList samples) {
		if (isLinear(samples, start, end)) {
			return getMidpoint(start, end);
		}
		final Point2D b = (Point2D)samples.get(0);
		final Point2D d = (Point2D)samples.get(samples.size() - 1);
		if (b.equals(d)) {
			//it's a triangle!
			return b; //or d, whichever
		}

		final Line2D line1 = new Line2D.Double(start, b);
		final Line2D line2 = new Line2D.Double(end, d);

		final Point2D intersection = Util.getIntersectionOfRays(line1, line2);
		if (intersection == null) {
			return getMidpoint(start, end);
		}

		return intersection;
	}

	private static double pointLineDistance(Point2D sample, Line2D line) {
		return Util.pointLineDistance(sample, line);
	}

	//is this necessary
	private static boolean areAllErrorsTheSame(ErrorPoint[] errors) {
		double value = errors[0].error;
		for (int i = 1; i < errors.length; i++) {
			if (errors[i].error != value) {
				return false;
			}
		}
		return true;
	}

	private static int getWorstIndex(ErrorPoint[] errors) {
		int worstIndex = 0;
		double worstIndexValue = errors[0].error;
		for (int i = 1; i < errors.length; i++) {
			if (errors[i].error > worstIndexValue) {
				worstIndex = i;
				worstIndexValue = errors[i].error;
			}
		}
		return worstIndex;
	}

	private ErrorPoint[] calculateErrors(ArrayList points, ArrayList lines, Point2D start, Point2D end) {
		final Point2D originalStart = getStartPoint();
		final Point2D originalEnd = getEndPoint();

		final ErrorPoint[] result = new ErrorPoint[points.size() + 2];
		final ErrorPoint startErrorPoint = new ErrorPoint();
		startErrorPoint.point = originalStart;
		startErrorPoint.closestPoint = start;
		startErrorPoint.error = originalStart.distance(start);
		result[0] = startErrorPoint;

		Line2D line = null;
		int lineIndex = 0;
		final Iterator iterator = points.iterator();
		int j = 1;
		while (iterator.hasNext()) {
			final Point2D point = (Point2D)iterator.next();

			lineIndex = findClosestVerticalLine(point, lines, lineIndex);
			if (lineIndex < 0) {
				System.err.println("Failure to findClosestVerticalLine");
				return null;
			}
			final Line2D closestLine = (Line2D)lines.get(lineIndex);

			if (closestLine == null) {
				System.err.println("Failure to find closestLine with index " +
						lineIndex);
				return null;
			}
			final Point2D closestPoint = findPointLineVerticalIntersection(point, closestLine);
			//System.out.println("the closest point is " + closestPoint);
			final ErrorPoint errorPoint = new ErrorPoint();
			errorPoint.point = point;
			//errorPoint.closestPoint = closestPoint;
			errorPoint.closestPoint = closestPoint;
			errorPoint.error = closestPoint.distance(point);
			//System.out.println("adding with error " + errorPoint.error);
			result[j++] = errorPoint;

		}
		final ErrorPoint endErrorPoint = new ErrorPoint();
		endErrorPoint.point = originalEnd;
		endErrorPoint.closestPoint = end;
		endErrorPoint.error = originalEnd.distance(end);
		//System.out.println("adding end with error " + endErrorPoint.error);
		result[result.length - 1] = endErrorPoint;
		return result;
	}

	private static int findClosestVerticalLine(Point2D point, ArrayList lines,
			int lineIndex) {
		//first create a vertical line through the x value of point
		final Point2D start = new Point2D.Double(point.getX(), Double.MAX_VALUE);
		final Point2D end = new Point2D.Double(point.getX(), -1 * Double.MAX_VALUE);
		final Line2D verticalLine = new Line2D.Double(start, end);
		final int size = lines.size();
		for (int i = lineIndex; i < size; i++) {
			final Line2D line = (Line2D)lines.get(i);
			if (line.intersectsLine(verticalLine)) {
				return i;
			}
		}
		System.err.println("findClosestVerticalLine found nothing!");
		return -1;
	}

	private static Point2D findPointLineVerticalIntersection(Point2D point,
		 	Line2D line) {

		//first create a vertical line through the x value of point
		final Point2D start = new Point2D.Double(point.getX(), Double.MAX_VALUE);
		final Point2D end = new Point2D.Double(point.getX(), -1 * Double.MAX_VALUE);
		final Line2D verticalLine = new Line2D.Double(start, end);
		final Point2D intersection = new Point2D.Double();
		final int result = Util.findLineSegmentIntersection(line, verticalLine,
			 	intersection);
		if (result == -1) {
			//this shouldn't happen
			System.err.println("Parallel lines in findPointLineVerticalIntersection!");
		}
		//what about coincident
		return intersection;
	}

	public ArrayList getLines(QuadCurve2D curve) {
		final ArrayList result = new ArrayList();
		getLines(result, curve, LINE_NUMBER);
		return result;
	}

	//recursive method to retrieve all the straight lines that form a curve
	//builds the line segments that is used to represent a quad curve at a
	//certain level of n
	private static void getLines(ArrayList lines, QuadCurve2D curve, int n) {
		if (n == 1) {
			final Point2D start = curve.getP1();
			final Point2D end = curve.getP2();
			final Point2D control = curve.getCtrlPt();
			final Point2D midPoint1 = getMidpoint(start, control);
			final Point2D midPoint2 = getMidpoint(end, control);
			final Point2D center = getMidpoint(midPoint1, midPoint2);

			final Line2D left = new Line2D.Double(start, center);
			final Line2D right = new Line2D.Double(center, end);
			lines.add(left);
			lines.add(right);
			return;
		}

		final QuadCurve2D left = new QuadCurve2D.Double();
		final QuadCurve2D right = new QuadCurve2D.Double();
		curve.subdivide(left, right);
		getLines(lines, left, n - 1);
		getLines(lines, right, n - 1);
	}

	public boolean compareTo(NonLinearCurve2D curve) {
		return compareStartPoint(curve) && compareControlPoint(curve) &&
				compareEndPoint(curve);
	}

	public boolean equals(Object object) {
		final NonLinearCurve2D curve = (NonLinearCurve2D)object;
		return compareTo(curve);
	}

	private boolean compareStartPoint(NonLinearCurve2D curve) {
		return curve.getStartPoint().getY() == getStartPoint().getY(); 
	}

	//control point can be null sometimes
	private boolean compareControlPoint(NonLinearCurve2D curve) {
		final Point2D controlPoint = getControlPoint();
		final Point2D otherControlPoint = curve.getControlPoint();
		if (controlPoint == null && otherControlPoint == null) {
			return true;
		}
		if (controlPoint == null) {
			return false;
		}
		if (otherControlPoint == null) {
			return false;
		}
		return controlPoint.equals(otherControlPoint);
	}

	private boolean compareEndPoint(NonLinearCurve2D curve) {
		return curve.getEndPoint().getY() == getEndPoint().getY(); 
	}

	public String toString() {
		return getStartPoint() + " " + getControlPoint() + " " + getEndPoint();
	}


}
