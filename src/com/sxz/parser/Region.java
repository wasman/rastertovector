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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;
import java.util.Comparator;
import java.awt.image.BufferedImage;
import java.awt.geom.Point2D;

public class Region {

	private static final int NONE = -1;
	private static final int UP = 0;
	private static final int UPPER_RIGHT = 1;
	private static final int RIGHT = 2;
	private static final int LOWER_RIGHT = 3;
	private static final int DOWN = 4;
	private static final int LOWER_LEFT = 5;
	private static final int LEFT = 6;
	private static final int UPPER_LEFT = 7;

	private HashSet locations;
	private HashSet neighbors;
	private HashSet children;
	private HashSet parents;
	private FillColor fillColor;
    private LuminanceGradientParser lumin;

	public Region(Location location) {
		super();
		locations = new HashSet(100);
		neighbors = new HashSet();
		children = new HashSet();
		parents = new HashSet();
        fillColor = new FillColor(location.getColor());
        add(location);
	}

	public Region() {
		super();
		locations = new HashSet(100);
		neighbors = new HashSet();
		children = new HashSet();
		parents = new HashSet();
	}

    public boolean findError(Location location, double threshold) {
        //first check against the existing gradient for some circumstances
        if (fillColor != null) {
            final boolean result = fillColor.findError(location);
            if (result) {
                //System.out.println("using existing fillColor");
                return true;
            }
            final Rectangle rectangle = getBoundingBox();
            if (rectangle.getWidth() > 8 && rectangle.getHeight() > 8) {
                //System.out.println("FillColor is set");
                return false;
            }
        } else {
            final boolean result = lumin.findError(this, location, threshold);
            if (!result) {
                //System.out.println("Failed lumin test with " + result);
            }
            return result;
        }
        //fillColor failed to lets try lumin potentially instead

        final LuminanceGradientParser lumin = new LuminanceGradientParser();
        lumin.addAll(this, locations);
        final boolean result = lumin.findError(this, location, threshold);
        if (result) {
            this.lumin = lumin;
            fillColor = null;
        }
        return result;
    }

    public void setFillColor(FillColor fillColor) {
        this.fillColor = fillColor;
    }

    public FillColor getFillColor() {
        return fillColor;
    }

	public boolean isChildLocation(Location location) {
		final Iterator iterator = children.iterator();
		while (iterator.hasNext()) {
			final Region region = (Region)iterator.next();
			if (region.contains(location)) {
				return true;
			}
		}
		return false;
	}

	public boolean contains(Location location) {
		return locations.contains(location);
	}

