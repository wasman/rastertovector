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

public final class FillColor {

	private Color color;

    public FillColor(Color color) {
        super();
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean findError(Location location) {
        return color.equals(location.getColor());
    }

    public boolean equals(Object object) {
        if (object instanceof FillColor) {
            final FillColor otherPaint = (FillColor)object;
            return color.equals(otherPaint.getColor());
        }
        return false;
    }
    
    public String toString() {
        return color.toString();
    }
}
