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

import java.util.Comparator;

//should sort edge locations only - clockwise
public final class NearestLocationComparator implements Comparator {

	private Color color;
	public NearestLocationComparator() {
		super();
	}

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

	public int compare(Object o1, Object o2){
		final Location location1 = (Location)o1;
		final Location location2 = (Location)o2;
		final Color color1 = location1.getColor();
		final Color color2 = location2.getColor();
		return (int)(Color.getColorDistance(color, color1) -
				Color.getColorDistance(color, color2));
	}
}
