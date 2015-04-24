package FractalCL;
/*
Copyright (c) 2010-2011, Advanced Micro Devices, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following
disclaimer. 

Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
disclaimer in the documentation and/or other materials provided with the distribution. 

Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
derived from this software without specific prior written permission. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

If you use the software (in whole or in part), you shall adhere to all applicable U.S., European, and other export
laws, including but not limited to the U.S. Export Administration Regulations ("EAR"), (15 C.F.R. Sections 730 through
774), and E.U. Council Regulation (EC) No 1334/2000 of 22 June 2000.  Further, pursuant to Section 740.6 of the EAR,
you hereby certify that, except pursuant to a license granted by the United States Department of Commerce Bureau of 
Industry and Security or as otherwise permitted pursuant to a License Exception under the U.S. Export Administration 
Regulations ("EAR"), you will not (1) export, re-export or release to a national of a country in Country Groups D:1,
E:1 or E:2 any restricted technology, software, or source code you receive hereunder, or (2) export to Country Groups
D:1, E:1 or E:2 the direct product of such technology or software, if such foreign produced direct product is subject
to national security controls as identified on the Commerce Control List (currently found in Supplement 1 to Part 774
of EAR).  For the most current Country Group listings, or for additional information about the EAR or your obligations
under those regulations, please refer to the U.S. Bureau of Industry and Security's website at http://www.bis.doc.gov/. 

 */
//

/*import static org.jocl.CL.CL_CONTEXT_PLATFORM;
import static org.jocl.CL.CL_DEVICE_TYPE_ALL;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_MEM_WRITE_ONLY;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clCreateCommandQueue;
import static org.jocl.CL.clCreateContext;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clEnqueueWriteBuffer;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clSetKernelArg;*/

import static org.jocl.CL.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.util.Date;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jocl.*;
import org.jocl.samples.JOCLEventSample.ExecutionStatistics;


/**
 * @author AF7FC
 *
 */
