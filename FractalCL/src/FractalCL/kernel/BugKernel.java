package FractalCL.kernel;
/*
 * Your file header goes here...
 * 
 */
import java.awt.EventQueue;

import com.amd.aparapi.Kernel;
/* As is, this produces an IllegalStateException "child list broken"
 * removing almost any block produces and internal null pointer exception.  
 */
public class BugKernel extends Kernel {

	boolean testPass = true;

	@Override
	public void run() {
		System.out.println(this.toString());
/*		if (testPass) {
			int int1 = 1;
			if (dummyMethod() == 0) {
				int int3 = 3;
			}
		} else {
			int int2 = 2;
		}*/
	}

	int dummyMethod() {
		return 0;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("BUGKERNEL");
					BugKernel kernel = new BugKernel();
					kernel.execute(10);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}