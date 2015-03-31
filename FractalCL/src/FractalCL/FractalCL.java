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

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.util.Date;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.apfloat.Apfloat;

import FractalCL.kernel.MandelKernel;

import com.amd.aparapi.Device;
import com.amd.aparapi.Range;

/**
 * @author AF7FC
 *
 */
public class FractalCL extends JFrame implements MouseListener,MouseMotionListener, KeyListener,
		WindowListener, ComponentListener, MouseWheelListener {

	/** User selected zoom-in point on the Mandelbrot view. */
	public static volatile Point to = null;
	GraphicsEnvironment gEnvironment;
	GraphicsDevice gDevice; // second Monitor
	GraphicsConfiguration gConfiguration;
	DisplayMode dMode;
	JComponent viewer;
	Rectangle oldBounds, newBounds;
	JSplitPane splitPane;

	static int width = 775;
	static int  height = 775;
	static int centerPixelX,centerPixelY, testGridResolution,testGridWidth;
	static double centerX = -1f;
	static double centerY = 0f;
	static int mouseX,mouseY,mouseDownX,mouseDownY;
	static Range range,rangeG;
	static BufferedImage image;
	static int[] rgb, testGrid;
	static MandelKernel kernel;
	static double pixelScale;
	static double  zoomSpeed = .05f;
	static int hDiv = 1;
	static int vDiv = 1;
	static Range[][] ranges = new Range[vDiv][hDiv];
	long clickTimer;
	static Device clDevice;
	/**
	 * 
	 */
	public FractalCL() {
		super("Mandelbrot");
		initialize();
		addKeyListener(this);
		addWindowListener(this);
		getRootPane().addMouseListener(this);
		getRootPane().addComponentListener(this);
		getContentPane().addMouseMotionListener(this);
		getContentPane().addMouseListener(this);
		addMouseWheelListener(this);
		gEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gDevice = gEnvironment.getScreenDevices()[0]; // second Monitor
		gConfiguration = getContentPane().getGraphicsConfiguration();
		// gDevice.setFullScreenWindow(this);
		// dMode = gDevice.getDisplayMode();
		// width = dMode.getWidth();
		// height = dMode.getHeight();
		//this.setIgnoreRepaint(true);
		//this.setUndecorated(true);
	}

	@Override
	public void paint(Graphics g) {
		// getContentPane().setBackground(Color.green);
		g.drawImage(image, 0, 0, width, height, this);
	}

	// setup image and OCL kernal based on current window size
	public void initialize() {
		System.out.println("initialize()");
	
		if (this.isVisible() == false) {
			//this.setBounds(1930, 0, 3840, 2160);
			this.setBounds(1940,0,width,height);
			newBounds = getBounds();
	//		this.setUndecorated(true);
			
			this.setVisible(true);
		}
		
		//splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		//getContentPane().add(comp)(splitPane);
		width = this.getContentPane().getWidth()+testGridResolution;
	    height = this.getContentPane().getHeight()+testGridResolution;
	    
	    testGridResolution = 25;
		width = width-width%testGridResolution;
		height = height-height%testGridResolution;
	    
		//width = w-w%16;
		//height = h-h%16;
		System.out.println("WxH "+width+"\t"+height);
		clDevice = Device.best();
		System.out.println(clDevice.toString());
		System.out.println(Device.firstGPU().toString());
		System.out.println(clDevice.toString());
		//range = clDevice.createRange2D(width/hDiv-2, height/vDiv-2);
		//range = clDevice.createRange2D(256, 256);
		//range = clDevice.createRange2D(2992, 1992);
		
		
		range = clDevice.createRange2D(width, height);
/*		for(int i = 20; i < 30; i++){
			System.out.println("trying resolution "+i);
			if(width%i==0){
				testGridResolution = i;
				System.out.println("Resolution = "+i);
				break;
			}
		}*/
		
		
		
		testGridWidth = width/testGridResolution;
		rangeG = clDevice.createRange2D(width/testGridResolution, height/testGridResolution);
		testGrid = new int[(width/testGridResolution)*(height/testGridResolution)];
		System.out.println("W: "+width+" H: "+height);
		//System.out.println("Range WxH "+range.getGlobalSize(0)+"\t"+range.getGlobalSize(1));
		//System.out.println(range.toString());

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		rgb = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		System.out.println(rgb.toString());
				//
	//	System.out.println(wr.toString());
		//rgb = ((DataBufferInt)wr.get).getData();
		System.out.println("rgb "+rgb.length);
		pixelScale = 2.0/width;
		centerX =-0.75;
		centerY = 0f;
		centerPixelX = width/2;
		centerPixelY = height/2;
		// save current values
	
		if (kernel != null) {
			pixelScale = kernel.getPixelScale();
			centerX = kernel.getCenterX();
			centerY = kernel.getCenterY();
			kernel.dispose();
		}
		kernel = new MandelKernel(width, height,testGrid, rgb);

		kernel.setExplicit(true);
		kernel.setPixelScale(pixelScale);

		
		render(1f, centerX, centerY);
		

		// new kernel to match window size

		

		
		// Report target execution mode: GPU or JTP (Java Thread Pool).
		System.out.println("Execution mode=" + kernel.getExecutionMode());
		
/*		Mixer.Info[] mixers = AudioSystem.getMixerInfo();
		for(int i = 0; i < mixers.length; i++){
			System.out.println(mixers[i].toString());
		}
		*/
	}


/**
 * Render.
 *
 * @param zoomFactor the zoom factor
 * @param centerX the center x
 * @param centerY the center y
 * @return the long
 */
	private long render(double zoomFactor, double centerX, double centerY) {
		//System.out.println("RENDER "+zoomFactor+"\t"+centerX+"\t"+centerY);
		kernel.setTestGrid(new int[(width/testGridResolution)*(height/testGridResolution)]);
		long elapsedStart = System.currentTimeMillis();
		double newScale = kernel.getPixelScale() * zoomFactor;
		kernel.setPixelScale(newScale);
		kernel.setCenter(centerX, centerY);

		kernel.setTestGridResolution(testGridResolution);
		kernel.setTestPass(true);
		kernel.execute(rangeG);
		
		kernel.setTestPass(false);
		kernel.execute(range);


			
		this.paint(this.getContentPane().getGraphics());
		return System.currentTimeMillis() - elapsedStart;
	}

	
	private void recenter(Point p){
		
		int steps = 10;
		double xDelta = -1*(centerPixelX-p.x)*kernel.getPixelScale()/steps;
		double yDelta = -1*(centerPixelY-p.y)*kernel.getPixelScale()/steps;
		for(int i = 0; i <steps; i++ ){
			centerX = kernel.getCenterX()+xDelta;
			centerY = kernel.getCenterY()+yDelta;
			System.out.println("Recenter: "+render(1f, centerX, centerY));

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

		// float newCenterX = (width/2-arg0.getX())*kernel.getPixelScale();
		// float newCenterY = (height/2-arg0.getY())*kernel.getPixelScale();

		for (int i = 0; i < amount; i += 1) {
			// System.out.println("Elapsed milliseconds = "+render(1+(step*zoomSpeed),(centerX-newCenterX)/4,(centerY-newCenterY)/4));
			render((float)(1+ (step * zoomSpeed)), centerX, centerY);
			//System.out.println("" + step);
		}

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {

		
		centerX  = kernel.getCenterX()+ ((mouseX - arg0.getX()) * kernel.getPixelScale());
		centerY = kernel.getCenterY()+ ((mouseY - arg0.getY()) * kernel.getPixelScale());

		mouseX = arg0.getX();
		mouseY = arg0.getY();

		long elapsed = render(1f, centerX, centerY);
		System.out.print(" "+elapsed);
	//	System.out.println("center "+centerX+"\t"+centerY);
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
			kernel.dispose();
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
		kernel.dispose();
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
		System.out.println(arg0.toString());
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		
		
		
		int xDelta = Math.abs(newBounds.width-this.getWidth());
		int yDelta = Math.abs(newBounds.height- this.getHeight());
		System.out.println(xDelta+" "+yDelta);
		if( xDelta >testGridResolution || yDelta > testGridResolution){
			
			this.setBounds(newBounds.x,newBounds.y,getBounds().width-getBounds().width%testGridResolution, getBounds().height-getBounds().height%testGridResolution);
			newBounds = getBounds();
			
		}
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

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}



	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
