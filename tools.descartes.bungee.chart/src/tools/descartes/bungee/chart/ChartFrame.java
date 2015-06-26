/*******************************************************************************
* Copyright (c) 2014 Andreas Weber, Nikolas Herbst
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/

package tools.descartes.bungee.chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class ChartFrame extends ApplicationFrame  {

	 private static final int STANDARD_HEIGHT = 400;
	private static final int STANDARD_WIDTH = 800;

	private static final long serialVersionUID = -189343389387199692L;


    public ChartFrame(final String title, JFreeChart chart) {
        this(title, chart, STANDARD_WIDTH, STANDARD_HEIGHT, false);
    }
    
    public ChartFrame(final String title, JFreeChart chart, int width, int height, boolean visible) {
    	super(title);

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(width, height));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
        
        pack();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(visible);
    }
    
    @Override
   public void windowClosing(java.awt.event.WindowEvent event) {
    	this.dispose();
    }
}
