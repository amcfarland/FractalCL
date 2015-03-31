package FractalCL.kernel;
/*
 * Your file header goes here...
 * 
 */
import java.awt.Color;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Kernel.Constant;

   // TODO: Auto-generated Javadoc
/**
    * The Class MandelKernel.
    */
   public  class MandelKernel extends Kernel{

      /** The rgb. */
      final private int rgb[];
      
      public int testGrid[];

      /** The width. */
      private int width;

      /** The height. */
      private int height;

      private boolean testPass =  true;

     // private int xPixelOffset;
     // private int yPixelOffset;
      /** The max iterations. */
       private int maxIterations = 1250;

      /** The pallette. */
      @Constant  private int pallette[] = new int[maxIterations + 1];

      /** The pixel scale. */
      private double pixelScale ;
      
      /** The center x. */
      private double centerX = -1.0;
      
      /** The center y. */
      private double centerY = .0;

      private int testGridResolution = -1;
      
      

      /**
       * Instantiates a new mandel kernel.
       *
       * @param _width the _width
       * @param _height the _height
       * @param _rgb the _rgb
       */
     // public MandelKernel(int _width, int _height, int xOffset, int yOffset,int[] testGrid, int[] _rgb) {
      public MandelKernel(int _width, int _height, int[] testGrid, int[] _rgb) {
    	  //Initialize palette values
         for (int i = 0; i < maxIterations; i++) {
            float h = i / (float) maxIterations;
            float b = 1.0f - h/2;
          // float b = (float) Math.pow((double)h, 2.0);
            pallette[i] = Color.HSBtoRGB(h, 1f, b);

         }

         width = _width;
         height = _height;
        // xPixelOffset = xOffset;
        // yPixelOffset = yOffset;
         rgb = _rgb;
         this.testGrid = testGrid;

      }

      
      
      /**
       * Gets the count.
       *
       * @param x the x
       * @param y the y
       * @return the count
       */
      public int getCount(double x, double y){
        int count =0;
    	 double zx = x;
         double zy = y;
         double new_zx = 0f;

         // Iterate until the algorithm converges or until maxIterations are reached.
         while (count < maxIterations && zx * zx + zy * zy < 8) {
            new_zx = zx * zx - zy * zy + x;
            zy = 2 * zx * zy + y;
            zx = new_zx;
           count++;
         }
         return count ;
      }

      /* (non-Javadoc)
       * @see com.amd.aparapi.Kernel#run()
       */
      @Override public void run() {
 
		    
	    	// int gidTest;
	    	 double x,y,x2,y2,x3,y3;
	    	 int count = -1;
	    	 int value;
	    	 int delta;
	    	 int toleranceP = 1;
	    	 int toleranceN = -1*toleranceP;
		  if(testPass){
		      
		      int  gidTest = (getGlobalId(1)*(width/testGridResolution)+getGlobalId(0));
		      int gidDisplay = getGlobalId(1)*testGridResolution*width+getGlobalId(0)*testGridResolution;
		           
		 
		      testGrid[gidTest] = -1;
		      x = centerX-((width /2)*pixelScale)+(getGlobalId(0)*testGridResolution*pixelScale);
	           y = centerY-((height/2)*pixelScale)+(getGlobalId(1)*testGridResolution*pixelScale);
	           
	           x2 = x+testGridResolution*pixelScale;
	           y2 = y+testGridResolution*pixelScale;
	          
	          
	   	    value = getCount(x,y);
	   	    boolean abort = false;
	   	 	//top edge
	   	      for(int i =0; i < testGridResolution; i+=1){
	        	count = getCount(x+i*pixelScale,y);
	        	delta = value-count;
	        	if( delta < toleranceN || delta > toleranceP)  {
	        		abort = true;
	        		i = testGridResolution;
	        		}
	         }
	         
	   	      //right edge
	   	      for(int i =0; i < testGridResolution; i+=1){
	        	count = getCount(x+testGridResolution*pixelScale,y+i*pixelScale);
	        	delta = value-count;
	        	if( delta < toleranceN || delta > toleranceP)  {
	        		abort = true;
	        		i = testGridResolution;
	        		}
	         }
	   	     
	   	      for(int i =0; i < testGridResolution; i+=1){
	        	count = getCount(x+i*pixelScale,y+testGridResolution*pixelScale);
	        	delta = value-count;
	        	if( delta < toleranceN || delta > toleranceP)  {
	        		abort = true;
	        		i = testGridResolution;
	        		}
	         }
	   	     
	   	      for(int i =0; i < testGridResolution; i+=1){
	        	count = getCount(x,y+i*pixelScale);
	        	delta = value-count;
	        	if( delta < toleranceN || delta > toleranceP)  {
	        		abort = true;
	        		i = testGridResolution;
	        		}
	         }
	          if(!abort){
	        	   testGrid[gidTest] = value;
	        	   } 

	        	 
   	  }
     	
      if(!testPass){
    		  
  		  	int gidTest = (getGlobalId(1)/testGridResolution*(width/testGridResolution)+getGlobalId(0)/testGridResolution);
  		  int gidDisplay = getGlobalId(1)*width+getGlobalId(0);
			if(testGrid[gidTest] >-1){
				rgb[gidDisplay] = 37777;
				
				//rgb[gidDisplay] = pallette[testGrid[gidTest]];
				}else{
		  		  	x = centerX-((width /2)*pixelScale)+(getGlobalId(0)*pixelScale);
					y = centerY-((height/2)*pixelScale)+(getGlobalId(1)*pixelScale);
					rgb[gidDisplay] = pallette[getCount(x,y)];
				}
			
    	 } 

      }

      public void setTestGrid(int[] grid){
    	  testGrid = null;
    	  testGrid = grid;
      }
      
      
      public double getPixelScale(){
    	  return pixelScale;
      }
      public void setPixelScale(double pixelScale) { 
    	  this.pixelScale = pixelScale;
    	 
      }
      
      /**
       * Sets the center.
       *
       * @param centerX the center x
       * @param centerY the center y
       */
      public void setCenter(double centerX,double centerY){
    	  this.centerX = centerX;
    	  this.centerY = centerY;
    	 //System.out.println("offset "+centerX+" "+centerY);
      }
      public double getCenterX(){
    	  return centerX;
      }
      public double getCenterY(){
    	  return centerY;
      }
     void setSize(int width, int height){
    	  this.width = width;
    	  this.height = height;
      }
//     void setPixelOffsets(int xOffset,int yOffset){
//    	 xPixelOffset = xOffset;
//    	 yPixelOffset = yOffset;
//     }
    //  int getPixelOffsetX(){ return xPixelOffset;}
    //  int getPixelOffsetY(){ return yPixelOffset;}
     public void setTestGridResolution(int resolution){
    	 testGridResolution = resolution;
    	 System.out.println("resolution "+resolution);
     }
     public int  getTestGridSpacing(){ return testGridResolution;}
     public void setTestPass(boolean b){ testPass = b;}
     
   }