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
package com.sxz.parser;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.HashSet;
import com.sxz.math.NonLinearCurve2D;
import com.sxz.math.ReduceError;
import com.sxz.math.SampleContainer;
import com.sxz.math.Sample;
import com.sxz.math.Util;

public abstract class LinearGradientParser extends GradientParser {

    public LinearGradientParser() {
        super();
    }

    public Gradient getGradient(HashSet locations) {
        //System.out.println("called getGradient with locations size " + locations.size());
        //first build the derivative of the locations
        double riseTotal = 0.0;
        int riseCount = 0;
        double runTotal = 0.0;
        int runCount = 0;
        //first do run
        Iterator iterator = locations.iterator();
        while (iterator.hasNext()) {
            final Location location = (Location)iterator.next();
            final double colorValue = getColorValue(location);

            final Location right = location.getRight();
            if (right != null && locations.contains(right)) {
                runTotal += (getColorValue(right) - colorValue);
                runCount++;
            }
            final Location down = location.getDown();
            if (down != null && locations.contains(down)) {
                riseTotal += getColorValue(down) - colorValue;
                riseCount++;
            }
        }
        double run = 0;
        if (runCount > 0) {
            run = runTotal / runCount;
        }
        double rise = 0;
        if (riseCount > 0) {
            rise = riseTotal / riseCount;
        }
        /*
        if (run == 0 && rise == 0) {
            System.err.println("no gradient");
            return null;
        }
        */
        final Rectangle boundingBox = getBoundingBox(locations);
        final double width = boundingBox.getWidth(); 
        final double height = boundingBox.getHeight(); 
        if (width == 0 && height == 0) {
            System.out.println("LinearGradientParser no width and height");
            new Exception().printStackTrace();
            iterator = locations.iterator();
            while (iterator.hasNext()) {
                final Location location = (Location)iterator.next();
                System.out.println("location " + location);
            }
            return null;
        }
        //System.out.println("getGradientLine(" + boundingBox.getX() + ", " + boundingBox.getY() + ", " + width + ", " + height + ")");

        Line2D line = getGradientLine(run, rise, width, height, boundingBox.getX1(), boundingBox.getY1());
        if (line == null) {
            System.out.println("line is null");
            //we have a problem
            System.err.println("LinearGradientParser line is null");
            return null;
        }
        //System.out.println("with run/rise at " + run + " " + rise);
        //now trim the line to be the right length
        //System.out.println("line before trim is " + line.getP1() + " " + line.getP2());
        line = trimToShape(locations, line); 
        //System.out.println("line after trim is " + line.getP1() + " " + line.getP2());

        final SampleContainer sampler = new SampleContainer();
        iterator = locations.iterator();
        while (iterator.hasNext()) {
            final Location location = (Location)iterator.next();
            final Point2D point = new Point2D.Double(location.getX(), location.getY());
            final Point2D nearestPoint = Util.getNearestPointOnLine(point, line);
            //if this is null, something has gone horribly wrong
            if (nearestPoint == null) {
                System.err.println("uh -oh!");
                System.err.println("for point " + point);
                System.err.println("and line " + line.getP1() + " " + line.getP2());
                System.err.println("and nearestPointOnLine " + Util.getNearestPointOnLine(point, line));
            }
            final double distance = Util.length(line.getP1(), nearestPoint);
            //System.out.println("adding to sampler " + distance + " value " + getColorValue(location));
            //System.out.println("for location " + location);
            sampler.add(distance, getColorValue(location));
        }
        final NonLinearCurve2D curve = new NonLinearCurve2D();
        final Iterator samples = sampler.iterator();
        while (samples.hasNext()) {
            final Sample sample = (Sample)samples.next();
            //System.out.println("adding sample to curve " + sample.getX() + " " + sample.getAverage());
            curve.add(sample.getX(), sample.getAverage());
        }

        /*
        //old simple way
        final double totalAmountChanged = getTotalAmountChanged(run, rise,
                line);
        //System.out.println("total amount changed is " + totalAmountChanged);
        double originTotal = 0.0;
        int count = 0;
        iterator = locations.iterator();
        //System.out.println("for origin point " + line.getP1());
        while (iterator.hasNext()) {
            final Location location = (Location)iterator.next();
            final Point2D point = new Point2D.Double(location.getX(), location.getY());
            final Point2D nearestPoint = Util.getNearestPointOnLine(point,
                    line);
            //System.out.println("for location " + location);
            //System.out.println("the nearestPoint is " + nearestPoint);
            if (nearestPoint.equals(line.getP1())) {
                final double colorValue = getColorValue(location);
                originTotal += colorValue;
                count++;
            }
        }
        originTotal = originTotal / count;
        */
        final ReduceError reduceError = curve.getReduceError();
        if (reduceError == null) {
            System.err.println("LinearGradientParser ReduceError is null");
            return null;
        }
        if (Util.length(line) != curve.width()) {
            //System.out.println("LinearGradientParser has varied length of " + Util.length(line) + " and " + curve.width());
            //System.out.println("with line " + line.getP1() + " " + line.getP2());
            //System.out.println("and curve " + curve);
        }

        final LinearGradient linearGradient = new LinearGradient();
        linearGradient.setGradientLine(line);
        linearGradient.setCurve(curve);
        linearGradient.setValueAtOrigin(sampler.first());
        linearGradient.setReduceError(reduceError);
        return linearGradient;
    }

