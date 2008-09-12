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
import java.awt.geom.QuadCurve2D;
import java.util.Iterator;
import java.util.HashSet;
import com.sxz.math.NonLinearCurve2D;
import com.sxz.math.Sample;
import com.sxz.math.SampleContainer;
import com.sxz.math.Util;
import com.sxz.math.ReduceError;

public class LuminanceGradientParser {

    private double riseTotal;
    private double runTotal;
    private int riseCount;
    private int runCount;
    private Rectangle rectangle;

    //cache these for future reference
    private Line2D gradientLine;
    private NonLinearCurve2D curve;
    private QuadCurve2D quadCurve;

    public LuminanceGradientParser() {
        super();
        rectangle = new Rectangle();
    }

    private static double getColorTotal(Location location) {
        return location.getColor().getTotal();
    }

    public void addAll(Region region, HashSet locations) {
        final Iterator iterator = locations.iterator();
        while (iterator.hasNext()) {
            final Location location = (Location)iterator.next();
            add(region, location);
        }
        gradientLine = null;
    }

    public void add(Region region, Location location) {
        final HashSet locations = region.getLocations();
        final double colorTotal = getColorTotal(location);
        final Location right = location.getRight();
        if (right != null && locations.contains(right)) {
            runTotal += (getColorTotal(right) - colorTotal);
            runCount++;
        }
        final Location down = location.getDown();
        if (down != null && locations.contains(down)) {
            riseTotal += getColorTotal(down) - colorTotal;
            riseCount++;
        }
        rectangle.add(location.getX(), location.getY());
    }

    public boolean findError(Region region, Location location, double threshold) {
        if (region.size() == 0) {
            return true;
        }
        final Rectangle boundingBox = region.getBoundingBox();
        if (boundingBox.getWidth() == 1 || boundingBox.getHeight() == 1) {
            return true;
        }
        if (gradientLine != null) {
            //System.out.println("for gradientLine " + gradientLine.getP1() + " " + gradientLine.getP2());
            //System.out.println("with length " + Util.length(gradientLine));
            final Point2D point = new Point2D.Double(location.getX(), location.getY());
            Point2D nearestPoint = Util.getNearestPointOnLine(point, gradientLine);
            //System.out.println("nearestPoint is " + nearestPoint);
            //System.out.println("with distance to line of " + gradientLine.ptSegDist(nearestPoint));
            if (gradientLine.ptSegDist(nearestPoint) < 2) {
            //if (nearestPoint != null) {
                nearestPoint = Util.getNearestPointOnLineSegmentSafely(point, gradientLine);
                //System.out.println("later nearestPoint is " + nearestPoint);
                double distance = Util.length(gradientLine.getP1(), nearestPoint);
                //System.out.println("x distance " + distance);
                //System.out.println("curve is " + curve);
                if (distance <= curve.width() && distance >= curve.getStartPoint().getX()) {
                    final double actualValue = curve.getValue(quadCurve, distance);
                    if (Double.isNaN(actualValue)) {
                        System.err.println("actualValue is NaN in LinearGradient");
                        System.out.println("for point " + point);
                        System.out.println("for nearestPoint " + nearestPoint);
                        System.out.println("and distance " + distance);
                        System.out.println("for line " + gradientLine.getP1() + " " + gradientLine.getP2());
                        System.out.println("and curve " + curve);
                        return false;
                    }
                    /*
                    System.out.println("actualValue is " + actualValue + " in LuminanceGradientParser");
                    System.out.println("for point " + point);
                    System.out.println("for nearestPoint " + nearestPoint);
                    System.out.println("and distance " + distance);
                    System.out.println("for line " + gradientLine.getP1() + " " + gradientLine.getP2());
                    */
                    final double result = Math.abs(actualValue - getColorTotal(location));
                    if (result < threshold) {
                        return true;
                    }
                    //System.out.println("failed initial test with " + result);
                    //return Math.abs(actualValue - getColorTotal(location)) < threshold;
                }
            }
        }
        //System.out.println("have region size " + region.size());

        final HashSet locations = region.getLocations();

        double runTotal = this.runTotal;
        double riseTotal = this.riseTotal;
        int runCount = this.runCount;
        int riseCount = this.riseCount;

        final double colorTotal = getColorTotal(location);
        final Location right = location.getRight();
        if (right != null && locations.contains(right)) {
            runTotal += (getColorTotal(right) - colorTotal);
            runCount++;
        }
        final Location down = location.getDown();
        if (down != null && locations.contains(down)) {
            riseTotal += getColorTotal(down) - colorTotal;
            riseCount++;
        }
        final Rectangle rectangle = this.rectangle.getRectangle();
        rectangle.add(location.getX(), location.getY());

        double run = 0;
        if (runCount > 0) {
            run = runTotal / runCount;
        }
        double rise = 0;
        if (riseCount > 0) {
            rise = riseTotal / riseCount;
        }

        Line2D line = Util.getGradientLine(run, rise, rectangle.getWidth(), rectangle.getHeight(), rectangle.getX1(), rectangle.getY1());
        if (line == null) {
            System.out.println("bad gradient line for " + run + " " + rise + " " + rectangle);
            //something didn't add up
            return false;
        }

        //don't leave method with location still in locations
        locations.add(location);
        line = trimToShape(locations, line); 
        //System.out.println("trimmed line is " + line.getP1() + " " + line.getP2());

        //get samples along gradient line
        final SampleContainer sampler = new SampleContainer();
        final Iterator iterator = locations.iterator();
        while (iterator.hasNext()) {
            final Location aLocation = (Location)iterator.next();
            final Point2D point = new Point2D.Double(aLocation.getX(), aLocation.getY());
            final Point2D nearestPoint = Util.getNearestPointOnLine(point, line);
            if (nearestPoint == null) {
                System.err.println("uh -oh!");
                System.err.println("for point " + point);
                System.err.println("and line " + line.getP1() + " " +
                        line.getP2());
                System.err.println("and nearestPointOnLine " +
                        Util.getNearestPointOnLine(point, line));
            }
            final double distance = Util.length(line.getP1(), nearestPoint);
            sampler.add(distance, getColorTotal(aLocation));
        }

        locations.remove(location);

        if (sampler.size() < 3) {
            return true;
        }

        final NonLinearCurve2D curve = new NonLinearCurve2D();
        final Iterator samples = sampler.iterator();
        while (samples.hasNext()) {
            final Sample sample = (Sample)samples.next();
            curve.add(sample.getX(), sample.getAverage());
            //System.out.println("adding lumin sample " + sample.getX() + " " + sample.getAverage());
        }

        final ReduceError reduceError = curve.getReduceError();
        //System.out.println("Have reduceError " + reduceError.error);
        if (reduceError == null) {
            System.err.println("LinearGradientParser ReduceError is null");
            return false;
        }
        if (reduceError.error < threshold) {
            gradientLine = line;
            this.curve = curve;
            this.quadCurve = curve.getQuadCurve2D();
            return true;
        }
        return false;
    }

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

}
