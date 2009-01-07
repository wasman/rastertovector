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
package com.sxz.GUI;

import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.text.NumberFormat;
import java.awt.geom.Line2D;
import com.sxz.parser.Color;
import com.sxz.parser.LinearGradient;
import com.sxz.parser.Location;
import com.sxz.parser.Region;
import com.sxz.parser.Shape;
import com.sxz.parser.Polyline;
import com.sxz.parser.PaintGenerator;
import com.sxz.parser.FillColor;
import com.sxz.parser.ColorGradient;
import com.sxz.parser.Frame;

final class FormatSVG {

	private final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
	static {
		NUMBER_FORMAT.setMaximumFractionDigits(1);
	}

	private static String getNumber(double number) {
		return NUMBER_FORMAT.format(number);
	}

	private static String toHexString(double amount) {
		String hexString = Integer.toHexString((int)Math.round(amount));
		if (hexString.length() == 1) {
			hexString = "0" + hexString;
		}
		return hexString;
	}

	static String getHexColorString(Color color) {
		final String key = color.getRed() + ", " + color.getGreen() + ", " + color.getBlue(); 
		final String colorName = SVGColorMap.getColorName(key);
		if (colorName == null) {
			final String result = "#" + toHexString(color.getRed()) + toHexString(color.getGreen()) + toHexString(color.getBlue()); 
			return result;
		}
		return colorName;
	}

	private StringBuffer stringBuffer;

	FormatSVG() {
		super();
        stringBuffer = new StringBuffer();
	}

    void initialize() {
        stringBuffer.setLength(0);
        //capacity?
    }

	private void formatPoint(int x, int y, Region region) {
        stringBuffer.append("<rect width=\"1\" height=\"1\" ");
        stringBuffer.append("x=\"");
        stringBuffer.append(x);
        stringBuffer.append("\" ");
        stringBuffer.append("y=\"");
        stringBuffer.append(y);
        final FillColor fillColor = region.getFillColor();
        if (fillColor != null) {
            final Color color = fillColor.getColor();
            stringBuffer.append("\" style=\"stroke:");
            stringBuffer.append(getHexColorString(color));
            stringBuffer.append("; fill:");
            stringBuffer.append(getHexColorString(color));
            stringBuffer.append("\" />\n");
        } else {
            stringBuffer.append("\" style=\"stroke:url(#" + region.hashCode() + "); fill:url(#" + region.hashCode() + ")\"/>\n");
        }
    }

	private void formatPolyline(Polyline polyline, Region region) {
		final ArrayList locations = polyline.getLocations();
		//point data is entered here
		final int size = locations.size();
		if (size == 1) {
			final Location location = (Location)locations.get(0);
            formatPoint(location.getX(), location.getY(), region);
			return;
		}

		stringBuffer.append("<path d=\"M");
		Location previousLocation = null;
		Location nextLocation = null;
		float previousInitialPadding = 0.0f;
		//final Location lastLocation = (Location)locations.get(size - 1);
		for (int i = 0; i < size; i++) {
			final Location location = (Location)locations.get(i);
			if (i > 0) {
				previousLocation = (Location)locations.get(i - 1);
			} else {
				previousLocation = null;
			}
			if (i < (size - 1)) {
				nextLocation = (Location)locations.get(i + 1);
			} else {
				nextLocation = null;
			}
			float initialPadding = 0.0f;
			if (i == 0) {
				if (location.getX() == nextLocation.getX()) {
					if (location.getY() != 0) {
						if (location.getY() < nextLocation.getY()) {
							initialPadding = -.5f;
						} else {
							initialPadding = .5f;
						}
					}
				} else {
					if (location.getX() != 0) {
						if (location.getX() < nextLocation.getX()) {
							initialPadding = -.5f;
						} else {
							initialPadding = .5f;
						}
					}
				}
			}
			float endPadding = 0.0f;
			if (i == (size - 1)) {
				if (location.getX() == previousLocation.getX()) {
					if (location.getY() != 0) {
						if (location.getY() < previousLocation.getY()) {
							endPadding = -.5f;
						} else {
							endPadding = .5f;
						}
					}
				} else {
					if (location.getX() != 0) {
						if (location.getX() < previousLocation.getX()) {
							endPadding = -.5f;
						} else {
							endPadding = .5f;
						}
					}
				}
			}

			//this is for doing h or v's for svg path d attribute
			//System.out.println("initialPadding is " + initialPadding);
			//System.out.println("previousInitialPadding is " + previousInitialPadding);
			//System.out.println("endPadding is " + endPadding);
			if (previousLocation != null) {
				if (previousLocation.getX() == location.getX()) {
					stringBuffer.append("v");
					stringBuffer.append(getNumber(location.getY() - previousLocation.getY() - previousInitialPadding + endPadding));
				} else {
					stringBuffer.append("h");
					stringBuffer.append(getNumber(location.getX() - previousLocation.getX() - previousInitialPadding + endPadding));
				}
			} else {
				if (location.getY() == nextLocation.getY()) {
					stringBuffer.append(getNumber(location.getX() + initialPadding));
				} else {
					stringBuffer.append(getNumber(location.getX()));
				}
				stringBuffer.append(" ");
				if (location.getX() == nextLocation.getX()) {
					stringBuffer.append(getNumber(location.getY() + initialPadding));
				} else {
					stringBuffer.append(getNumber(location.getY()));
				}
			}
			previousInitialPadding = initialPadding;
			if (i < (size - 1)) {
				//stringBuffer.append(" ");
				previousLocation = location;
			}
		}

		final FillColor fillColor = region.getFillColor();
		if (fillColor != null) {
            final Color color = fillColor.getColor();
			stringBuffer.append("\" style=\"stroke:");
			stringBuffer.append(getHexColorString(color));
			stringBuffer.append("\"/>\n");
		} else {
            stringBuffer.append("\" style=\"stroke:url(#" + region.hashCode() + ")\"/>\n");
		}
	}