	public boolean contains(int x, int y) {
		final Iterator iterator = locations.iterator();
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
			if (location.getX() == x && location.getY() == y) {
				return true;
			}
		}
		return false;
	}

	public boolean isNeighbor(Region region) {
		return neighbors.contains(region);
	}

	//really upper left location
	public static Location getLeftMostLocation(HashSet locations) {
		Location leftMost = null;
		final Iterator iterator = locations.iterator();
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
			if (leftMost == null || location.getX() < leftMost.getX()) {
				leftMost = location;
			} else if (location.getX() == leftMost.getX() && location.getY() < leftMost.getY()) {
				leftMost = location;
			}
		}
		return leftMost;
	}

	public static Location getLowerLeftMostLocation(HashSet locations) {
		Location leftMost = null;
		final Iterator iterator = locations.iterator();
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
			if (leftMost == null || location.getX() < leftMost.getX()) {
				leftMost = location;
				//notice change of equality sign for Y
			} else if (location.getX() == leftMost.getX() && location.getY() > leftMost.getY()) {
				leftMost = location;
			}
		}
		return leftMost;
	}

	//are we even calling this?
	public Location getUpperLeftMostLocation(HashSet locations) {
		Location leftMost = null;
		final Iterator iterator = locations.iterator();
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
			if (leftMost == null || (location.getX() < leftMost.getX() &&
					location.getY() < leftMost.getY())) {
				leftMost = location;
			}
		}
		return leftMost;
	}

	public Rectangle getBoundingBox() {
        Rectangle result = new Rectangle();
		final Iterator iterator = locations.iterator();
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
            result.add(location.getX(), location.getY());
		}
		return result;
	}

	public boolean isEdge() {
		final Iterator iterator = locations.iterator();
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
			if (location.getRight() == null ||
					location.getLeft() == null ||
					location.getUp() == null ||
					location.getDown() == null) {
				return true;
			}
		}
		return false;
	}

	public void addParent(Region parent) {
		parents.add(parent);
	}

	public HashSet getParents() {
		return parents;
	}

	public void setChildRegions() {
		/*
        if (lumin != null) {
            //gradients aren't easily extended so hold off on them for now
            return;
        }
		*/
		children = getChildRegions();
		final Iterator iterator = children.iterator();
		while (iterator.hasNext()) {
			final Region childRegion = (Region)iterator.next();
            //System.out.println("adding " + childRegion.getBoundingBox() + " to " + this.getBoundingBox());
			childRegion.addParent(this);
		}
	}

	//tricky little method
	public HashSet getChildRegions() {
		//System.out.println("called getChildRegions for " + this.hashCode());
		final HashSet result = new HashSet();
		if (neighborSize() == 0) {
			//System.out.println("No neighbors");
			return result;
		}

		//result holds successes only
		//alreadyChecked holds failures and successes
		final HashSet alreadyChecked = new HashSet();
		final HashSet failures = new HashSet();
		final Rectangle boundingBox = getBoundingBox();

		final Iterator iterator = neighbors();
		while (iterator.hasNext()) {
			final Region neighbor = (Region)iterator.next();
			//System.out.println("testing " + neighbor.hashCode());
			//this shouldn't need to be tested
			if (neighbor == this) {
				//System.out.println("neighbor is this region");
				continue;
			}

			if (alreadyChecked.contains(neighbor)) {
				//System.out.println("already checked neighbor " + neighbor.hashCode());
				continue;
			}

			alreadyChecked.add(neighbor);

			if (!boundingBox.contains(neighbor.getBoundingBox())) {
                //System.out.println("getChildRegions neighbor is not withinbounding box " + neighbor.hashCode() + " " + neighbor.getBoundingBox());
				failures.add(neighbor);
				continue;
			}

			//System.out.println("getChildRegions neighbor is withinbounding box or is edge of " + this.getBoundingBox() + " and child " + neighbor.getBoundingBox());
			result.add(neighbor);
		
			final HashSet childSet = new HashSet();
			neighborRecurse(neighbor, childSet, failures, alreadyChecked, boundingBox);
			result.addAll(childSet);
		}
		//System.out.println("children final size for " + this.hashCode() + " is " + result.size());
		//System.out.println("children final size for " + this + " is " + result.size());
		return result;
	}

	private void neighborRecurse(Region region, HashSet result, HashSet failures, HashSet alreadyChecked, Rectangle boundingBox) {
		//System.out.println("called neighborRecurse for " + region.hashCode());
		final Iterator iterator = region.neighbors();
		while (iterator.hasNext()) {
			final Region neighbor = (Region)iterator.next();
			//System.out.println("testing " + neighbor.hashCode());
			if (neighbor == this) {
				//System.out.println("recurseNeighbor neighbor is this region");
				continue;
			}

			if (alreadyChecked.contains(neighbor)) {
				//System.out.println("alreadyChecked so continue");
				//return false;
				continue;
			}

			alreadyChecked.add(neighbor);

			//if (!withinBoundingBox(neighbor, boundingBox)) {
			if (!boundingBox.contains(neighbor.getBoundingBox())) {
				failures.add(neighbor);

				//return false;
				continue;
			}

			//System.out.println("neighborRecurse neighbor is withinbounding box or is edge of id# " + this.hashCode() + " has child# " + neighbor.hashCode());
			result.add(neighbor);
			neighborRecurse(neighbor, result, failures, alreadyChecked, boundingBox);
		}

		//return true;
		return;
	}

	public void setEdges() {
		final Iterator iterator = iterator();
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
			location.setEdge(isEdge(location));
		}
		final Iterator childrenIterator = children.iterator();
		while (childrenIterator.hasNext()) {
			final Region child = (Region)childrenIterator.next();
			final Iterator childLocationIterator = child.iterator();
			while (childLocationIterator.hasNext()) {
				final Location childLocation = (Location)childLocationIterator.next();
				childLocation.setEdge(isEdge(childLocation));
			}
		}
	}

	public boolean isSameRegionOrChild(Location location) {
		return location.getRegion() == this || children.contains(location.getRegion());
	}

	private boolean isEdge(Location location) {
		final Location up = location.getUp();
		if (up == null || !isSameRegionOrChild(up)) {
			return true;
		}

		final Location down = location.getDown();
		if (down == null || !isSameRegionOrChild(down)) {
			return true;
		}

		final Location left = location.getLeft();
		if (left == null || !isSameRegionOrChild(left)) {
			return true;
		}

		final Location right = location.getRight();
		if (right == null || !isSameRegionOrChild(right)) {
			return true;
		}
/*
        final Location upperLeft = location.getUpperLeft();
        if (upperLeft == null || !isSameRegionOrChild(upperLeft)) {
            return true;
        }

        final Location upperRight = location.getUpperRight();
        if (upperRight == null || !isSameRegionOrChild(upperRight)) {
            return true;
        }
        final Location lowerLeft = location.getLowerLeft();
        if (lowerLeft == null || !isSameRegionOrChild(lowerLeft)) {
            return true;
        }

        final Location lowerRight = location.getLowerRight();
        if (lowerRight == null || !isSameRegionOrChild(lowerRight)) {
            return true;
        }
*/
		return false;
	}

	public double findSeparation(Location location) {
        //find neighboring locations
        double most = 0.0;
        final Location left = location.getLeft();
        if (left != null && left.getRegion() == this) {
            final double distance = location.getColorDistance(left);
            if (distance > most) {
                most = distance;
            }
        }
        final Location right = location.getRight();
        if (right != null && right.getRegion() == this) {
            final double distance = location.getColorDistance(right);
            if (distance > most) {
                most = distance;
            }
        }
        final Location up = location.getUp();
        if (up != null && up.getRegion() == this) {
            final double distance = location.getColorDistance(up);
            if (distance > most) {
                most = distance;
            }
        }
        final Location down = location.getDown();
        if (down != null && down.getRegion() == this) {
            final double distance = location.getColorDistance(down);
            if (distance > most) {
                most = distance;
            }
        }
        return most;
    }

	public boolean add(Location location) {
		locations.add(location);
		location.setRegion(this);
        if (lumin != null) {
            lumin.add(this, location);
        }
		return true;
	}

	public void addNeighbor(Region region) {
		neighbors.add(region);
		region.neighbors.add(this);
	}

	public void removeNeighbor(Region region) {
		neighbors.remove(region);
	}

	public void removeNeighbors() {
		final Iterator iterator = neighbors();
		while (iterator.hasNext()) {
			final Region region = (Region)iterator.next();
			region.removeNeighbor(this);
		}
	}

	//when merging regions use this method
	public void replaceNeighbor(Region newRegion) {
		final Iterator iterator = neighbors.iterator();
		while (iterator.hasNext()) {
			//this is the third party region to be transferred to the new
			//region
			final Region region = (Region)iterator.next();
			region.removeNeighbor(this);
			if (region != newRegion) {
				region.addNeighbor(newRegion);
			}
		}
		//this is just for laughs
		neighbors.clear();
	}

	public Iterator neighbors() {
		return neighbors.iterator(); 
	}

	public int neighborSize() {
		return neighbors.size();
	}

	public int size() {
		return locations.size();
	}

	public Iterator iterator() {
		return locations.iterator();
	}

	public HashSet getLocations() {
		return locations;
	}

    //this isn't practical for significant usage
    public Location getLocation(int x, int y) {
        final Iterator iterator = locations.iterator();
        while (iterator.hasNext()) {
            final Location location = (Location)iterator.next();
            if (location.getX() == x && location.getY() == y) {
                return location;
            }
        }
        return null;
    }

	public HashSet getEdgeLocations() {
		//System.out.println("Called getEdgeLocations on " + this.hashCode());
		final HashSet result = new HashSet();
		//set edges here to only matter for it and its children
		setEdges();

		//collect edges from this regions locations
		//keeping in mind that a location may not be an edge if this region
		//has a child location nearby
		final Iterator iterator = iterator();
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
			//if any neighbors of location are not its own region and
			//not a child region then it is not an edge
			if (location.isEdge()) {
				result.add(location);
			}
		}
		//TODO: iterate through children and add would be locations to this set
		final Iterator childIterator = children.iterator();
		while (childIterator.hasNext()) {
			final Region child = (Region)childIterator.next();
			//now loop over locations in region
			final Iterator childLocations = child.iterator();
			while (childLocations.hasNext()) {
				final Location location = (Location)childLocations.next();
				//do no add a polyline location
				if (location.isEdge() && !isPolyline(location, children)) {
					result.add(location);
				}
			}
		}

		return result;
	}

	public Location getNextPolylineLocation(Location currentLocation, HashSet edges, HashSet used) {
		//System.out.println("Called getNextPolylineLocation with " + currentLocation);
		final Region region = currentLocation.getRegion();
		final Location up = currentLocation.getUp();
		//System.out.println("up is " + up);
		if (up != null && (up.isEdge() && up.getRegion() == region && edges.contains(up)) && isPolyline(up, used)) {
			return up;
		}
		final Location down = currentLocation.getDown();
		if (down != null && (down.isEdge() && down.getRegion() == region && edges.contains(down)) && isPolyline(down, used)) {
			return down;
		}
		final Location left = currentLocation.getLeft();
		if (left != null && (left.isEdge() && left.getRegion() == region && edges.contains(left)) && isPolyline(left, used)) {
			return left;
		}
		final Location right = currentLocation.getRight();
		if (right != null && (right.isEdge() && right.getRegion() == region && edges.contains(right)) && isPolyline(right, used)) {
			return right;
		}
		//System.out.println("Woops - a serious problem in getNextPolylineLocation");
		return null;
	}

	public Location getNextLocation(Location currentLocation, ArrayList history, HashSet edges) {
		int direction = 3;
		if (history.size() > 1) {
			final Location first = (Location)history.get(history.size() - 1);
			final Location second = (Location)history.get(history.size() - 2);
			direction = getDirection(first, second);
		}
		int newDirection = (direction + 3) % 4;
		int i = 0;
		while (i < 4) {
			if (newDirection == 0) {
				Location right = currentLocation.getRight();
				if (right != null && right.isEdge() && edges.contains(right)) {
					return right;
				}
			}
			if (newDirection == 3) {
				Location down = currentLocation.getDown();
				if (down != null && down.isEdge() && edges.contains(down)) {
					return down;
				}
			}
			if (newDirection == 2) {
				Location left = currentLocation.getLeft();
				if (left != null && left.isEdge() && edges.contains(left)) {
					return left;
				}
			}
			if (newDirection == 1) {
				Location up = currentLocation.getUp();
				if (up != null && up.isEdge() && edges.contains(up)) {
					return up;
				}
			}
			newDirection++;
			if (newDirection > 3) {
				newDirection = 0;
			}
			i++;
		}
		//System.out.println("getNextLocation failed!");	
		return null;
	}

    //Not using this right now - might not be debugged entirely
	public Location getNextLocationEightWay(Location currentLocation, ArrayList history, HashSet edges) {
        //System.out.println("called getNextLocationEightWay on " + currentLocation);
		int direction = 5;
		if (history.size() > 1) {
			final Location first = (Location)history.get(history.size() - 1);
			final Location second = (Location)history.get(history.size() - 2);
			direction = getDirectionEightWay(first, second);
		}
        //System.out.println("old direction is " + direction);
		int newDirection = (direction + 5) % 8;
        //System.out.println("new direction is " + newDirection);
		int i = 0;
		while (i < 8) {
			if (newDirection == 0) {
				Location right = currentLocation.getRight();
				if (right != null && right.isEdge() && edges.contains(right)) {
					return right;
				}
                //System.out.println("it isn't right");
			}
			if (newDirection == 7) {
				Location lowerRight = currentLocation.getLowerRight();
				if (lowerRight != null && lowerRight.isEdge() && edges.contains(lowerRight)) {
					return lowerRight;
				}
                ////System.out.println("it isn't lowerright");
			}
			if (newDirection == 6) {
				Location down = currentLocation.getDown();
				if (down != null && down.isEdge() && edges.contains(down)) {
					return down;
				}
                //System.out.println("it isn't down");
			}
			if (newDirection == 5) {
				Location lowerLeft = currentLocation.getLowerLeft();
				if (lowerLeft != null && lowerLeft.isEdge() && edges.contains(lowerLeft)) {
					return lowerLeft;
				}
                //System.out.println("it isn't lowerLeft");
			}
			if (newDirection == 4) {
				Location left = currentLocation.getLeft();
				if (left != null && left.isEdge() && edges.contains(left)) {
					return left;
				}
                //System.out.println("it isn't left");
			}
			if (newDirection == 3) {
				Location upperLeft = currentLocation.getUpperLeft();
				if (upperLeft != null && upperLeft.isEdge() && edges.contains(upperLeft)) {
					return upperLeft;
				}
                //System.out.println("it isn't upperleft");
			}
			if (newDirection == 2) {
				Location up = currentLocation.getUp();
				if (up != null && up.isEdge() && edges.contains(up)) {
					return up;
				}
			}
			if (newDirection == 1) {
				Location upperRight = currentLocation.getUpperRight();
				if (upperRight != null && upperRight.isEdge() && edges.contains(upperRight)) {
					return upperRight;
				}
                //System.out.println("it isn't upperright");
			}
			newDirection++;
			if (newDirection > 7) {
				newDirection = 0;
			}
			i++;
		}
		//System.out.println("getNextLocationEightWay failed!");	
		return null;
	}

	public static int getDirection(Location first, Location second) {
		final int x = first.getX() - second.getX();
		final int y = first.getY() - second.getY();
		//4 way chain code
		if (x < 0 && y == 0) {
			return 2;
		}
		if (x == 0 && y < 0) {
			return 1;
		}
		if (x > 0 && y == 0) {
			return 0;
		}
		return 3;
	}

    //8 way chain code just for polygons
	public static int getDirectionEightWay(Location first, Location second) {
		final int x = first.getX() - second.getX();
		final int y = first.getY() - second.getY();
		if (x > 0 && y > 0) {
			return 7;
		}
		if (x == 0 && y > 0) {
			return 6;
		}
		if (x < 0 && y > 0) {
			return 5;
		}
		if (x < 0 && y == 0) {
			return 4;
		}
		if (x < 0 && y < 0) {
			return 3;
		}
		if (x == 0 && y < 0) {
			return 2;
		}
		if (x > 0 && y < 0) {
			return 1;
		}
		return 0;
	}

	/**
	 * Determines is a particular pixel location is whole or part of a
	 * polyline.  Picture a 3x3 grid with currentLocation at the middle.
	 * If currentLocation has three contiguous neighbors in any of the
	 * 4 possible corner combinations, then it is not a polyline.
	 * The 4 possible corners with *'s are the neighbors checked against
	 * the '@' currentLocation.
	 *
	 *  -**    ---    ---    **-
	 *  -@*    -@*    *@-    *@-
	 *  ---    -**    **-    ---
	 *
	 */
	public boolean isPolyline(Location currentLocation, HashSet used) {
		//System.out.println("calling isPolylineFourWay with " + currentLocation);
		if (currentLocation.hasNonEdgeNeighbor(this)) {
			//currentLocation cannot be a polyline if it has a neighbor
			//that is not an edge.
			return false;
		}

		Location up = currentLocation.getUp();
		if (up != null && (used.contains(up) || up.getRegion() != this)) {
			up = null;
		}

		Location upperRight = currentLocation.getUpperRight();
		if (upperRight != null && (used.contains(upperRight) || upperRight.getRegion() != this)) {
			upperRight = null;
		}

		Location right = currentLocation.getRight();
		if (right != null && (used.contains(right) || right.getRegion() != this)) {
			right = null;
		}

		if (up != null) {
			if (upperRight != null) {
				if (right != null) {
					return false;
				}
			}
		}

		Location lowerRight = currentLocation.getLowerRight();
		if (lowerRight != null && (used.contains(lowerRight) || lowerRight.getRegion() != this)) {
			lowerRight = null;
		}

		Location down = currentLocation.getDown();
		if (down != null && (used.contains(down) || down.getRegion() != this)) {
			down = null;
		}

		if (right != null) {
			if (lowerRight != null) {
				if (down != null) {
					return false;
				}
			}
		}

		Location lowerLeft = currentLocation.getLowerLeft();
		if (lowerLeft != null && (used.contains(lowerLeft) || lowerLeft.getRegion() != this)) {
			lowerLeft = null;
		}

		Location left = currentLocation.getLeft();
		if (left != null && (used.contains(left) || left.getRegion() != this)) {
			left = null;
		}

		if (down != null) {
			if (lowerLeft != null) {
				if (left != null) {
					return false;
				}
			}
		}

		Location upperLeft = currentLocation.getUpperLeft();
		if (upperLeft != null && (used.contains(upperLeft) || upperLeft.getRegion() != this)) {
			upperLeft = null;
		}

		if (left != null) {
			if (upperLeft != null) {
				if (up != null) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * As we walk the polyline, we can determine if the currentLocation
	 * is an end of the polyline if it has only one neighbor that is part
	 * of that same polyline.
	 */
	public boolean isPolylineEnd(Location currentLocation, HashSet used) {
		if (currentLocation.hasNonEdgeNeighbor(this)) {
			return false;
		}
		int count = 0;
		final Location up = currentLocation.getUp();
		if (up != null && up.getRegion() == this && up.isEdge() && !used.contains(up)) {
			count++;
		}
		final Location down = currentLocation.getDown();
		if (down != null && down.getRegion() == this && down.isEdge() && !used.contains(down)) {
			count++;
		}
		final Location left = currentLocation.getLeft();
		if (left != null && left.getRegion() == this && left.isEdge() && !used.contains(left)) {
			count++;
		}
		final Location right = currentLocation.getRight();
		if (right != null && right.getRegion() == this && right.isEdge() && !used.contains(right)) {
			count++;
		}
		return count <= 1;	
	}

	/**
	 * Once we have determined we have a pixel location that is part of a
	 * polyline. Traverse along the polyline until one end of the polyline
	 * is found.  It doesn't matter which end we find.
	 * 8 way chain code
	 */
	public Location getPolylineEnd(Location currentLocation, HashSet alreadyChecked, HashSet edgeSet) {
		//System.out.println("looking for polylineEnd for " + currentLocation);
		//base case
		if (alreadyChecked.contains(currentLocation)) {
			return currentLocation;
		} else {
			alreadyChecked.add(currentLocation);
		}

		//check to see if currentLocation is an end
		if (isPolylineEnd(currentLocation, edgeSet)) {
			//System.out.println("found it");
			return currentLocation;
		}

		//otherwise recurse in all eight directions
		final Location up = currentLocation.getUp();
		if (up != null && !alreadyChecked.contains(up) && up.getRegion() == this && up.isEdge() && isPolyline(up, edgeSet) && !edgeSet.contains(up)) {
			return getPolylineEnd(up, alreadyChecked, edgeSet);
		}

		final Location down = currentLocation.getDown();
		if (down != null && !alreadyChecked.contains(down) && down.getRegion() == this && down.isEdge() && isPolyline(down, edgeSet) && !edgeSet.contains(down)) {
			return getPolylineEnd(down, alreadyChecked, edgeSet);
		}

		final Location left = currentLocation.getLeft();
		if (left != null && !alreadyChecked.contains(left) && left.getRegion() == this && left.isEdge() && isPolyline(left, edgeSet) && !edgeSet.contains(left)) {
			return getPolylineEnd(left, alreadyChecked, edgeSet);
		}

		final Location right = currentLocation.getRight();
		if (right != null && !alreadyChecked.contains(right) && right.getRegion() == this && right.isEdge() && isPolyline(right, edgeSet) && !edgeSet.contains(right)) {
			return getPolylineEnd(right, alreadyChecked, edgeSet);
		}
		//System.out.println("returning getPolylineEnd " + currentLocation);
		return currentLocation;
	}

	//retrieve all polylines from a region before going on
	public HashSet processPolylines(HashSet edges, HashSet result) {
		//System.out.println("starting processPolyline");
		final HashSet alreadyTried = new HashSet();
		final HashSet used = new HashSet();

		Iterator edgeIterator = edges.iterator();
		while (edgeIterator.hasNext()) {
			final Location origin = (Location)edgeIterator.next();
			//System.out.println("Testing next polyline origin " + origin);

			//remove all non-polylines
			if (!isPolyline(origin, used)) {
				edges.remove(origin);
				edgeIterator = edges.iterator();
				alreadyTried.add(origin);
				//System.out.println("location is not a polyline: " + origin);
				continue;
			}

			//System.out.println("have polyline origin " + origin);
			final ArrayList actualList = new ArrayList();	
			final ArrayList list = new ArrayList();

			//call this only once to find the end of the line
			Location nextLocation = getPolylineEnd(origin, new HashSet(), used);
			//System.out.println("polyline end is " + nextLocation);

			while (nextLocation != null && isPolyline(nextLocation, used)) {
				//System.out.println("Testing next polyline location " + nextLocation);
				list.add(nextLocation);
				final int size = actualList.size();
				if (size > 1) {
					final Location firstLocation = (Location)actualList.get(size - 2);
					final Location secondLocation = (Location)actualList.get(size - 1);
					if (getDirection(firstLocation, secondLocation) ==
							getDirection(secondLocation, nextLocation)) {
						actualList.remove(size - 1);
					}
				}
				used.add(nextLocation);
				actualList.add(nextLocation);
				edges.remove(nextLocation);
				nextLocation = getNextPolylineLocation(nextLocation, edges, used);
			}
			final Polyline polyline = new Polyline();
			polyline.addLocations(actualList);
			result.add(polyline);

			//start with a new iterator on the remaining points
			edgeIterator = edges.iterator();
		}
		//System.out.println("Done with polyline");
		return alreadyTried;
	}

	//chain code by 4
	public HashSet getSortedEdges() {
		//getEdgeLocations is dependent on children already being set correctly
		HashSet edges = getEdgeLocations();
		final HashSet result = new HashSet();

		//first remove all the polylines
		edges = processPolylines(edges, result);
		final HashSet edgePool = (HashSet)edges.clone();
		//get an arbitrary starting point and walk through the edges
		//until the starting point is reached

		final ArrayList list = new ArrayList(edges.size());
		while (edgePool.size() > 0) {
			final ArrayList actualList = new ArrayList(edges.size());
			list.clear();

			final Location origin = getLeftMostLocation(edges);
			if (origin == null) {
				return result;
			}

			list.add(origin);
			edgePool.remove(origin);
			actualList.add(origin);

			Location nextLocation = getNextLocationEightWay(origin, list, edges);
			while (nextLocation != null && nextLocation != origin) {
				list.add(nextLocation);
				edgePool.remove(nextLocation);
				final int size = actualList.size();
				if (size > 1) {
					final Location firstLocation = (Location)actualList.get(size - 2);
					final Location secondLocation = (Location)actualList.get(size - 1);
					if (getDirectionEightWay(firstLocation, secondLocation) ==
							getDirectionEightWay(secondLocation, nextLocation)) {
						actualList.remove(size - 1);
					}
				}
				actualList.add(nextLocation);
				nextLocation = getNextLocationEightWay(nextLocation, list, edges);
			}

			//remove last element of path to allow z to go all the way
			final int size = actualList.size();
			if (size > 2) {
				final Location firstLocation = (Location)actualList.get(size - 2);
				final Location secondLocation = (Location)actualList.get(size - 1);
				if (getDirectionEightWay(firstLocation, secondLocation) ==
						getDirectionEightWay(secondLocation, origin)) {
					actualList.remove(size - 1);
				}
			}
            final Polygon polygon = new Polygon(actualList);
			result.add(polygon);
			//System.out.println("Result size is now " + result.size());
			//removeRedundantLocations(list);
			//System.out.println("pruned " + (size - list.size()));
			//return list;
			edges = (HashSet)edgePool.clone();
		}
		if (edgePool.size() > 0) {
			System.err.println("Missed points:");
			final Iterator leftOver = edgePool.iterator();
			while (leftOver.hasNext()) {
				System.err.println(leftOver.next());
			}
		}
		//System.out.println("Returning result size " + result.size() + " for " +
				//this.hashCode());
		return result;
	}

	public boolean merge(Region region) {
		final HashSet locations = region.getLocations();
		final boolean result = mergeLocations(locations);
		region.replaceNeighbor(this);
        //they are all gradients once merged
        //fix this if fixing the merge
        //paint = PaintGenerator.generate(getLocations());
		return result;
	}

	public boolean mergeLocations(HashSet locations) {
		final Iterator iterator = locations.iterator();
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
			if (!add(location)) {
				return false;
			}
			//location.setRegion(this);
		}
		//this.locations.addAll(locations);
		return true;
	}

	public void mergeLocation(Location location) {
		location.setRegion(this);
		this.locations.add(location);
	}

	public HashSet getChildren() {
		return children;
	}

	public boolean isChild(Region region) {
		return children.contains(region);
	}

	public void removeChild(Region region) {
		children.remove(region);
	}

	public boolean hasMultipleParents() {
		return parents.size() > 1;
	}

    public boolean isParentSameColor() {
        if (fillColor == null) {
            return false;
        }
        final int count = parents.size();
        if (count == 0 || count > 1) {
            return false;
        }
        final Region parent = (Region)parents.iterator().next();
        return parent.getFillColor() != null && fillColor.equals(parent.getFillColor());
            
    }

	public boolean isBiggestParent(Region parent) {
		final int size = parent.size();
		final Iterator iterator = parents.iterator();
		while (iterator.hasNext()) {
			final Region otherParent = (Region)iterator.next();
			if (otherParent == parent) {
				continue;
			}
			if (otherParent.size() > size) {
				return false;
			}
		}
		return true;
	}

	public boolean isPoint() {
		return locations.size() == 1;
	}

	public Color getAverageColor() {
		double red = 0;
		double green = 0;
		double blue = 0;
		final Iterator iterator = locations.iterator();
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();
			final Color color = location.getColor();
			red += color.getRed();
			green += color.getGreen();
			blue += color.getBlue();
		}
		final int size = locations.size();
		red = red / size; 
		green = green / size; 
		blue = blue / size; 
		return new Color(red, green, blue);
	}

	//iterate through all locations and find all 
	//that are immediate neighbors to region
	public HashSet findNeighborLocations(Region region) {
		final HashSet result = new HashSet();
		final Iterator iterator = region.iterator();
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();

			final Location left = location.getLeft();
			if (left != null && left.getRegion() == this) {
				result.add(location);
				continue;
			}

			final Location right = location.getRight();
			if (right != null && right.getRegion() == this) {
				result.add(location);
				continue;
			}

			final Location up = location.getUp();
			if (up != null && up.getRegion() == this) {
				result.add(location);
				continue;
			}

			final Location down = location.getDown();
			if (down != null && down.getRegion() == this) {
				result.add(location);
				continue;
			}
		}
		return result;
	}

    public double getNeighborPaint(Region region) {
        //if (region.getPaint() instanceof FillColor && region.size() > 10) {
            //return Double.MAX_VALUE;
        //}
        final HashSet combinedLocations = new HashSet();
        //System.out.println("target region size is " + size());
        combinedLocations.addAll(getLocations());
        combinedLocations.addAll(region.getLocations());
        //System.out.println("neighbor region size is " + region.size());
        //System.out.println("combined locations size is " + combinedLocations.size());
        final Paint paint = PaintGenerator.generate(combinedLocations);
        return paint.getWorstError();
    }

    //Find average difference between neighbor pixels of two regions
	public double getPixelNeighborDifference(Region region) {
		final HashSet otherLocations = findNeighborLocations(region);
		final Iterator iterator = otherLocations.iterator();
		double total = 0.0;
		int count = 0;
		while (iterator.hasNext()) {
			final Location location = (Location)iterator.next();

			final Location left = location.getLeft();
			if (left != null && left.getRegion() == this) {
				total += Color.getColorDistance(left.getColor(),
					   	location.getColor());
				count++;
			}

			final Location right = location.getRight();
			if (right != null && right.getRegion() == this) {
				total += Color.getColorDistance(right.getColor(),
					   	location.getColor());
				count++;
			}

			final Location up = location.getUp();
			if (up != null && up.getRegion() == this) {
				total += Color.getColorDistance(up.getColor(),
					   	location.getColor());
				count++;
			}

			final Location down = location.getDown();
			if (down != null && down.getRegion() == this) {
				total += Color.getColorDistance(down.getColor(),
					   	location.getColor());
				count++;
			}
		}
		if (count == 0) {
			return Double.MAX_VALUE;
		}
		return total / count;
	}

	public NearestRegion getNearestRegion() {
		double bestDifference = Double.MAX_VALUE;
		Region bestRegion = null;

		final Iterator iterator = neighbors();
		while (iterator.hasNext()) {
			final Region neighbor = (Region)iterator.next();
			//final double tempDifference = getPixelNeighborDifference(neighbor);
			final double tempDifference = getNeighborPaint(neighbor);

			if (tempDifference < bestDifference) {
				bestRegion = neighbor;
				bestDifference = tempDifference;
			}
		}

		if (bestRegion == null) {
			return null;
		}
		final NearestRegion nearestRegion = new NearestRegion();
		nearestRegion.setRegion(this);
		nearestRegion.setNearestNeighbor(bestRegion);
		nearestRegion.setDifference(bestDifference);
		return nearestRegion;
	}
}
