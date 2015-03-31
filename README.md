# FractalCL
A Mandelbrot set visualizer that uses OpenCL [ Aparapi ] including visual feedback and configurable parameters to evaluate optimizations.

This is a practice project to learn the basics of using OpenCL (from Java) by using AMD's Aparapi keep hand-coding to a minimum. It is primarily an excercise in implementing a well know task (MandelBrot) as an OpenCL kernel to understand the trade-offs involved. It specifically intends to be verbosely commented for the beginner. It also has a hands-on GUI and visual feedback/debug modes to help appreciate how things are getting processed (and just for fun.)

Future developments may include the addition of the Julia set and others, as well as adding an OpenGL display mode. Although it is not optimized at the OpenCL level, it is intended to be relatively fast, and at initial commit it  maintain 5-10 fps at 4k resolution near the limit of double floating point precision on a high end Radeon (R9 290)
