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
import com.sxz.math.NonLinearCurve2D;
import com.sxz.math.Util;

public class LinearGradient extends Gradient {

    private Line2D gradientLine;
    private NonLinearCurve2D curve;
    private QuadCurve2D quadCurve;
    private double valueAtOrigin;

    public LinearGradient() {
        super();
        quadCurve = null;
    }

    public Line2D getGradientLine() {
        return gradientLine;
    }

    public void setGradientLine(Line2D gradientLine) {
        this.gradientLine = gradientLine;
    }

    public NonLinearCurve2D getCurve() {
        return curve;
    }

    public void setCurve(NonLinearCurve2D curve) {
        this.curve = curve;
        quadCurve = null;
    }

    public double getValueAtOrigin() {
        return valueAtOrigin;
    }

    public void setValueAtOrigin(double valueAtOrigin) {
        this.valueAtOrigin = valueAtOrigin;
    }

    public double getEnd() {
        return curve.getEndPoint().getY();
    }

    public double findError(double x, double y, double value) {
        //final long start = System.currentTimeMillis();

        final Point2D point = new Point2D.Double(x, y);
        final Point2D nearestPoint = Util.getNearestPointOnLineSegment(point,
                gradientLine);
        if (nearestPoint == null) {
            //System.out.println("nearestPoint is null in LinearGradient");
            //System.out.println("for " + x + " " + y + " and value " + value);
            //System.out.println("and line " + gradientLine.getP1() + " " + gradientLine.getP2());
            //bail out if we are not within the gradient
            return Double.MAX_VALUE;
        }
        final double distance = Util.length(gradientLine.getP1(), nearestPoint);
        if (quadCurve == null) {
            quadCurve = curve.getQuadCurve2D();
        }
        final double actualValue = curve.getValue(quadCurve, distance);

        if (Double.isNaN(actualValue)) {
            System.err.println("actualValue is NaN in LinearGradient");
            System.out.println("for point " + point);
            System.out.println("for nearestPoint " + nearestPoint);
            System.out.println("and distance " + distance);
            System.out.println("for line " + gradientLine.getP1() + " " + gradientLine.getP2());
            System.out.println("and curve " + curve);
            return Double.MAX_VALUE;
        }

        //final long end = System.currentTimeMillis();
        //System.out.println("LinearGradient.findError 2 time is " + (end - start));
        return Math.abs(value - actualValue);
    }

    public boolean compareTo(Gradient gradient) {
        final LinearGradient linearGradient = (LinearGradient)gradient;
        return gradientLine.getP1().equals(linearGradient.getGradientLine().getP1()) &&
            gradientLine.getP2().equals(linearGradient.getGradientLine().getP2()) &&
            getReduceError().equals(linearGradient.getReduceError()) &&
            valueAtOrigin == linearGradient.getValueAtOrigin();
    }
}
