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
import java.util.HashSet;
import java.util.Iterator;
import java.awt.image.BufferedImage;

public final class LocationPool {

	private Location[][] locations;
	private HashSet unmarked;

	public LocationPool(BufferedImage bufferedImage) {
		super();
		process(bufferedImage);
	}

	public int getWidth() {
		return locations.length;
	}

	public int getHeight() {
		return locations[0].length;
	}

	public void setMarked(Location location) {
		if (location.isMarked()) {
			new Exception().printStackTrace(System.out);
			System.out.println("Already marked " + location.getX() + " : " +
					location.getY());
		}
		location.setMarked(true);
		unmarked.remove(location);
	}

	public boolean isEmpty() {
		return unmarked.isEmpty();
	}

	public Location getLocation(int x, int y) {
		return locations[x][y];
	}

	//this isn't really random
	public Location getRandomLocation() {
	/*
		if (unmarked.isEmpty()) {
			return null;
		}
	*/

		final Iterator iterator = unmarked.iterator();
		final Location location = (Location)iterator.next();
		iterator.remove();
		location.setMarked(true);
		return location;
	}

	//make sure to call process and setBufferedImage before calling anything
	//else
	private void process(BufferedImage bufferedImage) {
		final int w = bufferedImage.getWidth();
		final int h = bufferedImage.getHeight();

		//not sure about alpha yet
		final boolean hasAlpha = bufferedImage.getColorModel().hasAlpha();

		final int[] pixels = bufferedImage.getRGB(0, 0, w, h, null, 0, w);
		unmarked = new HashSet(w * h);
		locations = new Location[w][h];
		for (int i = 0; i <	h; i++) {
			for (int j = 0; j < w; j++) {
				final Location location = new Location(j, i);
				final int rgb = pixels[i * w + j];
                if (hasAlpha) {
                    final int alpha = Color.getAlpha(rgb);
                    if (alpha == 0) {
                        continue;
                    } else {
                        final Color color = new Color(rgb, alpha);
                        location.setColor(color);
                    }

                } else {
                    final Color color = new Color(rgb);
                    location.setColor(color);
                }
				locations[j][i] = location;
				unmarked.add(location);
                //location.getColor().convertToYCbCr();
			}
		}
		setNeighbors(w, h);
	}

	//the location neighbors - not region neighbors
	private void setNeighbors(int w, int h) {

		for (int i = 0; i <	h; i++) {
			for (int j = 0; j < w; j++) {
				final Location location = locations[j][i];
                if (location == null) {
                    continue;
                }
				if (j > 0) {
					location.setLeft(locations[j - 1][i]);
					if (i > 0) {
						location.setUpperLeft(locations[j - 1][i - 1]);
					}
					if (i < h - 1) {
						location.setLowerLeft(locations[j - 1][i + 1]);
					}
				}
				if (j < (w - 1)) {
					location.setRight(locations[j + 1][i]);
					if (i > 0) {
						location.setUpperRight(locations[j + 1][i - 1]);
					}
					if (i < h - 1) {
						location.setLowerRight(locations[j + 1][i + 1]);
					}
				}
				if (i > 0) {
					location.setUp(locations[j][i - 1]);
				}
				if (i < (h - 1)) {
					location.setDown(locations[j][i + 1]);
				}
			}
		}
	}

}
