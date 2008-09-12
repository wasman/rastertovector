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

import java.util.Iterator;
import java.util.HashSet;

public final class PaintGenerator {

    private static LinearGradientParser RED_LINEAR_PARSER = new LinearGradientParser.Red();
    private static LinearGradientParser GREEN_LINEAR_PARSER = new LinearGradientParser.Green();
    private static LinearGradientParser BLUE_LINEAR_PARSER = new LinearGradientParser.Blue();


    private PaintGenerator() {
        super();
    }

    //synchronize here on the static members to keep thread safe
    public static ColorGradient generate(HashSet locations) {
        //System.out.println("rebuilding gradient with size " + locations.size());
        final ColorGradient linearColorGradient = new ColorGradient();
        //first lets test linear to see what we have
        linearColorGradient.setRed(RED_LINEAR_PARSER.getGradient(locations));
        linearColorGradient.setGreen(GREEN_LINEAR_PARSER.getGradient(locations));
        linearColorGradient.setBlue(BLUE_LINEAR_PARSER.getGradient(locations));

        //final double worstError = linearColorGradient.getWorstError();
        //System.out.println("linearWorstError is " + worstError);

        return linearColorGradient;
    }
}
