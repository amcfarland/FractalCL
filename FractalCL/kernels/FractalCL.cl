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

int getIterations(int maxIterations, double r, double i);
int getIterations(int maxIterations, double r, double i){
	
	int iteration = 0;
	double xx;
  	double yy;
    double x = 0;
    double y = 0;
    double magnitudeSquared = 0;
    
    while (iteration<maxIterations && magnitudeSquared<4){
        xx = x*x;
        yy = y*y;
        y = 2*x*y+i;
        x = xx-yy+r;
        magnitudeSquared=xx+yy;
        iteration++;
    }
	return iteration;
}



__kernel void computeMandelbrot(
    __global uint *output, 
    int width,
    int height,
    double centerX, 
    double centerY,
    int aggregationSize,
    __global uint *aggregationGrid,
    double pixelScale,
    int maxIterations, 
    __global uint *colorMap,
    int colorMapSize,
    int aggregationFlag
    ){



    unsigned int ix = get_global_id(0);
    unsigned int iy = get_global_id(1);

	if(aggregationFlag > 0){
		
	   double r = (centerX - ((width  / 2) * pixelScale)) + (ix * pixelScale*aggregationSize);
	   double i = (centerY - ((height / 2) * pixelScale)) + (iy * pixelScale*aggregationSize);
	   
	   double rNext = r;
	   double iNext = i;
	   
	int upperLeft = getIterations(maxIterations,r,i);
	int variance = 0;
	int count = 0;
	int step = 1;
	//top left to rop right
	while(variance == 0 && count < aggregationSize){
		rNext = rNext+pixelScale;
		if(!(getIterations(maxIterations,rNext,iNext)== upperLeft)){
			variance+= 1;
		}
		count+= step;
			
			int aggregationIndex = iy*(width/aggregationSize)+ix+count;
	 		int aggregationValue = aggregationGrid[aggregationIndex]; 
	 		output[(iy*width*aggregationSize)+(ix*aggregationSize)] = colorMap[aggregationValue];	
	}
	
	//top right to bottom right
	count = 0;
	while(variance == 0 && count < aggregationSize){
		iNext = iNext+pixelScale;
		if(!(getIterations(maxIterations,rNext,iNext)== upperLeft)){
			variance+= 1;
		}
		count+= step;
	}
	
	//bottom right to bottom left;
	count = 0;
	while(variance == 0 && count < aggregationSize){
		rNext = rNext-pixelScale;
		if(!(getIterations(maxIterations,rNext,iNext)== upperLeft)){
			variance+= 1;
		}
		count+= step;
	}
	
	//bottom left to top left
	count = 0;
	while(variance == 0 && count < aggregationSize){
		iNext = iNext-pixelScale;
		if(!(getIterations(maxIterations,rNext,iNext)== upperLeft)){
			variance+= 1;
		}
		count+= step;
	}
	
	
	
	
	
	   if(variance == 0){
			aggregationGrid[iy*(width/aggregationSize)+ix] = upperLeft;
		}else{
			aggregationGrid[iy*(width/aggregationSize)+ix] = 0;
		}
	//	


	
	
	}else{
	   	double r = (centerX - ((width  / 2) * pixelScale)) + (ix * pixelScale);
	   	double i = (centerY - ((height / 2) * pixelScale)) + (iy * pixelScale);
    	int aggregationIndex = (iy/aggregationSize)*(width/aggregationSize)+ix/aggregationSize;
		int aggregationValue = aggregationGrid[aggregationIndex];
		
	 		// && ( ((ix/aggregationSize)*aggregationSize) == ix || ((iy/aggregationSize)*aggregationSize) == iy)
	 	int xDelta = ix+ ((ix/aggregationSize)*aggregationSize);
	 	int yDelta = iy+ ((iy/aggregationSize)*aggregationSize);

	 		
			if(aggregationValue > 0 && (xDelta >0 || yDelta >0)){
			//	output[iy*width+ix] = colorMap[aggregationValue];
				output[iy*width+ix] = 0xFFFFFF;
			}else{
				output[iy*width+ix] = colorMap[getIterations(maxIterations,r,i)];
			}

	}
}


