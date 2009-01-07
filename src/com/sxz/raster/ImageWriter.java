package com.sxz.raster;

import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.Image;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.Shape;
import java.text.AttributedCharacterIterator;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class ImageWriter {

	private BufferedImage bufferedImage;
	private boolean alpha;
	private Color background;
	private int width;
	private int height;

	public ImageWriter() {
		alpha = false;
		background = Color.WHITE;
		width = 100;
		height = 100;
	}

	public BufferedImage getBufferedImage() {
		restoreBufferedImage();
		return bufferedImage;
	}

	public void setAlpha(boolean alpha) {
		this.alpha = alpha;
	}

	public boolean getAlpha() {
		return alpha;
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

	public void setBackground(Color background) {
		this.background = background;
	}

	public Color getBackground() {
		return background;
	}

	private void restoreBufferedImage() {
		if (bufferedImage != null) {
			return;
		}
		if (alpha) {
			bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB );
		} else {
			bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
		final Graphics2D graphic = (Graphics2D)bufferedImage.getGraphics();
		//graphic.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		graphic.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (alpha) {
			graphic.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			
		}
		graphic.setBackground(background);
		graphic.clearRect(0, 0, width, height);
	}

	public String save(String filename) {
		//save to the filename and cache the image
		String extension = "png";
		final String[] formats = ImageIO.getWriterFormatNames();
		for (int i = 0; i < formats.length; i++) {
			if (filename.endsWith(formats[i])) {
				extension = formats[i];
			}
		}
		final File file = new File(filename);
		/*
		final File directory = file.getParentFile();
		if (directory != null && !directory.canWrite()) {
			System.oput.println("Cannot write to file " + file.getAbsolutePath());
			return "Can't write to file " + filename;
		}
		*/
		try {
			System.out.println("Saving image " + file);
			ImageIO.write(bufferedImage, extension, file);
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return "success";
	}
}
