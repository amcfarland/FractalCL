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

import java.awt.Color;
import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JSplitPane;












import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;













import com.amd.aparapi.Device;
import com.amd.aparapi.Range;

import java.awt.BorderLayout;
import java.awt.Dimension;

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
	static int centerPixelX,centerPixelY, testGridResolution,testGridWidth;
	static double centerX = -1f;
	static double  centerY = 0f;
	static int mouseX,mouseY,mouseDownX,mouseDownY;
	static Range range,rangeG;
	static BufferedImage image;
	static int[] rgb, testGrid;
	int maxIterations = 5000;
	
	//static MandelKernel kernel;
	static double pixelScale;
	static double  zoomSpeed = .05f;
	static int hDiv = 1;
	static int vDiv = 1;
	static Range[][] ranges = new Range[vDiv][hDiv];
	long clickTimer;
	static Device clDevice;
	
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
    private cl_mem testMem;
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
		//System.out.println("initialize()");
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

		}else{
			if(pixelMem != null)
				clReleaseMemObject(pixelMem);
			

		
		    width = drawPanel.getWidth();
		    height = drawPanel.getHeight();
		    image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		    rgb = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			// resize to a multiple of testGridResolution
		    testGridResolution = 25;
		    //width = (width-width%testGridResolution)+testGridResolution;
			//height = (height-height%testGridResolution)+testGridResolution;
			


	        // Create the memory object which will be filled with the pixel data
	        pixelMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY,width * height * Sizeof.cl_int, null, null);
	        

		//	System.out.println("WxH "+width+"\t"+height);
			clDevice = Device.best();

			testGridWidth = width/testGridResolution;
			testGrid = new int[(width/testGridResolution)*(height/testGridResolution)];

		
			
			centerPixelX = width/2;
			centerPixelY = height/2;
			// save current values
			
	        // Create and fill the memory object containing the color map
	        colorMap = new int[maxIterations];
	        float step = 5f/(float)maxIterations;
	        for(int f= 0; f< maxIterations; f++){
	        	Color temp = Color.getHSBColor(1+f*step, .7f, .8f);
	        	colorMap[f] = temp.getRGB();
	        //	System.out.println(colorMap[(int)f]);
	        }
	        colorMapMem = clCreateBuffer(context, CL_MEM_READ_WRITE,colorMap.length * Sizeof.cl_int, null, null);
	        
	        
	        
	        
			render(1f, centerX, centerY);
		}
		


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
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

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
        cl_device_id device = devices[deviceIndex];

       
        // Create a context for the selected device
        context = clCreateContext(contextProperties, 1, new cl_device_id[]{device},null, null, null);
        
        // Create a command-queue for the selected device
        commandQueue =clCreateCommandQueue(context, device, 0, null);

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
        cl_program cpProgram = clCreateProgramWithSource(context, 1,new String[]{ source }, null, null);

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
		
		
		
      // if(true) return;
		pixelScale *= zoomFactor;
		// Set work size and execute the kernel
        width = drawPanel.getWidth();
        height = drawPanel.getHeight();
        long globalWorkSize[] = new long[2];
        globalWorkSize[0] = width;
        globalWorkSize[1] = height;


        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(pixelMem));
        clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(new int[]{width}));
        clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(new int[]{height}));
        clSetKernelArg(kernel, 3, Sizeof.cl_double, Pointer.to(new double[]{ centerX}));
        clSetKernelArg(kernel, 4, Sizeof.cl_double, Pointer.to(new double[]{ centerY}));
        clSetKernelArg(kernel, 5, Sizeof.cl_double, Pointer.to(new double[]{ pixelScale}));
        clSetKernelArg(kernel, 6, Sizeof.cl_int, Pointer.to(new int[]{ maxIterations }));
        clSetKernelArg(kernel, 7, Sizeof.cl_mem, Pointer.to(colorMapMem));
        clSetKernelArg(kernel, 8, Sizeof.cl_int, Pointer.to(new int[]{ colorMap.length }));

        clEnqueueWriteBuffer(commandQueue, colorMapMem, true, 0,colorMap.length * Sizeof.cl_uint, Pointer.to(colorMap), 0, null, null);
        clEnqueueNDRangeKernel(commandQueue, kernel, 2, null,globalWorkSize, null, 0, null, null);
        

        clEnqueueReadBuffer(commandQueue, pixelMem, CL_TRUE, 0,Sizeof.cl_uint * width*height, Pointer.to(rgb), 0, null, null);
       

        drawPanel.getGraphics().drawImage(image, 0, 0, this);
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
	
	
	void showPixelInfo(MouseEvent e){
	    int ix = e.getX();
	    int iy = e.getY();

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
		
	    controlPanel.getTable().getModel().setValueAt(iteration, 0, 1);
	    controlPanel.getTable().getModel().setValueAt(r, 1, 1);
	    controlPanel.getTable().getModel().setValueAt(i, 2, 1);
	    
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
//System.out.println(arg0.toString());
		int amount = Math.abs(arg0.getWheelRotation());
		int step = arg0.getWheelRotation() / Math.abs(amount); // 1 or -1

		// float newCenterX = (width/2-arg0.getX())*kernel.getPixelScale();
		// float newCenterY = (height/2-arg0.getY())*kernel.getPixelScale();

	//	for (int i = 0; i < amount; i += 1) {
			// System.out.println("Elapsed milliseconds = "+render(1+(step*zoomSpeed),(centerX-newCenterX)/4,(centerY-newCenterY)/4));
			render((float)(1+ (step * zoomSpeed)), centerX, centerY);
			//System.out.println("" + step);
		//}

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
		render(1f, centerX, centerY);
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
		CL.clUnloadCompiler();
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
		System.out.println("Initializing...");
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		System.out.println(arg0.toString());
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
	
		showPixelInfo(arg0);
		//System.out.println(arg0.getX()+"\t"+arg0.getY()+"\t"+iterations);
	}






	public void adjustmentValueChanged(AdjustmentEvent arg0) {
		//System.out.println("Adjustment says "+arg0.getValue());
		Object source = arg0.getSource();
		if( source.equals(controlPanel.getIterationsPanel().scrollBar)){
			maxIterations = arg0.getValue();
			render(1f,centerX,centerY);
		}
		
	}

}
