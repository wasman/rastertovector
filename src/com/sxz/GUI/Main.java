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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Cursor;
import java.util.Properties;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import javax.swing.JFrame;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.Box;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import javax.imageio.ImageIO;
import com.sxz.parser.FrameParser;
import com.sxz.parser.LocationPool;
import com.sxz.parser.FetchImage;
import com.sxz.raster.Parser;

final class Main {

	private static final String TITLE = "SVG Gradient Extractor";

	private static BufferedImage IMAGE = null;
	private static String FILENAME = null;

    Main() {
        super();
    }

	private static int getPropertyValue(Properties properties, String key, String defaultValue) {
		try {
			return Integer.parseInt(properties.getProperty(key, defaultValue));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		return 0;
	}

	public static void main(String[] args) {
		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("gradient.properties"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

        final JFrame frame = new JFrame(TITLE);
		final Drawable drawable = new Drawable();
		final JMenuBar menuBar = new JMenuBar();
		final JScrollPane scrollPane = new JScrollPane(drawable);
		scrollPane.setAutoscrolls(true);
		scrollPane.getViewport().setBackground(java.awt.Color.gray);
		drawable.setPreferredSize(new Dimension(600, 400));
		//scrollPane.getViewport().setPreferredSize(new Dimension(800, 600));

		final JMenu menu = new JMenu("File");
		menuBar.add(menu);

		final JMenuItem open = new JMenuItem("Open");
		menu.add(open);

		final JMenu view = new JMenu("View");
		menuBar.add(view);

		menuBar.add(Box.createHorizontalGlue());

		final JMenu help = new JMenu("Help");
		menuBar.add(help);

		final JMenuItem about = new JMenuItem("About");
		help.add(about);

		Action aboutAction = new AbstractAction("about") {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(frame, "SVG Gradient Extractor\nhttp://www.darklilac.com\nAuthor: Darklilac", "About", JOptionPane.PLAIN_MESSAGE);
			}
		};
		about.addActionListener(aboutAction);

		final JMenuItem normal = new JMenuItem("Normal");
		normal.setEnabled(false);
		view.add(normal);
		Action normalAction = new AbstractAction("normal") {
			public void actionPerformed(ActionEvent event) {
				drawable.setPreferredSize(new Dimension(IMAGE.getWidth(), IMAGE.getHeight()));
				drawable.drawImage(IMAGE);
			}
		};
		normal.addActionListener(normalAction);

		final JMenuItem large = new JMenuItem("Large");
		large.setEnabled(false);
		view.add(large);
		Action largeAction = new AbstractAction("large") {
			public void actionPerformed(ActionEvent event) {
				final BufferedImage largeImage = resizeImage(2);
				drawable.setPreferredSize(new Dimension(largeImage.getWidth(), largeImage.getHeight()));
				drawable.drawImage(largeImage);
				scrollPane.revalidate();
			}
		};
		large.addActionListener(largeAction);

		final JMenuItem extraLarge = new JMenuItem("Extra Large");
		extraLarge.setEnabled(false);
		view.add(extraLarge);
		Action extraLargeAction = new AbstractAction("extraLarge") {
			public void actionPerformed(ActionEvent event) {
				final BufferedImage largeImage = resizeImage(3);
				drawable.setPreferredSize(new Dimension(largeImage.getWidth(), largeImage.getHeight()));
				drawable.drawImage(largeImage);
				scrollPane.revalidate();
			}
		};
		extraLarge.addActionListener(extraLargeAction);

		Action extractSVGAction = new AbstractAction("extractSVG") {
			public void actionPerformed(ActionEvent event) {
				final BufferedImage result = drawable.extract();
				if (result == null) {
					return;
				}
				if (result.getWidth() * result.getHeight() > getPropertyValue(properties, "maxsize", "10000")) {
					JOptionPane.showMessageDialog(frame, "Selected area too large");
					return;
				}
				FrameParser parser = null;
				try {
					drawable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					final LocationPool locationPool = new LocationPool(result);
					parser = new FrameParser(locationPool);
					parser.setThreshold(getPropertyValue(properties, "threshold", "40"));
					parser.setThreshold(getPropertyValue(properties, "separation", "60"));
					parser.process();
				} finally {
					drawable.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				FormatSVG format = new FormatSVG();
				//System.out.println(format.format(parser.getFrame()));
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new SVGFileFilter());
				int value = fileChooser.showSaveDialog(frame);
				if (value == JFileChooser.APPROVE_OPTION) {
					System.out.println("trying to save file " + fileChooser.getSelectedFile().getName());
					FormatXML formatXML = new FormatXML();
					writeFile(fileChooser.getSelectedFile().getAbsolutePath(),
							format.format(parser.getFrame()));
				}

			}
		};

		final JMenuItem extractSVG = new JMenuItem("Extract SVG");
		extractSVG.addActionListener(extractSVGAction);
		extractSVG.setEnabled(false);
		menu.add(extractSVG);

		Action extractXMLAction = new AbstractAction("extractXML") {
			public void actionPerformed(ActionEvent event) {
				final BufferedImage result = drawable.extract();
				if (result == null) {
					return;
				}
				if (result.getWidth() * result.getHeight() > getPropertyValue(properties, "maxsize", "10000")) {
					JOptionPane.showMessageDialog(frame, "Selected area too large");
					return;
				}
				FrameParser parser = null;
				try {
					drawable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					final LocationPool locationPool = new LocationPool(result);
					parser = new FrameParser(locationPool);
					parser.setThreshold(getPropertyValue(properties, "threshold", "40"));
					parser.setThreshold(getPropertyValue(properties, "separation", "60"));
					parser.process();
				} finally {
					drawable.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new XMLFileFilter());
				int value = fileChooser.showSaveDialog(frame);
				if (value == JFileChooser.APPROVE_OPTION) {
					System.out.println("trying to save file " + fileChooser.getSelectedFile().getName());
					FormatXML formatXML = new FormatXML();
					writeFile(fileChooser.getSelectedFile().getAbsolutePath(),
							formatXML.format(parser.getFrame()));
				}

			}
		};

		final JMenuItem extractXML = new JMenuItem("Extract XML");
		extractXML.addActionListener(extractXMLAction);
		extractXML.setEnabled(false);
		menu.add(extractXML);

		//Get nonlinear drawn image
		Action extractPNGAction = new AbstractAction("extractPNG") {
			public void actionPerformed(ActionEvent event) {
				final BufferedImage result = drawable.extract();
				if (result == null) {
					return;
				}
				if (result.getWidth() * result.getHeight() > getPropertyValue(properties, "maxsize", "10000")) {
					JOptionPane.showMessageDialog(frame, "Selected area too large");
					return;
				}
				System.out.println("Have valid data");
				FrameParser parser = null;
				try {
					drawable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					final LocationPool locationPool = new LocationPool(result);
					parser = new FrameParser(locationPool);
					parser.setThreshold(getPropertyValue(properties, "threshold", "40"));
					parser.setThreshold(getPropertyValue(properties, "separation", "60"));
					parser.process();
				} finally {
					drawable.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}

				FormatXML format = new FormatXML();
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new PNGFileFilter());
				int value = fileChooser.showSaveDialog(frame);
				if (value == JFileChooser.APPROVE_OPTION) {
					System.out.println("trying to save file " + fileChooser.getSelectedFile().getAbsolutePath());
					String outputXML = format.format(parser.getFrame());
					//System.out.println("Output XML: " + outputXML);
					
					Parser.parseString(outputXML, fileChooser.getSelectedFile().getAbsolutePath());
					//ImageIO.write(result, ".png", fileChooser.getSelectedFile());
				}

			}
		};
		final JMenuItem extractPNG = new JMenuItem("Extract PNG");
		extractPNG.addActionListener(extractPNGAction);
		extractPNG.setEnabled(false);
		menu.add(extractPNG);

		Action extractOriginalPNGAction = new AbstractAction("extractOriginalPNG") {
			public void actionPerformed(ActionEvent event) {
				final BufferedImage result = drawable.extract();
				if (result == null) {
					return;
				}
				if (result.getWidth() * result.getHeight() > getPropertyValue(properties, "maxsize", "10000")) {
					JOptionPane.showMessageDialog(frame, "Selected area too large");
					return;
				}

				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new PNGFileFilter());
				int value = fileChooser.showSaveDialog(frame);
				if (value == JFileChooser.APPROVE_OPTION) {
					System.out.println("trying to save file " + fileChooser.getSelectedFile().getName());
					try {
						ImageIO.write(result, "png", fileChooser.getSelectedFile());
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}

			}
		};

		final JMenuItem extractOriginalPNG = new JMenuItem("Extract Original PNG");
		extractOriginalPNG.addActionListener(extractOriginalPNGAction);
		extractOriginalPNG.setEnabled(false);
		menu.add(extractOriginalPNG);

		Action extractAllAction = new AbstractAction("extractAll") {
			public void actionPerformed(ActionEvent event) {
				final BufferedImage result = drawable.extract();
				if (result == null) {
					return;
				}
				if (result.getWidth() * result.getHeight() > getPropertyValue(properties, "maxsize", "10000")) {
					JOptionPane.showMessageDialog(frame, "Selected area too large");
					return;
				}

				FrameParser parser = null;
				try {
					drawable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					final LocationPool locationPool = new LocationPool(result);
					parser = new FrameParser(locationPool);
					parser.setThreshold(getPropertyValue(properties, "threshold", "40"));
					parser.setThreshold(getPropertyValue(properties, "separation", "60"));
					parser.process();
				} finally {
					drawable.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}

				System.out.println("trying to save data from file " + FILENAME);

				FormatXML formatXML = new FormatXML();
				FormatSVG formatSVG = new FormatSVG();
				//write out xvg	
				System.out.println("writing svg");
				writeFile(FILENAME + ".svg",
						formatSVG.format(parser.getFrame()));
				//write out raster output
				String outputXML = formatXML.format(parser.getFrame());
				System.out.println("XML output is " + outputXML);
				System.out.println("writing rasterized png");
				Parser.parseString(outputXML, FILENAME + ".png");
				//write out xml
				System.out.println("writing xml");
				writeFile(FILENAME + ".xml", outputXML);
				//and output source png raster data
				System.out.println("writing source png");
				try {
					ImageIO.write(result, "png", new File(FILENAME + ".source.png"));
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}

			}
		};

		final JMenuItem extractAll = new JMenuItem("Extract All");
		extractAll.addActionListener(extractAllAction);
		extractAll.setEnabled(false);
		menu.add(extractAll);

		Action action = new AbstractAction("open") {
			public void actionPerformed(ActionEvent event) {
				//open file here
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new ImageFileFilter());
				int value = fileChooser.showOpenDialog(frame);
				if (value == JFileChooser.APPROVE_OPTION) {
					System.out.println("trying to open file " + fileChooser.getSelectedFile().getName());
					frame.setTitle(TITLE + " - " + fileChooser.getSelectedFile().getName());
					FILENAME = fileChooser.getSelectedFile().getAbsolutePath();

					IMAGE = FetchImage.getImage(fileChooser.getSelectedFile().getAbsolutePath());
					drawable.setPreferredSize(new Dimension(IMAGE.getWidth(), IMAGE.getHeight()));
					drawable.drawImage(IMAGE);
					extractSVG.setEnabled(true);
					extractAll.setEnabled(true);
					extractXML.setEnabled(true);
					extractPNG.setEnabled(true);
					extractOriginalPNG.setEnabled(true);
					normal.setEnabled(true);
					large.setEnabled(true);
					extraLarge.setEnabled(true);
					scrollPane.revalidate();
				}
			}
		};

		Action exitAction = new AbstractAction("exit") {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		};

		open.addActionListener(action);

		final JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(exitAction);
		menu.add(exit);

		frame.setJMenuBar(menuBar);

        // Add button to the frame
		final JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(scrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(topPanel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set initial size
        int frameWidth = 800;
        int frameHeight = 600;
        frame.setSize(frameWidth, frameHeight);
        frame.setLocation(100, 100);

        // Show the frame
        frame.setVisible(true);
	}

	static BufferedImage resizeImage(int multiplier) {
		final AffineTransform transform = new AffineTransform();
		transform.scale(multiplier, multiplier);
		final AffineTransformOp imageOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		return imageOp.filter(IMAGE, null);
	}

	static void writeFile(String filename, String output) {
		final File file = new File(filename);
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)));
			out.print(output);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
				out = null;
			}
		}
	}
}
