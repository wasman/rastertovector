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
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeMap;

public final class FrameParser {

	private Frame frame;
	private LocationPool locationPool;
	private double threshold;
	private double separation;
    private NearestLocationComparator comparer;

	public FrameParser(LocationPool locationPool) {
		super();
		this.locationPool = locationPool;
		frame = new Frame();
		threshold = 20.0;
        //Just make something up here
		separation = 45.0;
        comparer = new NearestLocationComparator();
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public double getSeparation() {
		return separation;
	}

	public void setSeparation(double separation) {
		this.separation = separation;
	}

	public Frame getFrame() {
		return frame;
	}

	//sets the edge locations for all the regions
	public void setEdges() {
		final Iterator iterator = frame.iterator();
		while (iterator.hasNext()) {
			final Region region = (Region)iterator.next();
			region.setEdges();
		}
	}

	public int getWidth() {
		return locationPool.getWidth();
	}

	public int getHeight() {
		return locationPool.getHeight();
	}

	//what is this for?
	private void addNeighbors(Location seed, ArrayList todoList, HashSet alreadyChecked) {
		final int w = getWidth();
		final int h = getHeight();

		final int x = seed.getX();
		final int y = seed.getY();
		final Region region = seed.getRegion();
		if (x > 0) {
			final Location location = locationPool.getLocation(x - 1, y);
            if (location != null) {
                if (location.isMarked()) {
                    final Region neighbor = location.getRegion();
                    if (neighbor != null && neighbor != region && !region.isNeighbor(neighbor)) {
                        region.addNeighbor(neighbor);
                    }
                } else if (!alreadyChecked.contains(location) && !todoList.contains(location)) {
                    if (seed.getColor().equals(location.getColor())) {
                        todoList.add(0, location);
                    } else {
                        todoList.add(location);
                    }
                }
			}
		}
		if (x < (w - 1)) {
			final Location location = locationPool.getLocation(x + 1, y);
            if (location != null) {
                if (location.isMarked()) {
                    final Region neighbor = location.getRegion();
                    if (neighbor != null && neighbor != region && !region.isNeighbor(neighbor)) {
                        region.addNeighbor(neighbor);
                    }
                } else if (!alreadyChecked.contains(location) && !todoList.contains(location)) {
                    if (seed.getColor().equals(location.getColor())) {
                        todoList.add(0, location);
                    } else {
                        todoList.add(location);
                    }
                }
			}
		}
		if (y < (h - 1)) {
			final Location location = locationPool.getLocation(x, y + 1);
            if (location != null) {
                if (location.isMarked()) {
                    final Region neighbor = location.getRegion();
                    if (neighbor != null && neighbor != region && !region.isNeighbor(neighbor)) {
                        region.addNeighbor(neighbor);
                    }
                } else if (!alreadyChecked.contains(location) && !todoList.contains(location)) {
                    if (seed.getColor().equals(location.getColor())) {
                        todoList.add(0, location);
                    } else {
                        todoList.add(location);
                    }
                }
			}
		}
		if (y > 0) {
			final Location location = locationPool.getLocation(x, y - 1);
            if (location != null) {
                if (location.isMarked()) {
                    final Region neighbor = location.getRegion();
                    if (neighbor != null && neighbor != region && !region.isNeighbor(neighbor)) {
                        region.addNeighbor(neighbor);
                    }
                } else if (!alreadyChecked.contains(location) && !todoList.contains(location)) {
                    if (seed.getColor().equals(location.getColor())) {
                        todoList.add(0, location);
                    } else {
                        todoList.add(location);
                    }
                }
			}
		}
	}

	//let's put all the image pixels into regions
	//aka statistical segmentation
	public void process() {
		//in case we run this again - clean things up first
		frame.clear();

		final int w = getWidth();
		final int h = getHeight();
        frame.setWidth(w);
        frame.setHeight(h);

		final ArrayList todoList = new ArrayList();
		Location location = null;
		Region region = null;

		final HashSet alreadyChecked = new HashSet();
		//outer loop
		while (!locationPool.isEmpty()) {
			alreadyChecked.clear();
			//this is the new region we are currently working with
			Location seed = locationPool.getRandomLocation();
            /*
            if (region != null) {
                System.out.println("region had size " + region.size());
            }
            */

			region = new Region(seed);
			//System.out.println("initial seed for new region is " + seed);
			alreadyChecked.add(seed);
			addNeighbors(seed, todoList, alreadyChecked);
            comparer.setColor(seed.getColor());
			Collections.sort(todoList, comparer);

			//the seed will be the first and only location in the new region
			//check for the seed location's gradient here before continuing
			//adjust the todoList as appropriate to put additional locations in

			//inner loop
			while (!todoList.isEmpty()) {
				seed = (Location)todoList.remove(0);
				//System.out.println("todoList popped off " + seed);
				if (seed.isMarked()) {
					//this is a bad thing
					System.err.println("processing a marked location!");
				}

				alreadyChecked.add(seed);

				final Color color = seed.getColor();

				final int x = seed.getX();
				final int y = seed.getY();

				if (region.findSeparation(seed) > separation) {
					//black and white do not make a gradient
                    //System.out.println("failed becuase of separation " + region.findSeparation(seed));
					continue;
				}
                //System.out.println("for region " + region);
                //System.out.println("with size " + region.size());
                //System.out.println("for location " + seed);
                //final long start = System.currentTimeMillis();
                final boolean goodEnough = region.findError(seed, threshold);
                //final long end = System.currentTimeMillis();
                //System.out.println("test time is " + (end - start) / 100.0);
                //final double worstError = paint.getWorstError();
				if (!goodEnough) {
					//if the pixel doesn't fit, you must acquit
					continue;
				}

                //region.setPaint(paint);
				region.add(seed);

				locationPool.setMarked(seed);

				//map neighbors to create graph here
				//left
				if (x > 0) {
					location = locationPool.getLocation(x - 1, y);
                    if (location != null) {
                        if (location.isMarked()) {
                            final Region neighbor = location.getRegion();
                            if (neighbor != null && neighbor != region && !region.isNeighbor(neighbor)) {
                                region.addNeighbor(neighbor);
                            }
                        } else if (!alreadyChecked.contains(location) && !todoList.contains(location)) {
                            if (seed.getColor().equals(location.getColor())) {
                                todoList.add(0, location);
                            } else {
                                todoList.add(location);
                            }
                        }
					}
				}

				//right
				if (x < (w - 1)) {
					location = locationPool.getLocation(x + 1, y);
                    if (location != null) {
                        if (location.isMarked()) {
                            final Region neighbor = location.getRegion();
                            if (neighbor != null && neighbor != region && !region.isNeighbor(neighbor)) {
                                region.addNeighbor(neighbor);
                            }
                        } else if (!alreadyChecked.contains(location) && !todoList.contains(location)) {
                            if (seed.getColor().equals(location.getColor())) {
                                todoList.add(0, location);
                            } else {
                                todoList.add(location);
                            }
                        }
					}
				}

				//top
				if (y > 0) {
					location = locationPool.getLocation(x, y - 1);
                    if (location != null) {
                        if (location.isMarked()) {
                            final Region neighbor = location.getRegion();
                            if (neighbor != null && neighbor != region && !region.isNeighbor(neighbor)) {
                                region.addNeighbor(neighbor);
                            }
                        } else if (!alreadyChecked.contains(location) && !todoList.contains(location)) {
                            if (seed.getColor().equals(location.getColor())) {
                                todoList.add(0, location);
                            } else {
                                todoList.add(location);
                            }
                        }
					}
				}

				//bottom
				if (y < (h - 1)) {
					location = locationPool.getLocation(x, y + 1);
                    if (location != null) {
                        if (location.isMarked()) {
                            final Region neighbor = location.getRegion();
                            if (neighbor != null && neighbor != region && !region.isNeighbor(neighbor)) {
                                region.addNeighbor(neighbor);
                            }
                        } else if (!alreadyChecked.contains(location) && !todoList.contains(location)) {
                            if (seed.getColor().equals(location.getColor())) {
                                todoList.add(0, location);
                            } else {
                                todoList.add(location);
                            }
                        }
					}
				}
                comparer.setColor(seed.getColor());
				Collections.sort(todoList, comparer);
			}

			frame.addRegion(region);
		}
        //System.out.println("total region size is " + frame.size());
        //frame.mergeSmall(2, separation);
        //System.out.println("total region size after merge is " + frame.size());
        frame.setChildRegions();
	}

	public Location getLocation(int x, int y) {
		return locationPool.getLocation(x, y);
	}
}
