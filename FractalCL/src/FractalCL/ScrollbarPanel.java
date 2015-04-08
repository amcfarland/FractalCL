package FractalCL;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

public class ScrollbarPanel extends JPanel implements AdjustmentListener{
	JScrollBar scrollBar;
	 JLabel  label;
	
	
	public ScrollbarPanel(int min, int max, int value,String tooltip) {
		FlowLayout flowLayout = (FlowLayout) getLayout();
		flowLayout.setHgap(1);
		flowLayout.setVgap(2);
		flowLayout.setAlignment(FlowLayout.LEFT);
		setPreferredSize(new Dimension(200, 26));
		setToolTipText(tooltip);
		
		scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
		scrollBar.setMaximum(max);
		scrollBar.setMinimum(min);
		scrollBar.setValue(value);
		scrollBar.setPreferredSize(new Dimension(130, 24));
		scrollBar.addAdjustmentListener(this);
		scrollBar.setToolTipText(tooltip);
		add(scrollBar);
		
		label = new JLabel(""+value);
		label.setToolTipText(tooltip);
		add(label);
	}


	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		label.setText(((JScrollBar)e.getSource()).getValue()+"");
	}

}
