package FractalCL;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeListener;

import java.awt.Font;
import java.awt.Component;
import java.awt.event.AdjustmentListener;

public class ControlPanel extends JPanel {
	
    private JTable table;
	private ScrollbarPanel iterationsPanel;
	private ScrollbarPanel aggregationPanel;
	
	
	public ControlPanel(AdjustmentListener listener){
		super();
		setMaximumSize(new Dimension(200, 32767));
		setMinimumSize(new Dimension(200, 500));
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setPreferredSize(new Dimension(200, 800));
		setBorder(null);
		setBackground(Color.yellow);
		ToolTipManager.sharedInstance().setInitialDelay(400);
		
		setTable(new JTable());
		getTable().setShowGrid(false);
		getTable().setModel(new DefaultTableModel(
			new Object[][] {
				{"Iterations", null},
				{"Real", null},
				{"Imaginary", null},
				{"Render ms", null},

			},new String[] {"New column", "New column"}
		) {
			boolean[] columnEditables = new boolean[] {
				true, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		getTable().getColumnModel().getColumn(1).setPreferredWidth(117);
		getTable().setColumnSelectionAllowed(true);
		getTable().setCellSelectionEnabled(true);
		getTable().setBackground(Color.YELLOW);
		add(getTable());
		
		setIterationsPanel(new ScrollbarPanel(1,5010,1000, "Max Iterations"));
		getIterationsPanel().scrollBar.addAdjustmentListener(listener);
		add(getIterationsPanel());
		aggregationPanel = new ScrollbarPanel(0,100,25," Aggregation Box Size");
		add(aggregationPanel);
		
		
	}


	public JTable getTable() {
		return table;
	}


	public void setTable(JTable table) {
		this.table = table;
	}


	public ScrollbarPanel getIterationsPanel() {
		return iterationsPanel;
	}


	public void setIterationsPanel(ScrollbarPanel iterationsPanel) {
		this.iterationsPanel = iterationsPanel;
	}

}
