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

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.geom.Point2D;

public final class SampleContainer {

	private TreeMap values;

	public SampleContainer() {
		super();
		values = new TreeMap(new DoubleComparator());
	}

	public int size() {
		return values.size();
	}

	public void clear() {
		values.clear();
	}

	public double first() {
		final Sample sample = (Sample)values.get(values.firstKey());
		return sample.getAverage();
	}

	public void add(double x, double y) {
		final Double key = new Double(x);
		Sample sample = (Sample)values.get(key);
		if (sample == null) {
			sample = new Sample(x, y);
			values.put(key, sample);
			return;
		}
		sample.add(y);
	}
 
	public void remove(double x, double y) {
		final Double key = new Double(x);
		final Sample sample = (Sample)values.get(key);
		if (sample == null) {
			sample.remove(y);
			return;
		}
		sample.remove(y);
	}

	public Iterator iterator() {
		final ArrayList result = new ArrayList();
		final Iterator iterator = values.keySet().iterator();
		while (iterator.hasNext()) {
			final Double key = (Double)iterator.next();
			final Sample sample = (Sample)values.get(key);
			result.add(sample);
		}
		return result.iterator();
	}

}