public class FractalCL extends JFrame implements MouseListener,MouseMotionListener, KeyListener,
		WindowListener, ComponentListener, MouseWheelListener,AdjustmentListener {

	/** User selected zoom-in point on the Mandelbrot view. */
	public static volatile Point to = null;
	GraphicsEnvironment gEnvironment;
	GraphicsDevice gDevice; // second Monitor
	GraphicsConfiguration gConfiguration;
	DisplayMode dMode;
	JComponent viewer;
	Rectangle oldBounds, newBounds;
	JSplitPane splitPane;
	ControlPanel controlPanel;
    public static JPanel drawPanel;
    
	static int frameWidth = 800;
	static int  frameHeight = 600;
	static int width,height;
	static int aggregationSize = 25;
	static int centerPixelX,centerPixelY, testGridResolution,testGridWidth;
	static double centerX = -1f;
	static double  centerY = 0f;
	static int mouseX,mouseY,mouseDownX,mouseDownY;
	static BufferedImage image;
	static int[] rgb;
	int globalMaxIterations = 10000;
	int maxIterations = 1000;
	static double pixelScale;
	static double  zoomSpeed = .1f;
	static int hDiv = 1;
	static int vDiv = 1;
	long clickTimer;
	int aggregationFlag = 1;
	
    /** 
     * The OpenCL context
     */
    private cl_context context;

    /**
     * The OpenCL command queue
     */
    private cl_command_queue commandQueue;

    /**
     * The OpenCL kernel which will actually compute the Mandelbrot
     * set and store the pixel data in a CL memory object
     */
    private cl_kernel kernel;

    /**
     * The OpenCL memory object which stores the pixel data
     */
    private cl_mem pixelMem;

    /**
     * An OpenCL memory object which stores a nifty color map,
     * encoded as integers combining the RGB components of
     * the colors.
     */
    private cl_mem colorMapMem;

    /**
     * The color map which will be copied to OpenCL for filling
     * the PBO. 
     */
    private int colorMap[];
  
    
    private cl_mem aggregationGrid;
	
    private  cl_device_id device;
    
	private cl_program cpProgram;
	
	private cl_platform_id platforms[];
	
	private cl_platform_id  platform;
	
	/**
	 * 
	 */
	public FractalCL() {
		super("Mandelbrot");
		
		drawPanel = new JPanel();
		drawPanel.setPreferredSize(new Dimension(3999, 2000));
		drawPanel.setVisible(true);
	
		controlPanel = new ControlPanel(this);
		controlPanel.setBackground(Color.yellow);
		controlPanel.setVisible(true);
		maxIterations = controlPanel.getIterationsPanel().getValue();
		
		
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,controlPanel,drawPanel);
		splitPane.setDividerLocation(200);
		splitPane.setDividerSize(10);
		splitPane.setEnabled(false);
		getContentPane().add(splitPane, BorderLayout.CENTER);
	


		
		addKeyListener(this);
		addWindowListener(this);
		drawPanel.addMouseListener(this);
		drawPanel.addComponentListener(this);
		drawPanel.addMouseMotionListener(this);
		drawPanel.addMouseListener(this);
		drawPanel.addMouseWheelListener(this);
		drawPanel.addMouseMotionListener(this);

		
		
		
		
		gEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gDevice = gEnvironment.getScreenDevices()[0]; // second Monitor
		gConfiguration = getContentPane().getGraphicsConfiguration();
		// gDevice.setFullScreenWindow(this);
		// dMode = gDevice.getDisplayMode();
		// width = dMode.getWidth();
		// height = dMode.getHeight();
		//this.setIgnoreRepaint(true);
		//this.setUndecorated(true);


		initialize();
	}


	// setup image and OCL kernal based on current window size
	public void initialize() {
		System.out.println("initialize()");
		if (this.isVisible() == false) {
			initializeCL();
			setBounds(1940,0,frameWidth,frameHeight);
			setVisible(true);
			

			
			
		    width = drawPanel.getWidth();
		    height = drawPanel.getHeight();
		    image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		    rgb = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			newBounds = getBounds();
			pixelScale = 2.0f/width;
			centerX =-0.75f;
			centerY = 0f;
	        // Create and fill the memory object containing the color map
	        colorMap = new int[globalMaxIterations];
	        float step = 5f/(float)globalMaxIterations;
	        for(int f= 0; f< globalMaxIterations; f++){
	        	Color temp = Color.getHSBColor(1+f*step, .7f, .8f);
	        	colorMap[f] = temp.getRGB();
	        	
	        }

		}

		
			if(pixelMem != null)
				clReleaseMemObject(pixelMem);
			

		
		    width = drawPanel.getWidth();
		    height = drawPanel.getHeight();
		    image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		    rgb = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

	        // Create the memory object which will be filled with the pixel data
	        pixelMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY,width * height * Sizeof.cl_int, null, null);
	        aggregationGrid = clCreateBuffer(context, CL_MEM_READ_WRITE,width/aggregationSize * height/aggregationSize * Sizeof.cl_int, null, null);
		
			
			centerPixelX = width/2;
			centerPixelY = height/2;
			// save current values
			

	        colorMapMem = clCreateBuffer(context, CL_MEM_READ_WRITE,colorMap.length * Sizeof.cl_int, null, null);
	        
	        
	        render(1f, centerX, centerY);
	        
		
		
		

		// Report target execution mode: GPU or JTP (Java Thread Pool).
		//System.out.println("Execution mode=" + kernel.getExecutionMode());
		

	}

	
	
	
