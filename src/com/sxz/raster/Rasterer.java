package com.sxz.raster;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.awt.geom.Point2D;
import com.sxz.parser.Color;
import com.sxz.parser.Rectangle;

//LDR rasterer
public class Rasterer {

	public Rasterer() {
	}

	public void draw(Frame frame, String outputFilename) {
		final ImageWriter imageWriter = new ImageWriter();
        imageWriter.setBackground(java.awt.Color.green);
        //TODO: make the width and height dynamic
        final Rectangle rectangle = frame.getBoundingBox();
		imageWriter.setWidth((int)Math.round(rectangle.getX2()) + 1);
		imageWriter.setHeight((int)Math.round(rectangle.getY2()) + 1);
        System.out.println("Creating image " + imageWriter.getWidth() + " " + imageWriter.getHeight());
		final BufferedImage image = imageWriter.getBufferedImage();

		final Iterator iterator = frame.iterator();
		double maximumError = 0;
		while (iterator.hasNext()) {
            final Region region = (Region)iterator.next();

            final Iterator points = region.getPoints().iterator();
            while (points.hasNext()) {
                final Point2D point = (Point2D)points.next();
                //System.out.println("Drawing point " + point);
                Color color = region.getColor(point);
                //System.out.println("have Ycrcb color " + color);
                //color = color.convertToRGB();
                //System.out.println("have rgb color " + color);
                //worst gamut ever...
                try {
                    image.setRGB((int)Math.round(point.getX()), (int)Math.round(point.getY()), color.getRGB());
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("for point " + point + " " + color);
                    e.printStackTrace();
                    break;
                }
            }
		}
        //TODO: make output file name dynamic
		imageWriter.save(outputFilename);
	}

}