    public static class Red extends LinearGradientParser {
        public Red() {
            super();
        }

        public double getColorValue(Location location) {
            final Color color = location.getColor();
            return color.getRed();
        }
    }

    public static class Green extends LinearGradientParser {
        public Green() {
            super();
        }

        public double getColorValue(Location location) {
            final Color color = location.getColor();
            return color.getGreen();
        }
    }

    public static class Blue extends LinearGradientParser {
        public Blue() {
            super();
        }

        public double getColorValue(Location location) {
            final Color color = location.getColor();
            return color.getBlue();
        }
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

        final Point2D nearestPoint = Util.getNearestPointOnLine(opposite,
                gradientLine);
        if (nearestPoint == null) {
            //this is bad
            System.err.println("getGradientLine has an issue with " + opposite);
            System.err.println("with gradientLine " + gradientLine.getP1() + " "+ gradientLine.getP2());
        }
        gradientLine = new Line2D.Double(start, nearestPoint);
        return gradientLine;

    }

    public static double getTotalAmountChanged(double run, double rise, Line2D gradientLine) { 
        //now calculate total amount of change along gradientLine
        final double width = gradientLine.getP2().getX() - gradientLine.getP1().getX();
        final double height = gradientLine.getP2().getY() - gradientLine.getP1().getY();
        final double runWidth = run * width;
        final double riseHeight = rise * height;
        return runWidth + riseHeight;
    }
/*
    public static double getGradientValue(double x, double y, Line2D gradientLine, NonLinearCurve2D curve) {
        final Point2D point = new Point2D.Double(x, y);
        final Point2D nearestPoint = Util.getNearestPointOnLine(point,
                gradientLine);
        final double distance = Util.length(gradientLine.getP1(), nearestPoint);

        return curve.getValue(distance);
    }
*/

    public static Line2D trimToShape(HashSet locations, Line2D gradientLine) {
        final Iterator iterator = locations.iterator();
        final Point2D midpoint = new Point2D.Double((gradientLine.getP1().getX() + gradientLine.getP2().getX()) / 2, (gradientLine.getP1().getY() + gradientLine.getP2().getY()) / 2);
        Point2D closestToStart = midpoint;
        Point2D closestToEnd = midpoint;
        while (iterator.hasNext()) {
            final Location location = (Location)iterator.next();
            final Point2D point = new Point2D.Double(location.getX(), location.getY()); 
            final Point2D nearestPoint = Util.getNearestPointOnLine(point, gradientLine);
            if (nearestPoint == null) {
                //this is bad
                System.err.println("failed to get nearest point in trimtoshape");
                System.err.println("with point " + point);
                System.err.println("and line " + gradientLine.getP1() + " " + gradientLine.getP2());
            }
            //System.out.println("trimtoshape with point " + point);
            //System.out.println("with nearestPoint " + nearestPoint);
            //System.out.println("and line " + gradientLine.getP1() + " " + gradientLine.getP2());

            final double startDistance = nearestPoint.distance(gradientLine.getP1());
            //System.out.println("startDistance is " + startDistance);
            final double endDistance = nearestPoint.distance(gradientLine.getP2());
            //System.out.println("endDistance is " + endDistance);

            if (startDistance > endDistance) {
                //closer to the end
                if (closestToEnd == null) {
                    closestToEnd = nearestPoint;
                    continue;
                }
                if (closestToEnd.distance(gradientLine.getP2()) > endDistance) {
                    closestToEnd = nearestPoint;
                }
            } else {
                //closer to the end
                if (closestToStart == null) {
                    closestToStart = nearestPoint;
                    continue;
                }
                if (closestToStart.distance(gradientLine.getP1()) > startDistance) {
                    closestToStart = nearestPoint;
                }
            }
        }
        if (closestToStart == null) {
            closestToStart = gradientLine.getP1();
        }
        if (closestToEnd == null) {
            closestToEnd = gradientLine.getP2();
        }

        if (!gradientLine.getP1().equals(closestToStart) ||
                !gradientLine.getP2().equals(closestToEnd)) {
            return new Line2D.Double(closestToStart, closestToEnd);
        }
        return gradientLine;
    }

	public static Rectangle getBoundingBox(HashSet locations) {
        Rectangle result = new Rectangle();
		final Iterator iterator = locations.iterator();
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
            result.add(location.getX(), location.getY());
		}
		return result;
	}

}
