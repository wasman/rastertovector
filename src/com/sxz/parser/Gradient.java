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

import com.sxz.math.ReduceError;

public abstract class Gradient {

    private ReduceError reduceError;
    public Gradient() {
        super();
    }

    public double getError() {
        return reduceError.error;
    }

    public void setReduceError(ReduceError reduceError) {
        this.reduceError = reduceError;
    }

    public ReduceError getReduceError() {
        return reduceError;
    }

    //public abstract double getValue(double x, double y);

    public abstract double findError(double x, double y, double value);

    public abstract boolean compareTo(Gradient gradient);

}
