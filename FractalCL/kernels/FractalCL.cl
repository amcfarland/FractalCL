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

__kernel void computeMandelbrot(
    __global uint *output, 
    int width,
    int height,
    float centerX, 
    float centerY,
    float pixelScale,
    int maxIterations, 
    __global uint *colorMap,
    int colorMapSize
    )



{

    unsigned int ix = get_global_id(0);
    unsigned int iy = get_global_id(1);

   // float r = x0 + ix * (x1-x0) / sizeX;
  //  float i = y0 + iy * (y1-y0) / sizeY;
   float r = (centerX - ((width  / 2) * pixelScale)) + (get_global_id(0) * pixelScale);
   float i = (centerY - ((height / 2) * pixelScale)) + (get_global_id(1) * pixelScale);
   
   
    float x = 0;
    float y = 0;

    float magnitudeSquared = 0;
    int iteration = 0;
    while (iteration<maxIterations && magnitudeSquared<4)
    {
        float xx = x*x;
        float yy = y*y;
        y = 2*x*y+i;
        x = xx-yy+r;
        magnitudeSquared=xx+yy;
        iteration++;
    }
     if (iteration == maxIterations){
        output[iy*width+ix] = 0;
    }else{
       // float alpha = (float)iteration/maxIterations;
       // int colorIndex = (int)(alpha * colorMapSize);
     //   output[iy*width+ix] = colorMap[colorIndex];
     output[iy*width+ix] = colorMap[iteration];
    
	}
    
    
    /*
    
   int ix = get_global_id(0);
   int iy = get_global_id(1);

   int count = 0;
   float zx = centerX;
   float zy = centerY;
   float new_zx = 0.0;
   float x = (centerX - ((width  / 2) * pixelScale)) + (get_global_id(0) * pixelScale);
   float y = (centerY - ((height / 4) * pixelScale)) + (get_global_id(1) * pixelScale);
   
   while (count<maxIterations && new_zx<0.01){
      new_zx = ((zx * zx) - (zy * zy)) + x;
      zy = ((2.0 * zx) * zy) + y;
      zx = new_zx;
      count++;
   }
   
	*/
}

