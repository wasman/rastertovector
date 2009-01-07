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
package com.sxz.math.test;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Iterator;
import com.sxz.math.*;

public final class TestCurveGUI {

	public TestCurveGUI() {
		super();
	}

	public static void main(String[] args) {
		final Drawable panel = new Drawable();

		JFrame frame = new JFrame();
		Action action = new AbstractAction("draw") {
			// This method is called when the button is pressed
			public void actionPerformed(ActionEvent evt) {
				panel.drawCurve();
			}
		};
		JButton button = new JButton(action);
		Action clearAction = new AbstractAction("clear") {
			// This method is called when the button is pressed
			public void actionPerformed(ActionEvent evt) {
				panel.clear();
			}
		};
		JButton clearButton = new JButton(clearAction);
		final JPanel buttons = new JPanel();
		buttons.add(button);
		buttons.add(clearButton);

		// Add button to the frame
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.getContentPane().add(buttons, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set initial size
		int frameWidth = 600;
		int frameHeight = 500;
		frame.setSize(frameWidth, frameHeight);
		frame.setLocation(100, 100);

		// Show the frame
		frame.setVisible(true);

	}

	static class Drawable extends JComponent {
		private MouseListener listener = null;
		private NonLinearCurve2D curve = null;

		Drawable() {
			super();
			setDoubleBuffered(true);
			listener = new MouseListener(this);
			addMouseListener(listener);
		}

		void drawCurve() {
			if (listener.getSampler().size() == 0) {
				return;
			}
			curve = new NonLinearCurve2D();
			final Iterator iterator = listener.getSampler().iterator();
			while (iterator.hasNext()) {
				final Sample sample = (Sample)iterator.next();
				curve.add(sample.getX(), sample.getAverage());
			}
			repaint();
		}

		void clear() {
			curve = null;
			listener.getSampler().clear();
			repaint();
		}

		// This method is called whenever the contents needs to be painted
		public void paint(Graphics g) {
			// Retrieve the graphics context; this object is used to paint shapes
			Graphics2D g2d = (Graphics2D)g;
			g2d.setPaint(java.awt.Color.white);

			int x = 0;
			int y = 0;
			int width = getSize().width - 1;
			int height = getSize().height - 1;
			g2d.fillRect(x, y, width, height);
			g2d.setPaint(java.awt.Color.black);
			final SampleContainer sampler = listener.getSampler();
			System.out.println("sample size is " + sampler.size());
			final Iterator iterator = sampler.iterator();
			while (iterator.hasNext()) {
				final Sample sample = (Sample)iterator.next();
				System.out.println("Drawing " + sample);
				g2d.drawRect((int)Math.round(sample.getX()) - 2, (int)Math.round(sample.getAverage()) - 2, 5, 5);
			}
			if (curve != null) {
				g2d.setPaint(java.awt.Color.blue);
				final ReduceError reduceError = curve.getReduceError();
				System.out.println("error is " + reduceError.error);
				final QuadCurve2D quadCurve = curve.getQuadCurve2D(reduceError);
				g2d.draw(quadCurve);
				g2d.setPaint(java.awt.Color.green);
				final Point2D start = reduceError.start;
				g2d.fillRect((int)Math.round(start.getX() - 2), (int)Math.round(start.getY()) - 2, 5, 5);
				final Point2D end = reduceError.end;
				g2d.fillRect((int)Math.round(end.getX() - 2), (int)Math.round(end.getY()) - 2, 5, 5);
				g2d.setPaint(java.awt.Color.red);
				final Point2D control = reduceError.control;
				g2d.fillRect((int)Math.round(control.getX()) - 2, (int)Math.round(control.getY()) - 2, 5, 5);
				/*
				ReduceError newError = curve.predict(reduceError);
				if (newError != null) {
					System.out.println("have predicted error of " + newError.error);
					final QuadCurve2D newQuadCurve = curve.getQuadCurve2D(newError);
					g2d.setPaint(java.awt.Color.red);
					g2d.setStroke (new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] {12f, 12f}, 0f));

					g2d.draw(newQuadCurve);
				}
				*/

				/*
				g2d.setPaint(java.awt.Color.blue);
				final ArrayList lines = curve.getLines(quadCurve);
				for (int i = 0; i < lines.size(); i++) {
					final Line2D line = (Line2D)lines.get(i);
					g2d.draw(line);
				}
				*/

			}

		}
	}

	static class MouseListener extends MouseAdapter {
		private SampleContainer sampler = new SampleContainer();
		private Drawable drawable = null;

		MouseListener(Drawable drawable) {
			super();
			this.drawable = drawable;
		}

		public void mouseClicked(MouseEvent e) {
			System.out.println("Mouse clicked on " + e.getX() + " " + e.getY());
			sampler.add(e.getX(), e.getY()); 
			drawable.repaint();
		}

		SampleContainer getSampler() {
			return sampler;
		}

	}



}
