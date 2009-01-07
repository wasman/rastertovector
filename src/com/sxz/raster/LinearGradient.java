package com.sxz.raster;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import com.sxz.math.NonLinearCurve2D;
import com.sxz.math.Util;

public class LinearGradient extends Gradient {

    private Line2D gradientLine;
    private NonLinearCurve2D curve;
    //TODO: cache arraylist of lines instead?
    private QuadCurve2D quadCurve;

    public LinearGradient() {
        super();
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

    public double getValue(double x, double y) {
        if (quadCurve == null) {
            quadCurve = curve.getQuadCurve2D();
            if (quadCurve == null) {
                return Double.MAX_VALUE;
            }
        }
        final Point2D point = new Point2D.Double(x, y);
        final double length = Util.length(gradientLine);
        final Point2D nearestPoint = Util.getNearestPointOnLine(point,
                gradientLine);
        if (nearestPoint == null) {
            return 0.0;
            /*
            System.err.println("Ahh!!!");
            System.out.println("look for nearest point to " + point);
            System.out.println("on line " + gradientLine.getP1() + " " + gradientLine.getP2());
            System.out.println("with length " + length);
            System.out.println("and curve " + curve);
            System.out.println("and have " + nearestPoint);
            */
        }

        double distance = Util.length(gradientLine.getP1(), nearestPoint);
        //lame - rounding error
        if (distance > length) {
            distance = length;
        }
        //System.out.println("with distance " + distance);
        return curve.getValue(quadCurve, distance);
    }
}