/**
 * One time setup of OpenCL context
 */
	private void initializeCL(){
		
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 1;// should be R9 Hawaii

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        platforms = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        
        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];
        
        // Obtain a device ID 
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        device = devices[deviceIndex];

       
        // Create a context for the selected device
        context = clCreateContext(contextProperties, 1, new cl_device_id[]{device},null, null, null);
        

        // Create a command-queue, with profiling info enabled
        long properties = 0;
        properties |= CL.CL_QUEUE_PROFILING_ENABLE;
        commandQueue = CL.clCreateCommandQueue(context, device, properties, null);
        
        
        
        // Display device info
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetDeviceInfo(device,CL_DEVICE_NAME , 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetDeviceInfo(device, CL_DEVICE_NAME, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        System.out.println("Using device: "+new String(buffer, 0, buffer.length-1));
        

        
        
        
        // Program Setup
        String source = readFile("kernels/FractalCL.cl");

        // Create the program
         cpProgram = clCreateProgramWithSource(context, 1,new String[]{ source }, null, null);

        // Build the program
        clBuildProgram(cpProgram, 0, null, "-cl-mad-enable", null, null);

        // Create the kernel
        kernel = clCreateKernel(cpProgram, "computeMandelbrot", null);

        
        ///
        

      
        
        
	}
	
	
	
	
	public void paint(Graphics g) {
		super.paint(g);
         drawPanel.getGraphics().drawImage(image, 0, 0, this);
	}

	
	
/**
 * Render.
 *
 * @param zoomFactor the zoom factor
 * @param centerX the center x
 * @param centerY the center y
 * @return the long
 */
	private void  render(double zoomFactor, double centerX, double centerY) {
		//System.out.println("RENDER "+zoomFactor+"\t"+centerX+"\t"+centerY);
		
		
		//stop zooming in at limit of double precision
      if(zoomFactor > 1 ||  pixelScale > 0.0000000000000001){
    	  pixelScale *= zoomFactor; 
      }
		 
 
      
		// Set work size and execute the kernel
        width = drawPanel.getWidth();
        height = drawPanel.getHeight();

       
        

        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(pixelMem));
        clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(new int[]{width}));
        clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(new int[]{height}));
        clSetKernelArg(kernel, 3, Sizeof.cl_double, Pointer.to(new double[]{ centerX}));
        clSetKernelArg(kernel, 4, Sizeof.cl_double, Pointer.to(new double[]{ centerY}));
        clSetKernelArg(kernel, 5, Sizeof.cl_int, Pointer.to(new int[]{ aggregationSize}));
        clSetKernelArg(kernel, 6, Sizeof.cl_mem, Pointer.to( aggregationGrid));
        clSetKernelArg(kernel, 7, Sizeof.cl_double, Pointer.to(new double[]{ pixelScale}));
        clSetKernelArg(kernel, 8, Sizeof.cl_int, Pointer.to(new int[]{ maxIterations }));
        clSetKernelArg(kernel, 9, Sizeof.cl_mem, Pointer.to(colorMapMem));
        clSetKernelArg(kernel, 10, Sizeof.cl_int, Pointer.to(new int[]{ colorMap.length }));
        clSetKernelArg(kernel, 11, Sizeof.cl_int, Pointer.to(new int[]{ 1 }));
        clEnqueueWriteBuffer(commandQueue, colorMapMem, true, 0,colorMap.length * Sizeof.cl_uint, Pointer.to(colorMap), 0, null, null);
      
        long globalWorkSize[] = new long[]{width/aggregationSize,height/aggregationSize};
       
        //aggregationPass
        cl_event aggregationEvent = new cl_event();
        clEnqueueNDRangeKernel(commandQueue, kernel, 2, null,globalWorkSize, null, 0, null, aggregationEvent);
        CL.clWaitForEvents(1, new cl_event[]{aggregationEvent});

  
        //render pass
        cl_event renderEvent = new cl_event();
        globalWorkSize = new long[]{width,height};

        clSetKernelArg(kernel, 11, Sizeof.cl_int, Pointer.to(new int[]{ 0 }));
        clEnqueueNDRangeKernel(commandQueue, kernel, 2, null,globalWorkSize, null, 0, null, renderEvent);
        CL.clWaitForEvents(1, new cl_event[]{renderEvent});
       
        
        // read results
        cl_event resultsEvent = new cl_event();
        clEnqueueReadBuffer(commandQueue, pixelMem, CL_TRUE, 0,Sizeof.cl_uint * width*height, Pointer.to(rgb), 0, null, resultsEvent);
        CL.clWaitForEvents(1, new cl_event[]{resultsEvent});

       
        float aggregationMillis = getEventDurationMillis(aggregationEvent);
        float renderMillis = getEventDurationMillis(renderEvent);
        float resultsMillis = getEventDurationMillis(resultsEvent);
        float totalMillis = aggregationMillis+renderMillis+resultsMillis;
        
        controlPanel.getTable().setValueAt(String.format("%1.16f", pixelScale), 3, 1);
        controlPanel.getTable().setValueAt(String.format("%8.1f",aggregationMillis ), 4, 1);
        controlPanel.getTable().setValueAt(String.format("%8.1f", renderMillis), 5, 1);
        controlPanel.getTable().setValueAt(String.format("%8.1f", resultsMillis), 6, 1);
        controlPanel.getTable().setValueAt(String.format("%8.1f", totalMillis), 7, 1);
        
       // controlPanel.getTable().paint(controlPanel.getTable().getGraphics());
      
        
        double milliDouble = (double) totalMillis;
        int milliInt = (int)milliDouble;
        
        int v =(int) (200-milliInt+Math.sqrt(milliDouble));
        v = Math.max(v,5);
        int red = Math.max(0,255-v);
        int green = Math.min(255, v);
        Color c = new Color(red,green, 0);
        controlPanel.getRenderBar().setValue(v);
        controlPanel.getRenderBar().setForeground(c);
      
        drawPanel.getGraphics().drawImage(image, 0, 0, this);

	}

	void disposeCL(){
		
		CL.clUnloadPlatformCompiler(platform);
		CL.clReleaseMemObject(pixelMem);
		CL.clReleaseMemObject(colorMapMem);
		CL.clReleaseMemObject(pixelMem);
		CL.clReleaseCommandQueue(commandQueue);
		//CL.clReleaseEvent(kernelEvent0);
		//CL.clReleaseEvent(kernelEvent1);
		CL.clReleaseContext(context);
		CL.clReleaseDevice(device);
		CL.clReleaseProgram(cpProgram);
	}
	
	
	float getEventDurationMillis(cl_event e){
		float duration;
        long[] start= new long[1];;
        long[] end = new long[1];
                CL.clGetEventProfilingInfo(e , CL.CL_PROFILING_COMMAND_START,Sizeof.cl_ulong, Pointer.to(start), null);
                CL.clGetEventProfilingInfo(e, CL.CL_PROFILING_COMMAND_END,Sizeof.cl_ulong, Pointer.to(end), null);
        duration = (end[0]-start[0])/1000000f;
		return duration;
	}
	
	private void recenter(Point p){
		
		int steps = 10;
		double xDelta = -1*(centerPixelX-p.x)*pixelScale/steps;
		double yDelta = -1*(centerPixelY-p.y)*pixelScale/steps;
		for(int i = 0; i <steps; i++ ){
			centerX = centerX+xDelta;
			centerY = centerY+yDelta;
			render(1f, centerX, centerY);

		}
		

	}
	
	
	void showPixelInfo(int h, int v){
	    int ix = h;
	    int iy = v;

	   double r = (centerX - ((width  / 2) * pixelScale)) + (ix * pixelScale);
	   double i = (centerY - ((height / 2) * pixelScale)) + (iy * pixelScale);
	   
	   
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
		String real = String.format("%8.10f", r);
		String imaginary = String.format("%8.10f", i);
	    controlPanel.getTable().getModel().setValueAt(iteration, 0, 1);
	    controlPanel.getTable().getModel().setValueAt(real, 1, 1);
	    controlPanel.getTable().getModel().setValueAt(imaginary, 2, 1);
	    
	}
	
	
	
    /**
     * [from JOCL example code]
     * Helper function which reads the file with the given name and returns 
     * the contents of this file as a String. Will exit the application
     * if the file can not be read.
     * 
     * @param fileName The name of the file to read.
     * @return The contents of the file
     */
    private String readFile(String fileName)
    {
    	
    	try
        {
            BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName)));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while (true)
            {
                line = br.readLine();
                if (line == null)
                {
                    break;
                }
                sb.append(line).append("\n");
                System.out.println(line);
            }
            System.out.println("*** "+fileName);
            return sb.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
   
    }
    

	// ******************************************************************************************
	// *** LISTENERS ***
	// ***********************************************************************

	//
	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {

		
			int amount = Math.abs(arg0.getWheelRotation());
			int step = arg0.getWheelRotation() / Math.abs(amount); // 1 or -1
			render((float)(1+ (step * zoomSpeed)), centerX, centerY);
		     showPixelInfo(arg0.getX(), arg0.getY());

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {	
		centerX  = centerX+ ((mouseX - arg0.getX()) * pixelScale);
		centerY = centerY+ ((mouseY - arg0.getY()) * pixelScale);
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		initialize();
		//render(1f, centerX, centerY);


	}
	@Override
	public void mousePressed(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		clickTimer = new Date().getTime();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		
		
		if(new Date().getTime()-clickTimer < 200){		
			recenter(e.getPoint());
		}

	}

	
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}



	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			CL.clUnloadCompiler();
			System.exit(0);
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		disposeCL();
		System.exit(0);

	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void windowOpened(WindowEvent arg0) {

	}

	@Override
	public void componentShown(ComponentEvent arg0) {

	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		
		
		
/*		int xDelta = Math.abs(newBounds.width-this.getWidth());
		int yDelta = Math.abs(newBounds.height- this.getHeight());
		System.out.println(xDelta+" "+yDelta);
		if( xDelta >testGridResolution || yDelta > testGridResolution){
			
			this.setBounds(newBounds.x,newBounds.y,getBounds().width-getBounds().width%testGridResolution, getBounds().height-getBounds().height%testGridResolution);
			newBounds = getBounds();
			
		}*/
		initialize();

	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					FractalCL main = new FractalCL();
					main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}



	@Override
	public void mouseMoved(MouseEvent arg0) {
	
		showPixelInfo(arg0.getX(),arg0.getY());
		//System.out.println(arg0.getX()+"\t"+arg0.getY()+"\t"+iterations);
	}






	public void adjustmentValueChanged(AdjustmentEvent arg0) {
		//System.out.println("Adjustment says "+arg0.getValue());
		Object source = arg0.getSource();
		if( source.equals(controlPanel.getIterationsPanel().scrollBar)){
			maxIterations = arg0.getValue();
			initialize();
		}else if( source.equals(controlPanel.getAggregationPanel().scrollBar)){
			aggregationSize = arg0.getValue();
			initialize();
			
		}
		
		
	}

}
