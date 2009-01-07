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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;

final class Drawable extends JPanel {
	private MouseListener listener = null;
	private BufferedImage image = null;

	Drawable() {
		super();
		setDoubleBuffered(true);
		listener = new MouseListener(this);
		addMouseListener(listener);
		addMouseMotionListener(listener);
	}

	void drawImage(BufferedImage image) {
		this.image = image;
		setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		clearSelection();
		repaint();
	}

	void clear() {
		image = null;
		clearSelection();
		repaint();
	}

	private void clearSelection() {
		listener.x = -1;
		listener.y = -1;
		listener.width = -1;
		listener.height = -1;
	}

	BufferedImage extract() {
		if (image == null) {
			return null;
		}
		if (listener.x + listener.width > image.getWidth()) {
			clearSelection();
			repaint();
			return null;
		}
		if (listener.y + listener.height > image.getHeight()) {
			clearSelection();
			repaint();
			return null;
		}
		return image.getSubimage(listener.x, listener.y, listener.width,
				listener.height);
	}

	// This method is called whenever the contents needs to be painted
	public void paint(Graphics g) {
		// Retrieve the graphics context; this object is used to paint shapes
		Graphics2D g2d = (Graphics2D)g;
		g2d.setPaint(java.awt.Color.gray);

		int x = 0;
		int y = 0;
		int width = getSize().width - 1;
		int height = getSize().height - 1;
		g2d.fillRect(x, y, width, height);

		if (image != null) {
			g2d.drawImage(image, null, null);

			if (listener.width != -1) {
				//System.out.println("drawing rect " + listener.x + " " +
						//listener.y + " " + listener.width + " " +
						//listener.height);
				g2d.setPaint(java.awt.Color.black);
				g2d.setXORMode(java.awt.Color.white);
				g2d.drawRect(listener.x, listener.y, listener.width, listener.height);
			}
		}
	}
}

