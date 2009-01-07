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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class MouseListener extends MouseAdapter {
	private Drawable drawable = null;
	int x = -1;
	int y = -1;
	int width = -1;
	int height = -1;

	MouseListener(Drawable drawable) {
		super();
		this.drawable = drawable;
	}

	public void mousePressed(MouseEvent e) {
		x = e.getX();
		y = e.getY();
	}

	public void mouseDragged(MouseEvent e) {
		if (x == -1) {
			return;
		}
		width = e.getX() - x;
		height = e.getY() - y;
		drawable.repaint();
	}

	public void mouseClicked(MouseEvent e) {
		x = -1;
		y = -1;
		width = -1;
		height = -1;
		drawable.repaint();
	}
}
