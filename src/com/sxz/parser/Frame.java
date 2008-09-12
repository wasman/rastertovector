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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Frame {
	private HashSet regions;
	private int width;
	private int height;

	public Frame() {
		super();
		regions = new HashSet();
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public HashSet getRegions() {
		return (HashSet)regions.clone();
	}

	public void remove(Region region) {
		regions.remove(region);
	}

	public void addRegion(Region region) {
		if (regions.contains(region)) {
			System.err.println("Error: duplicate region");
		}
		regions.add(region);
	}

	public int size() {
		return regions.size();
	}

	public Iterator iterator() {
		return regions.iterator();
	}

	public void clear() {
		regions.clear();
	}

	public void setChildRegions() {
		final Iterator iterator = iterator();
		while (iterator.hasNext()) {
			final Region region = (Region)iterator.next();
			region.setChildRegions();
		}
	}

	/**
	 * Go through every region and find nearest neighbors
	 */
	public void merge(int regionCount, double maximumError) {
		System.out.println("starting mergeNearest with region count " + size());

		final List nearestRegions = new ArrayList(size());
		getNearestRegions(nearestRegions);

		while (nearestRegions.size() > 0 && size() > regionCount) {
			final NearestRegion nearestRegion =
                    (NearestRegion)nearestRegions.remove(0);

			if (nearestRegion.getDifference() > maximumError) {
				break;
			}
			final Region region = nearestRegion.getRegion();
			final Region neighborRegion = nearestRegion.getNearestNeighbor();

			if (!region.merge(neighborRegion)) {
				System.err.println("Failed to merge region " + region + " with " + neighborRegion);
				continue;
			}
			regions.remove(neighborRegion);
            nearestRegions.clear();
            getNearestRegions(nearestRegions);
		}
		System.out.println("ending mergeNearest with region count " + size());
	}

	private void getNearestRegions(List nearestRegions) {
		final Iterator iterator = iterator();
		while (iterator.hasNext()) {
			final Region region = (Region)iterator.next();
			final NearestRegion nearestRegion = region.getNearestRegion();
			if (nearestRegion == null) {
				continue;
			}
			nearestRegions.add(nearestRegion);
		}
		Collections.sort(nearestRegions, NearestRegionComparator.COMPARATOR);
	}

	public void mergeSmall(int minimumRegionSize, double separation) {
		List list = getSortedList();
        final HashSet skip = new HashSet();
		while (true) {
            final Region region = (Region)list.remove(0);
            //System.out.println("starting with region size " + region.size());
            //System.out.println("with paint " + region.getPaint());
			if (region == null || region.size() > minimumRegionSize) {
                skip.add(region);
				break;
			}
			final NearestRegion nearestRegion = region.getNearestRegion();
            //TODO: pull 64 from the frameparser separation
			if (nearestRegion != null && nearestRegion.getDifference() < separation) {
                final Region closestNeighbor = nearestRegion.getNearestNeighbor();
                /*
                System.out.println("merging with neighbor size " + closestNeighbor.size());
                System.out.println("with paint " + closestNeighbor.getPaint());
                System.out.println("with distance " + nearestRegion.getDifference());
                */
				closestNeighbor.merge(region);

                sortList(list);

			} else {
                skip.add(region);
                
				//debug code ....
                /*
				System.err.println("Error:Could not find a closest neighbor for ");
				System.err.println(region);
				final Iterator iterator = region.neighbors();
				while (iterator.hasNext()) {
					final Region neighbor = (Region)iterator.next();
					System.err.println("neighbor " + neighbor);

				}
                */
				//break;
			}
		}
		regions = new HashSet(list);
        regions.addAll(skip);
	}

	public static List getSortedList(HashSet regions) {
		final ArrayList result = new ArrayList(regions);
		Collections.sort(result, RegionComparator.COMPARATOR);	
		return result;
	}

	public List getSortedList() {
		return getSortedList(regions);
	}

	public List getReversedSortedList() {
		List result = getSortedList(regions);
		Collections.reverse(result);
		return result;
	}

	public static void sortList(List result) {
		Collections.sort(result, RegionComparator.COMPARATOR);	
	}

}
