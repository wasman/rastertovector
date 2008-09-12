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

public final class RegionComparator implements Comparator {

	public static final RegionComparator COMPARATOR = new RegionComparator();

	private RegionComparator() {
		super();
	}

	public int compare(Object o1, Object o2){
		final Region region1 = (Region)o1;
		final Region region2 = (Region)o2;
		return region1.size() - region2.size();
	}
}