	private void formatRegion(Region region, HashSet remainingRegions) {
		if (region.getFillColor() != null) {
			formatColorRegion(region, remainingRegions);
		} else {
			formatGradientRegion(region, remainingRegions);
		}
	}

	private void formatGradientRegion(Region region, HashSet remainingRegions) {

		final HashSet parents = region.getParents();
		if (parents.size() > 0) {
			final Iterator iterator = parents.iterator();
			while (iterator.hasNext()) {
				final Region parent = (Region)iterator.next();
				if (remainingRegions.contains(parent)) {
					remainingRegions.remove(parent);
					formatRegion(parent, remainingRegions);
				}
			}
		}
		
		final HashSet sets = region.getSortedEdges();
		if (sets == null) {
			System.err.println("region sorted edges is null for " + region);
			return;
		}
		//first print out <def>
		stringBuffer.append("<defs>\n");
        final ColorGradient colorGradient = PaintGenerator.generate(region.getLocations());
		stringBuffer.append("<linearGradient gradientUnits=\"userSpaceOnUse\" id=\"" + region.hashCode() + "\" "
                + getSVGLine(colorGradient) + ">\n");
		stringBuffer.append("\t<stop offset=\"0%\" stop-color=\"" + getColorStart(colorGradient) + "\"></stop>\n");
		stringBuffer.append("\t<stop offset=\"100%\" stop-color=\"" + getColorEnd(colorGradient) + "\"></stop>\n");
        //draw gradient here
		stringBuffer.append("</linearGradient>\n");
		stringBuffer.append("</defs>\n");

		final Iterator iterator = sets.iterator(); 
		while (iterator.hasNext()) {
			final Shape shape = (Shape)iterator.next();

			boolean isPolyline = false;
			if (shape instanceof Polyline) {
				formatPolyline((Polyline)shape, region);
				continue;
			}
			final ArrayList locations = shape.getLocations();

            stringBuffer.append("<path d=\"M");
            final int size = locations.size();
            Location previousLocation = null;
            Location nextLocation = null;
            float previousBackPadding = 0.0f;
            for (int i = 0; i < size; i++) {
                final Location location = (Location)locations.get(i);
                if (i > 0) {
                    previousLocation = (Location)locations.get(i - 1);
                } else {
                    previousLocation = null;
                }
                if (i < (size - 1)) {
                    nextLocation = (Location)locations.get(i + 1);
                } else {
                    nextLocation = null;
                }

                //this is for doing h or v's for svg path d attribute
                //this should be written better
                if (previousLocation != null) {
                    if (previousLocation.getX() == location.getX()) {
                        stringBuffer.append("v");
                        stringBuffer.append(getNumber(location.getY() - previousLocation.getY()));
                    } else if (previousLocation.getY() == location.getY()) {
                        stringBuffer.append("h");
                        stringBuffer.append(getNumber(location.getX() - previousLocation.getX()));
                    } else {
                        stringBuffer.append("L");
                        stringBuffer.append(getNumber(location.getX()));
                        stringBuffer.append(" ");
                        stringBuffer.append(getNumber(location.getY()));
                    }
                } else {
                    stringBuffer.append(getNumber(location.getX()));
                    stringBuffer.append(" ");
                    stringBuffer.append(getNumber(location.getY()));
                }
                if (i < (size - 1)) {
                    previousLocation = location;
                }
            }

            stringBuffer.append("z\" style=\"stroke:url(#" + region.hashCode() + "); fill:url(#" + region.hashCode() + ")\"/>\n");
		}
	}

