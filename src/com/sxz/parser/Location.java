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

public final class Location {

	private int x;
	private int y;
	private boolean marked;
	private boolean edge;
	private Region region;
	private Color color;
	//this fixes us on only four neighbors though
	private Location up;
	private Location down;
	private Location left;
	private Location right;
	private Location upperLeft;
	private Location upperRight;
	private Location lowerLeft;
	private Location lowerRight;

	public Location() {
		super();
	}

	public Location(int x, int y) {
		this();
		this.x = x; 
		this.y = y; 
	}

	public Location(Color color, int x, int y) {
		this();
		this.x = x; 
		this.y = y; 
		this.color = color;
	}

    public double getColorDistance(Location location) {
        return Color.getColorDistance(color, location.getColor());
    }

	//true if three of its neighbors are in a different region
	//only for non-angled pieces though
	//rename isEndOfLine()?
	public boolean isIsolated() {
		int matches = 0;
		if (up != null && up.getRegion() == region) {
			matches++;
		}
		if (down != null && down.getRegion() == region) {
			matches++;
		}
		if (left != null && left.getRegion() == region) {
			matches++;
		}
		if (right != null && right.getRegion() == region) {
			matches++;
		}
		return matches < 2;
	}

	//check if it is the start or end of a line
	public boolean isLineEnd() {
		return getNumberOfNeighbors() < 2;
	}

	//tricky, this one
	public boolean isEdgeOfRegion(HashSet children) {
		//if its on the edge of the image, then no
		if (up == null || down == null || left == null || right == null) {
			return true;
		}

		if (up.getRegion() != region && !children.contains(up.getRegion())) {
			return true;
		}
		if (upperLeft.getRegion() != region && !children.contains(upperLeft.getRegion())) {
			return true;
		}
		if (down.getRegion() != region && !children.contains(down.getRegion())) {
			return true;
		}
		if (lowerLeft.getRegion() != region && !children.contains(lowerLeft.getRegion())) {
			return true;
		}
		if (left.getRegion() != region && !children.contains(left.getRegion())) {
			return true;
		}
		if (upperRight.getRegion() != region && !children.contains(upperRight.getRegion())) {
			return true;
		}
		if (lowerRight.getRegion() != region && !children.contains(lowerRight.getRegion())) {
			return true;
		}
		if (right.getRegion() != region && !children.contains(right.getRegion())) {
			return true;
		}
		return false;
	}

	public boolean isPoint() {
		return getNumberOfNeighbors() < 2;	
	}
/*
	public boolean isContained(Region container) {
		final HashSet children = container.getChildren();

		if (!children.contains(this.getRegion())) {
			return false;
		}

		if (up != null && (up.getRegion() != container && !children.contains(up.getRegion()))) {
			return false;
		}
		if (down != null && (down.getRegion() != container && !children.contains(down.getRegion()))) {
			return false;
		}
		if (left != null && (left.getRegion() != container && !children.contains(left.getRegion()))) {
			return false;
		}
		if (right != null && (right.getRegion() != container && !children.contains(right.getRegion()))) {
			return false;
		}
		if (upperLeft != null && (upperLeft.getRegion() != container && !children.contains(upperLeft.getRegion()))) {
			return false;
		}
		if (upperRight != null && (upperRight.getRegion() != container && !children.contains(upperRight.getRegion()))) {
			return false;
		}
		if (lowerRight != null && (lowerRight.getRegion() != container && !children.contains(lowerRight.getRegion()))) {
			return false;
		}
		if (lowerLeft != null && (lowerLeft.getRegion() != container && !children.contains(lowerLeft.getRegion()))) {
			return false;
		}
		return true;
	}
*/

	private boolean isRelated(Region region) {
		return this.region == region || region.isChild(this.region);
	}

