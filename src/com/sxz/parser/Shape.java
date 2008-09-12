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

import java.util.ArrayList;
import java.util.Iterator;
import java.awt.geom.Point2D;

public abstract class Shape {

	private ArrayList locations;

	public Shape() {
		locations = new ArrayList();
	}

	public void addLocation(Location location) {
		locations.add(location);
	}

	public void addLocations(ArrayList locations) {
		this.locations.addAll(locations);
	}

	public ArrayList getLocations() {
		return locations;
	}

    public void setLocations(ArrayList locations) {
        this.locations = locations;
    }

	public int size() {
		return locations.size();
	}

	public Iterator iterator() {
		return locations.iterator();
	}
}
