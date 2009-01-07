package com.sxz.raster;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import java.io.IOException;
import java.io.File;
import java.io.StringBufferInputStream;
import java.util.StringTokenizer;
import java.util.Iterator;
import com.sxz.parser.Color;
import com.sxz.math.Util;
import com.sxz.math.NonLinearCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;

public final class Parser {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing file parameter dufus");
            return;
        }
        final File file = new File(args[0]);
        if (!file.exists()) {
            System.err.println("Could not find file " + file.getPath());
            return;
        }

        final Handler handler = new Handler();

        parseXmlFile(file, handler, false);
        System.out.println("done parsing file");

        final Frame frame = handler.frame;
        final Rasterer rasterer = new Rasterer();
        rasterer.draw(frame, "temp.png");
        /*    
        final Iterator iterator = frame.iterator();
        while (iterator.hasNext()) {
            final Region region = (Region)iterator.next();
            final Iterator points = region.getPoints().iterator();
            while (points.hasNext()) {
                final Point2D point = (Point2D)points.next();
                System.out.println(point + " has color " + region.getColor(point));
            }
        }
        */
        
    }

	public static void parseString(String input, String outputFilename) {
        final Handler handler = new Handler();
		System.out.println("parsing data stream " + input);

        parseXmlString(input, handler, false);

        final Frame frame = handler.frame;
        final Rasterer rasterer = new Rasterer();
        rasterer.draw(frame, outputFilename);
	}

    private static class Handler extends DefaultHandler {
        private StringBuffer stringBuffer = new StringBuffer();
        Frame frame = null;
        private Region region = null;
        private ColorGradient colorGradient = null;
        private LinearGradient linearGradient = null;
        public void characters(char[] ch, int start, int length) throws SAXException {
            final String string = new String(ch, start, length);
            stringBuffer.append(string);
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            final String tagName = qName.toLowerCase();
            if (tagName.equalsIgnoreCase("linearGradient")) {
                //System.out.println("start linearGradient tag");
                if (colorGradient == null) {
                    colorGradient = new ColorGradient();
                    //System.out.println("creating color gradient paint");
                    region.setPaint(colorGradient);
                }
                linearGradient = new LinearGradient();
                final String x1String = attributes.getValue("x1");
                final String y1String = attributes.getValue("y1");
                final String x2String = attributes.getValue("x2");
                final String y2String = attributes.getValue("y2");
                final String type = attributes.getValue("type");
                try {
                    final double x1 = Double.parseDouble(x1String); 
                    final double y1 = Double.parseDouble(y1String); 
                    final double x2 = Double.parseDouble(x2String); 
                    final double y2 = Double.parseDouble(y2String); 
                    linearGradient.setGradientLine(new Line2D.Double(x1, y1, x2, y2));
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
                if (type.equalsIgnoreCase("r")) {
                    colorGradient.setRed(linearGradient);
                } else if (type.equalsIgnoreCase("g")) {
                    colorGradient.setGreen(linearGradient);
                } else if (type.equalsIgnoreCase("b")) {
                    colorGradient.setBlue(linearGradient);
                } else if (type.equalsIgnoreCase("a")) {
                    colorGradient.setRed(linearGradient);
                    colorGradient.setGreen(linearGradient);
                    colorGradient.setBlue(linearGradient);
                }
            } else if (tagName.equalsIgnoreCase("frame")) {
                frame = new Frame();
            } else if (tagName.equalsIgnoreCase("region")) {
                region = new Region();
                frame.add(region);
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            final String tagName = qName.toLowerCase();
            final String content = stringBuffer.toString().trim();
            if (tagName.equalsIgnoreCase("frame")) {
            } else if (tagName.equalsIgnoreCase("region")) {
            } else if (tagName.equalsIgnoreCase("paint")) {
                colorGradient = null;
                //System.out.println("end paint tag");
                if (content.length() > 0) {
                    //fill color
                    final StringTokenizer tokens = new StringTokenizer(content);
                    //TODO: use color names
                    if (tokens.countTokens() < 3) {
                        //oops
                        System.out.println("not enough colors");
                    }
                    final String redString = tokens.nextToken();
                    final String greenString = tokens.nextToken();
                    final String blueString = tokens.nextToken();
                    double red = 0.0;
                    double green = 0.0;
                    double blue = 0.0;
                    try {
                        red = Double.parseDouble(redString);
                        green = Double.parseDouble(greenString);
                        blue = Double.parseDouble(blueString);
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }

                    final Color color = new Color(red, green, blue);
                    final FillColor fillColor = new FillColor(color);
                    //System.out.println("setting fill paint");
                    region.setPaint(fillColor);
                }
                
            } else if (tagName.equalsIgnoreCase("linearGradient")) {
                final StringTokenizer tokens = new StringTokenizer(content);
                try {
                    final double start = Double.parseDouble(tokens.nextToken());
                    final Point2D startPoint = new Point2D.Double(0, start); 
                    final Point2D controlPoint = parsePoint(tokens.nextToken());
                    final double end = Double.parseDouble(tokens.nextToken());
                    final Point2D endPoint = new Point2D.Double(Util.length(linearGradient.getGradientLine()), end); 
                    linearGradient.setCurve(new NonLinearCurve2D(startPoint,
                            controlPoint, endPoint));
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            } else if (tagName.equals("point")) {
                //TODO: make sure content is valid
                final Polyline polyline = new Polyline();
                polyline.add(parsePoint(content));
                region.add(polyline);
            } else if (tagName.equalsIgnoreCase("line")) {
                final Polyline polyline = new Polyline();
                processPoints(polyline, content);
                region.add(polyline);
            } else if (tagName.equalsIgnoreCase("polygon")) {
                final Polygon polygon = new Polygon();
                processPoints(polygon, content);
                region.add(polygon);
            } else if (tagName.equalsIgnoreCase("rectangle")) {
                final StringTokenizer tokens = new StringTokenizer(content);
                final String upperLeftString = tokens.nextToken();
                final Point2D upperLeft = parsePoint(upperLeftString);
                final String lowerRightString = tokens.nextToken();
                final Point2D lowerRight = parsePoint(lowerRightString);
                final Point2D upperRight = new Point2D.Double(lowerRight.getX(), upperLeft.getY());
                final Point2D lowerLeft = new Point2D.Double(upperLeft.getX(), lowerRight.getY());
                final Polygon polygon = new Polygon();
                polygon.add(upperLeft);
                polygon.add(upperRight);
                polygon.add(lowerRight);
                polygon.add(lowerLeft);
                region.add(polygon);

            }
            stringBuffer.setLength(0);
        }
    }

    private static void processPoints(Shape shape, String content) {
        final StringTokenizer tokens = new StringTokenizer(content);
        //first get the start point
        final String startPointString = tokens.nextToken();
        Point2D point = parsePoint(startPointString);
        Point2D origin = point;
        shape.add(point);

        while (tokens.hasMoreTokens()) {
            final String command = tokens.nextToken().toLowerCase();
            //TODO: check to make sure it starts with h or v
            final String direction = command.substring(0, 1);
            final String distanceString = command.substring(1);
            int distance = 0;
            if (direction.equalsIgnoreCase("h") || direction.equalsIgnoreCase("v")) {
                try {
                    distance = Integer.parseInt(distanceString);
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            }
            int count = 1;
            if (distance < 0) {
                count = -1;
            }
            Point2D nextPoint = null;
            if (direction.equalsIgnoreCase("h")) {
                nextPoint = new Point2D.Double(point.getX() + distance, point.getY());
            } else if (direction.equalsIgnoreCase("v")) {
                nextPoint = new Point2D.Double(point.getX(), point.getY() + distance);
            } else {
                nextPoint = parsePoint(command);
            }
            
            
            shape.add(nextPoint);
            point = nextPoint;
            
        }
    }

    private static Point2D parsePoint(String content) {
        final StringTokenizer tokens = new StringTokenizer(content, ",");
        //TODO: make sure count is OK
        final String xString = tokens.nextToken().trim();
        final String yString = tokens.nextToken().trim();
        double x = 0;
        double y = 0;
        try {
            x = Double.parseDouble(xString);
            y = Double.parseDouble(yString);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return new Point2D.Double(x, y);
    }

    private static void parseXmlString(String input, DefaultHandler handler, boolean validating) {
        try {
            // Create a builder factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(validating);

            // Create the builder and parse the file
			StringBufferInputStream inputStream = new StringBufferInputStream(input);
            factory.newSAXParser().parse(inputStream, handler);
			inputStream.close();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    private static void parseXmlFile(File file, DefaultHandler handler, boolean validating) {
        try {
            // Create a builder factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(validating);

            // Create the builder and parse the file
            factory.newSAXParser().parse(file, handler);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
