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
package com.sxz.math;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public final class ReduceError {
	public double error;
	public double errorIndex;
	public Point2D start;
	public Point2D control;
	public Point2D end;

	ReduceError() {
		super();
	}

	public boolean equals(ReduceError reduceError) {
		return start.equals(reduceError.start) && end.equals(reduceError.end) &&
			control.equals(reduceError.control);
	}
}