	public boolean hasNonEdgeNeighbor(Region container) {
		//System.out.println("hasNonEdgeNeighbor called for " + this);
	
		if (up != null && up.isRelated(container) && !up.isEdge()) {
			//System.out.println("hasNonEdgeNeighbor up " + up);
			return true;
		}
		if (down != null && down.isRelated(container) && !down.isEdge()) {
			//System.out.println("hasNonEdgeNeighbor down " + down);
			return true;
		}
		if (upperLeft != null && upperLeft.isRelated(container) && !upperLeft.isEdge()) {
			//System.out.println("hasNonEdgeNeighbor upperLeft " + upperLeft);
			return true;
		}
		if (upperRight != null && upperRight.isRelated(container) && !upperRight.isEdge()) {
			//System.out.println("hasNonEdgeNeighbor upperRight " + upperRight);
			return true;
		}
		if (lowerLeft != null && lowerLeft.isRelated(container) && !lowerLeft.isEdge()) {
			//System.out.println("hasNonEdgeNeighbor lowerLeft " + lowerLeft);
			return true;
		}
		if (lowerRight != null && lowerRight.isRelated(container) && !lowerRight.isEdge()) {
			//System.out.println("hasNonEdgeNeighbor lowerRight " + lowerRight);
			return true;
		}
		if (left != null && left.isRelated(container) && !left.isEdge()) {
			//System.out.println("hasNonEdgeNeighbor left " + left);
			return true;
		}
		if (right != null && right.isRelated(container) && !right.isEdge()) {
			//System.out.println("hasNonEdgeNeighbor right " + right);
			return true;
		}
		return false;
	}

	public int getNumberOfNeighbors() {
		int matches = 0;
		if (up != null && up.getRegion() == region) {
			matches++;
		}
		/*
		if (upperRight != null && upperRight.getRegion() == region) {
			matches++;
		}
		*/
		if (down != null && down.getRegion() == region) {
			matches++;
		}
		/*
		if (lowerRight != null && lowerRight.getRegion() == region) {
			matches++;
		}
		*/
		if (left != null && left.getRegion() == region) {
			matches++;
		}
		/*
		if (lowerLeft != null && lowerLeft.getRegion() == region) {
			matches++;
		}
		*/
		if (right != null && right.getRegion() == region) {
			matches++;
		}
		/*
		if (upperLeft != null && upperLeft.getRegion() == region) {
			matches++;
		}
		*/
		return matches;
	}

	public int getX() {
		return (int)x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return (int)y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Location getUp() {
		return up;
	}

	public void setUp(Location up) {
		this.up = up;
	}

	public Location getDown() {
		return down;
	}

	public void setDown(Location down) {
		this.down = down;
	}

	public Location getLeft() {
		return left;
	}

	public void setLeft(Location left) {
		this.left = left;
	}

	public Location getRight() {
		return right;
	}

	public void setRight(Location right) {
		this.right = right;
	}

	public Location getUpperLeft() {
		return upperLeft;
	}

	public void setUpperLeft(Location upperLeft) {
		this.upperLeft = upperLeft;
	}

	public Location getUpperRight() {
		return upperRight;
	}

	public void setUpperRight(Location upperRight) {
		this.upperRight = upperRight;
	}

	public Location getLowerLeft() {
		return lowerLeft;
	}

	public void setLowerLeft(Location lowerLeft) {
		this.lowerLeft = lowerLeft;
	}

	public Location getLowerRight() {
		return lowerRight;
	}

	public void setLowerRight(Location lowerRight) {
		this.lowerRight = lowerRight;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	public boolean isEdge() {
		return edge;
	}

	public void setEdge(boolean edge) {
		this.edge = edge;
	}

	public boolean equals(Object object) {
		if (!(object instanceof Location)) {
			return false;
		}
		final Location otherLocation = (Location)object;
		return (otherLocation.getX() == x) && (otherLocation.getY() == y);
	}

	public void setRedError(float redError) {
		redError += color.getRed();
		if (redError > 255) {
			redError = 255.0f;
		}
		if (redError < 0) {
			redError = 0.0f;
		}
		color = new Color((int)redError, color.getGreen(), color.getBlue());
	}

	public void setGreenError(float greenError) {
		greenError += color.getGreen();
		if (greenError > 255) {
			greenError = 255.0f;
		}
		if (greenError < 0) {
			greenError = 0.0f;
		}
		color = new Color(color.getRed(), (int)greenError, color.getBlue());
	}

	public void setBlueError(float blueError) {
		blueError += color.getBlue();
		if (blueError > 255) {
			blueError = 255.0f;
		}
		if (blueError < 0) {
			blueError = 0.0f;
		}
		color = new Color(color.getRed(), color.getGreen(), (int)blueError);
	}

    public String toString() {
        return x + " " + y + " " + color;
    }

	public Object clone() {
		final Location location = new Location();
		location.setX(x);
		location.setY(y);
		location.setMarked(marked);
		location.setEdge(edge);
		location.setRegion(region);
		location.setColor(color);
		location.setUp(up);
		location.setDown(down);
		location.setLeft(left);
		location.setRight(right);
		location.setUpperLeft(upperLeft);
		location.setUpperRight(upperRight);
		location.setLowerLeft(lowerLeft);
		location.setLowerRight(lowerRight);
		return location;
	}
}
