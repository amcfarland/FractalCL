package FractalCL;

import javax.swing.JPanel;

import java.awt.Dimension;

import javax.swing.JSlider;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SliderPanel extends JPanel implements ChangeListener{
	JSlider slider;
	 JLabel  label;
	
	
	public SliderPanel(int min, int max, int value,String tooltip) {
		FlowLayout flowLayout = (FlowLayout) getLayout();
		flowLayout.setHgap(1);
		flowLayout.setVgap(2);
		flowLayout.setAlignment(FlowLayout.LEFT);
		setPreferredSize(new Dimension(200, 26));
		setToolTipText(tooltip);
		
		slider = new JSlider();
		slider.setMaximum(max);
		slider.setMinimum(min);
		slider.setValue(value);
		slider.setPreferredSize(new Dimension(130, 24));
		slider.addChangeListener(this);
		slider.setToolTipText(tooltip);
		add(slider);
		
		label = new JLabel(""+value);
		label.setToolTipText(tooltip);
		add(label);
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		label.setText(((JSlider)e.getSource()).getValue()+"");
	}

}