	private void formatColorRegion(Region region, HashSet remainingRegions) {
		if (region.isParentSameColor()) {
			return;
		}

		final HashSet parents = region.getParents();
		if (parents.size() > 0) {
			final Iterator iterator = parents.iterator();
			while (iterator.hasNext()) {
				final Region parent = (Region)iterator.next();
				if (remainingRegions.contains(parent)) {
					remainingRegions.remove(parent);
					formatRegion(parent, remainingRegions);
				}
			}
		}

		final HashSet sets = region.getSortedEdges();
		if (sets == null) {
			System.err.print("region sorted edges is null for " + region);
			return;
		}

		final Iterator iterator = sets.iterator(); 
		while (iterator.hasNext()) {
			final Shape nextObject = (Shape)iterator.next();
			if (nextObject instanceof Polyline) {
				formatPolyline((Polyline)nextObject, region);
				continue;
			}

			final ArrayList locations = nextObject.getLocations();

            stringBuffer.append("<path d=\"M");
            //point data is entered here
            final int size = locations.size();
            //final Iterator locationIterator = locations.iterator();
            Location previousLocation = null;
            Location nextLocation = null;
            float previousBackPadding = 0.0f;
            for (int i = 0; i < size; i++) {
                //final Location location = (Location)locationIterator.next();
                final Location location = (Location)locations.get(i);
                if (i > 0) {
                    previousLocation = (Location)locations.get(i - 1);
                } else {
                    previousLocation = null;
                }
                if (i < (size - 1)) {
                    nextLocation = (Location)locations.get(i + 1);
                } else {
                    nextLocation = null;
                }

                //this is for doing h or v's for svg path d attribute
                //this should be written better
                if (previousLocation != null) {
                    if (previousLocation.getX() == location.getX()) {
                        stringBuffer.append("v");
                        stringBuffer.append(getNumber(location.getY() - previousLocation.getY()));
                    } else if (previousLocation.getY() == location.getY()) {
                        stringBuffer.append("h");
                        stringBuffer.append(getNumber(location.getX() - previousLocation.getX()));
                    } else {
                        stringBuffer.append("L");
                        stringBuffer.append(getNumber(location.getX()));
                        stringBuffer.append(" ");
                        stringBuffer.append(getNumber(location.getY()));
                    }
                } else {
                    stringBuffer.append(getNumber(location.getX()));
                    stringBuffer.append(" ");
                    stringBuffer.append(getNumber(location.getY()));
                }
                if (i < (size - 1)) {
                    //stringBuffer.append(" ");
                    previousLocation = location;
                }
            }

            FillColor fillColor = region.getFillColor();
            final Color color = fillColor.getColor();

            stringBuffer.append("z\" style=\"stroke:");
            stringBuffer.append(getHexColorString(color));
            stringBuffer.append(";fill:");
            stringBuffer.append(getHexColorString(color));
            stringBuffer.append("\"/>\n");
		}
	}

