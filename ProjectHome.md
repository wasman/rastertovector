## Description ##
The aim of this project is to develop a set of tools to extract data from raster images into a vector format.  The input is a raster image file such as .gif, .png, .jpg.  The output is a SVG file that can be viewed in Firefox or Inkscape.  The application currently attempts to extract linear gradients from a portion of a raster image.  This is different from tracing as no edge data is output.

### Tutorial ###
I wrote up a small brief tutorial [here](http://code.google.com/p/rastertovector/wiki/Tutorial).

## Requirements ##
Requires Java 6 or higher. [[Download](http://java.sun.com/javase/downloads/index.jsp)]
Windows, OS X, Linux or any any OS that runs Java 6.

## To Run ##
Download runsxz.jar and from the command line in the download directory execute: "java -jar runsxz.jar".

Open a file from the menu File->Open.

Drag your mouse over a small area to select a potential gradient to extract.

Save the SVG output and view it with Firefox to determine the quality of the output.  Not all regions of an image will product a linear gradient, it may take several tries to achieve the output you wish.Not everything in a raster image is a gradient.

## Inkscape ##
To integrate the output into Inkscape, open the output SVG file with it and copy and Paste Style onto an existing region.  Then modify the linear gradient as needed to get the desired effect.