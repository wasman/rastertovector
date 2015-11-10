# Tutorial #

## Introduction ##
This tutorial is designed to illustrate how to take a vector based image (SVG) and fill it with linear gradients pulled from raster images.  I use Inkscape here since it can read and write SVG.  Gradient Extractor will fill the role of pulling the SVG linear gradient from the raster image and saving it to SVG.

### Start with an Outline ###
First I drew an outline of a flower in Inkscape using the Bezier Curve tool.  Complaints about my own meager artistic skills aside:
![![](http://rastertovector.googlecode.com/files/drawing1.png)](http://rastertovector.googlecode.com/files/drawing1.svg)
Now I need to fill in the different parts of the vector image.

### Surf for Linear Gradients for the Petals ###
I think a nice blue would look good for the petals.  Since I am lazy, I will use one linear gradient for all the petals.  So I go to [Google Images](http://images.google.com/) and look up "gradient bit test" and find [this image](http://images.google.com/imgres?imgurl=http://www.visi.com/~leppik/images/bit_test24.png&imgrefurl=http://forum.notebookreview.com/showthread.php%3Ft%3D141895%26page%3D9&h=375&w=512&sz=62&hl=en&start=1&sig2=DMZU8e8F2rZerL2TDUX2bQ&um=1&usg=__2txvN9v1Vo8oLS7w1NgtxONG21k=&tbnid=NPj41MjkXRdY3M:&tbnh=96&tbnw=131&ei=1o7NSJ6NCZOMsQO28byHBw&prev=/images%3Fq%3Dgradient%2Bbit%2Btest%26um%3D1%26hl%3Den%26client%3Dfirefox-a%26rls%3Dorg.mozilla:en-US:official%26sa%3DG).  I download that image to "My Documents".

I can then open the "Linear Gradient Extractor" program and go to File->Open and select the image I just downloaded.  Then using the mouse, click and drag over the image.  The result is:
http://rastertovector.googlecode.com/files/screencapture1.PNG

Once the "Save As" dialog pops up, save the resulting SVG file to an appropriate location.  In Inkscape, open the the SVG file you just saved.  Copy and in the window of the flower outline I am editing, I Paste->Style on a petal.  The puts the linear gradient into a manageable form.  I stretch the linear gradient as I desire to get the effect and set the same gradient on all the remaining petals.

### Surf for Linear Gradients for the Center ###
Something tragic and purple I think would look nice for the center oval.  Again on Google Image I look for "glossy purple" and find some nice [bathroom tile](http://images.google.com/imgres?imgurl=http://www.glasstileimports.com/image/image/133/purple_2x2_glossy.jpg&imgrefurl=http://www.glasstileimports.com/product_line/Northern_Lights_Glass_Tiles/Celestial&h=500&w=333&sz=87&hl=en&start=61&sig2=2p_tanAVdoLKrpNmj2aVfg&um=1&usg=__O6d5uxMANyKkPy5Ny-SAQ4FhqEU=&tbnid=S0rXqtFQwlGO4M:&tbnh=130&tbnw=87&ei=hpDNSI__MZ7ItQPvgqHSCA&prev=/images%3Fq%3Dglossy%2Bpurple%26start%3D60%26ndsp%3D20%26um%3D1%26hl%3Den%26client%3Dfirefox-a%26rls%3Dorg.mozilla:en-US:official%26sa%3DN).

I download the image and open it in the Gradient Extractor to return the SVG that I will open in Inkscape.  Once the gradient is configured in Inkcape I get a final image:
![![](http://rastertovector.googlecode.com/files/drawing4.png)](http://rastertovector.googlecode.com/files/drawing4.svg)

### Denouement ###
Since I suck at art, but I know what I like, it is much easier just to pull what I see off of raster images than to generate the gradients from scratch.  Also, the Gradient Extractor can remove unwanted foreground pixels and just return the what I want.