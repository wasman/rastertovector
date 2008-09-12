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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public final class FetchImage {

	public static BufferedImage getImage(String name) {
		if (name == null) {
			//error message here
			return null;

		}
		if (name.startsWith("http:") || name.startsWith("ftp:")) {
			try {
				final URL url = new URL(name);
				final BufferedImage source = ImageIO.read(url);
				return source;
			} catch (MalformedURLException mfe) {
				mfe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		final File file = new File(name);
		if (!file.exists()) {
			//error message here
			return null;
		}
		if (!file.canRead()) {
			//error message here
			return null;
		}

		try {
			final BufferedImage source = ImageIO.read(file);
            //final boolean isAlpha = source.getColorModel().isAlphaPremultiplied();
            return source;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}
}
