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

import java.awt.geom.Point2D;

public final class Rectangle {
    //upper left
    private double x1;
    private double y1;
    //lower right
    private double x2;
    private double y2;

	public Rectangle() {
        x1 = -1;
        y1 = -1;
        x2 = -1;
        y2 = -1;
	}

	public Rectangle(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
	}

    public double getX1() {
        return x1;
    }

    public double getX2() {
        return x2;
    }

    public double getY1() {
        return y1;
    }

    public double getY2() {
        return y2;
    }

    public double getWidth() {
        return x2 - x1;
    }

    public double getHeight() {
        return y2 - y1;
    }

    public void add(Point2D point) {
        add(point.getX(), point.getY());
    }

    public void add(Rectangle rectangle) {
        add(rectangle.getX1(), rectangle.getY1());
        add(rectangle.getX2(), rectangle.getY2());
    }

    public void add(double x, double y) {
        if (x1 == -1) {
            x1 = x;
        }
        if (x2 == -1) {
            x2 = x;
        }
        if (y1 == -1) {
            y1 = y;
        }
        if (y2 == -1) {
            y2 = y;
        }
        if (x < x1) {
            x1 = x;
        } else if (x > x2) {
            x2 = x;
        }
        if (y < y1) {
            y1 = y; 
        } else if (y > y2) {
            y2 = y;
        }
    }

    public boolean contains(Rectangle rectangle) {
        if (rectangle.getX1() < x1) {
            return false;
        }
        if (rectangle.getX2() > x2) {
            return false;
        }
        if (rectangle.getY1() < y1) {
            return false;
        }
        if (rectangle.getY2() > y2) {
            return false;
        }
        return true;
    }

    public String toString() {
        return x1 + " " + y1 + " " + x2 + " " + y2;
    }

    public Rectangle getRectangle() {
        final Rectangle result = new Rectangle(x1, y1, x2, y2);
        return result;
    }

    public static void main(String[] args) {
        final Rectangle rectangle = new Rectangle(0, 0, 4, 4);
        final Rectangle rectangle2= new Rectangle(0, 2, 2, 3);
        System.out.println(rectangle.contains(rectangle2));
        System.out.println(rectangle2.contains(rectangle));
    }
}