	String format(Frame frame) {
		appendHeader();
		stringBuffer.append("<svg width=\"");
		stringBuffer.append(frame.getWidth());
		stringBuffer.append("\" height=\"");
		stringBuffer.append(frame.getHeight());
		stringBuffer.append("\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");

		//debug options here
		//stringBuffer.append("<g id=\"top_level\" transform=\"scale(2)\">\n");

        //need a little help to get SVG to render nicely
		stringBuffer.append("<defs>\n");
		stringBuffer.append("<style type=\"text/css\"><![CDATA[\n");
		stringBuffer.append("\tpath {\n");
		stringBuffer.append("\t\tshape-rendering: crispEdges;\n");
		stringBuffer.append("\t\tstroke-width: 1;\n");
		stringBuffer.append("\t\tfill: none;\n");
		stringBuffer.append("}\n");
		stringBuffer.append("\trect {\n");
		stringBuffer.append("\t\tshape-rendering: crispEdges;\n");
		stringBuffer.append("}\n");
		stringBuffer.append("]]></style>\n");
		stringBuffer.append("</defs>\n");
		/*
		final HashSet remainingRegions = frame.getRegions();
		while (remainingRegions.size() > 0) {
			final Region region = (Region)remainingRegions.iterator().next();
			remainingRegions.remove(region);
			formatRegion(region, remainingRegions);
		}
		*/
		List regions = frame.getReversedSortedList();
		/*
		for (int i = 0; i < regions.size(); i++) {
			Region region = (Region)regions.get(i);
			System.out.println(region.size());
			final FillColor fillColor = region.getFillColor();
			if (fillColor != null) {
				System.out.println(fillColor);
			}
			if (region.size() < 10) {
				break;
			}
		}
		*/
		/*
		final Region onlyRegion = (Region)regions.get(0);
		final HashSet children = onlyRegion.getChildren();
		final Iterator iterator = children.iterator();
		while (iterator.hasNext()) {
			final Region child = (Region)iterator.next();
			System.out.println("Have child " + child);
		}
		*/
		formatRegion((Region)regions.get(0), new HashSet());


        //debug option
		//stringBuffer.append("</g>\n");
		stringBuffer.append("</svg>");
        return stringBuffer.toString();
	}

	void appendHeader() {
		stringBuffer.append("<?xml version=\"1.0\"?>\n");
		stringBuffer.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20001102//EN\" \"http://www.w3.org/TR/2000/CR-SVG-20001102/DTD/svg-20001102.dtd\">\n");
	}

	//This should be fixed to calculate the blue and green points too
    static String getSVGLine(ColorGradient colorGradient) {
        final LinearGradient red = (LinearGradient)colorGradient.getRed();
        final LinearGradient green = (LinearGradient)colorGradient.getGreen();
        final LinearGradient blue = (LinearGradient)colorGradient.getBlue();
        final Line2D redLine = red.getGradientLine();
        final Line2D greenLine = green.getGradientLine();
        final Line2D blueLine = blue.getGradientLine();

        //return "x1=\"" + getNumber(line.getP1().getX()) + "\" y1=\"" + getNumber(line.getP1().getY()) + "\" x2=\"" + getNumber(line.getP2().getX()) + "\" y2=\"" + getNumber(line.getP2().getY()) + "\"";
		double x1 = (redLine.getP1().getX() + greenLine.getP1().getX()
				+ blueLine.getP1().getX()) / 3; 
		double y1 = (redLine.getP1().getY() + greenLine.getP1().getY()
				+ blueLine.getP1().getY()) / 3; 
		double x2 = (redLine.getP2().getX() + greenLine.getP2().getX()
				+ blueLine.getP2().getX()) / 3; 
		double y2 = (redLine.getP2().getY() + greenLine.getP2().getY()
				+ blueLine.getP2().getY()) / 3; 
        return "x1=\"" + getNumber(x1) + "\" y1=\"" + getNumber(y1) + "\" x2=\"" + getNumber(x2) + "\" y2=\"" + getNumber(y2) + "\"";
    }

    static String getColorStart(ColorGradient colorGradient) {
        final LinearGradient red = (LinearGradient)colorGradient.getRed();
        final LinearGradient green = (LinearGradient)colorGradient.getGreen();
        final LinearGradient blue = (LinearGradient)colorGradient.getBlue();
        final Color color = new Color(red.getValueAtOrigin(), green.getValueAtOrigin(), blue.getValueAtOrigin());        
        return getHexColorString(color);
    }

    static String getColorEnd(ColorGradient colorGradient) {
        final LinearGradient red = (LinearGradient)colorGradient.getRed();
        final LinearGradient green = (LinearGradient)colorGradient.getGreen();
        final LinearGradient blue = (LinearGradient)colorGradient.getBlue();
        final Color color = new Color(red.getEnd(), green.getEnd(), blue.getEnd());        
        return getHexColorString(color);
    }
}
