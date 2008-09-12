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

import java.util.ArrayList;
import java.util.Iterator;
import java.awt.geom.Point2D;

public final class Sample {

	private ArrayList values;
	private double x;

	public Sample() {
		super();
		values = new ArrayList();
	}

	public Sample(Point2D point) {
		this();
		this.x = point.getX();
		add(point.getY());
	}

	public Sample(double x, double value) {
		this();
		this.x = x;
		add(value);
	}

	public ArrayList getValues() {
		return values;
	}

	public void setValues(ArrayList values) {
		this.values = values;
	}

	public boolean remove(double value) {
		return values.remove(new Double(value));
	}

	public int getSize() {
		return values.size();
	}

	public void add(double value) {
		values.add(new Double(value));
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getAverage() {
		if (values.size() == 0) {
			return 0.0;
		}
		double result = 0.0;
		final Iterator iterator = values.iterator();
		while (iterator.hasNext()) {
			final Double value = (Double)iterator.next();
			result += value.doubleValue();
		}
		return result / values.size();
	}

	public Point2D getPoint() {
		return new Point2D.Double(x, getAverage());
	}

	public double findDeviation(double y) {
		return Math.abs(getAverage() - y); 
	}

	public double findWorstError(Point2D point) {
		double result = 0.0;
		final Iterator iterator = values.iterator();
		while (iterator.hasNext()) {
			final Double value = (Double)iterator.next();
			final Point2D source = new Point2D.Double(x, value.doubleValue());
			final double error = Math.abs(point.distance(source)); 
			if (error > result) {
				result = error;
			}
		}
		return result;
	}

	public double findTotalError(Point2D point) {
		double result = 0.0;
		final Iterator iterator = values.iterator();
		while (iterator.hasNext()) {
			final Double value = (Double)iterator.next();
			final Point2D source = new Point2D.Double(x, value.doubleValue());
			final double error = Math.abs(point.distance(source)); 
			result += error;
		}
		return result;
	}

	public Object clone() {
		final Sample result = new Sample();
		result.setX(x);
		result.setValues((ArrayList)getValues().clone());

		return result;
	}

	public String toString() {
		return "x: " + x + " y: " + getPoint().getY();
	}
}
