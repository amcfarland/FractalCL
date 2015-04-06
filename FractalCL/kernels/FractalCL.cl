/*
 * Andrew Mcfarland [https://github.com/amcfarland]
 *
 * based on  http://www.jocl.org/samples/SimpleMandelbrot.cl
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */

// output        : A buffer with sizeX*sizeY elements, storing
//                 the colors as RGB ints
// sizeX, sizeX  : The width and height of the buffer
// xCenter,yCenter: values for center of image
// maxIterations : The maximum number of iterations
// colorMap      : A buffer with colorMapSize elements,
//                 containing the pixel colors

//#pragma OPENCL EXTENSION cl_khr_fp64 : enable 

__kernel void computeMandelbrot(
    __global uint *output, 
    int width,
    int height,
    double centerX, 
    double centerY,
    double pixelScale,
    int maxIterations, 
    __global uint *colorMap,
    int colorMapSize
    )



{

    unsigned int ix = get_global_id(0);
    unsigned int iy = get_global_id(1);

   double r = (centerX - ((width  / 2) * pixelScale)) + (get_global_id(0) * pixelScale);
   double i = (centerY - ((height / 2) * pixelScale)) + (get_global_id(1) * pixelScale);
   
   
    double x = 0;
    double y = 0;
  	double xx;
  	double yy;
    double magnitudeSquared = 0;
    int iteration = 0;
    while (iteration<maxIterations && magnitudeSquared<4)
    {
        xx = x*x;
        yy = y*y;
        y = 2*x*y+i;
        x = xx-yy+r;
        magnitudeSquared=xx+yy;
        iteration++;
    }
     if (iteration == maxIterations){
        output[iy*width+ix] = 0;
    }else{
	     output[iy*width+ix] = colorMap[iteration];
    
	}

	
}

