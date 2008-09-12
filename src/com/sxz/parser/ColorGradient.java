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

public final class ColorGradient extends Paint {

	private Gradient red;
	private Gradient green;
	private Gradient blue;

    public ColorGradient() {
        super();
    }

    //to avoid always processing the entire region everytime, handle all the
    //no duh cases here
    public boolean findError(Location location, double threshold) {
        //System.out.println("called ColorGradient.findError()");
        double difference = red.findError(location.getX(), location.getY(),
                location.getColor().getRed());
        //System.out.println("with difference " + difference);
        if (difference > threshold) {
            return false;
        }
        if (!red.compareTo(green)) {
            difference = green.findError(location.getX(),
                    location.getY(), location.getColor().getGreen());
            if (difference > threshold) {
                return false;
            }
        }
        if (!red.compareTo(blue)) {
            difference = blue.findError(location.getX(),
                    location.getY(), location.getColor().getBlue());
            if (difference > threshold) {
                return false;
            }
        }
        //System.out.println("ColorGradient.findError returning " + difference);
        return true;
    }

    //detect if gradients are the same type and equal
    public boolean compareTo() {
        if (red instanceof LinearGradient &&
                green instanceof LinearGradient &&
                blue instanceof LinearGradient) {
            if (red.compareTo(green) && red.compareTo(blue) &&
                    green.compareTo(blue)) {
                return true;
            }
        }
        return false;
    }

    public double getWorstError() {
        if (red == null || green == null || blue == null) {
            return Double.MAX_VALUE;
        }
        double worstError = red.getError();
        final double greenError = green.getError();
        if (greenError > worstError) {
            worstError = greenError;
        }
        final double blueError = blue.getError();
        if (blueError > worstError) {
            worstError = blueError;
        }
        
        return worstError;
    }

    public Gradient getRed() {
        return red;
    }

    public Gradient getGreen() {
        return green;
    }

    public Gradient getBlue() {
        return blue;
    }

    public void setRed(Gradient red) {
        this.red = red;
    }

    public void setGreen(Gradient green) {
        this.green = green;
    }

    public void setBlue(Gradient blue) {
        this.blue = blue;
    }

}
