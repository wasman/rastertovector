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

public final class NearestRegionComparator implements Comparator {
	public static final NearestRegionComparator COMPARATOR = new NearestRegionComparator();

	private NearestRegionComparator() {
		super();
	}

	public int compare(Object o1, Object o2){
		final NearestRegion nearest1 = (NearestRegion)o1;
		final NearestRegion nearest2 = (NearestRegion)o2;
        return (int)(nearest1.getDifference() - nearest2.getDifference());
	}
}
