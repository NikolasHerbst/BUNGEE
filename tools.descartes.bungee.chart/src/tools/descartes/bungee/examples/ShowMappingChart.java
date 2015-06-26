/*******************************************************************************
* Copyright (c) 2014 Andreas Weber, Nikolas Herbst
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/

package tools.descartes.bungee.examples;

import java.io.File;

import org.jfree.chart.JFreeChart;

//import tools.descartes.bungee.chart.ChartPDFWriter;
import tools.descartes.bungee.analysis.IntensityDemandMapping;
import tools.descartes.bungee.chart.ChartGenerator;
import tools.descartes.bungee.utils.FileUtility;

public class ShowMappingChart {
	/**
	 * Starting point for the demonstration application.
	 *
	 * @param args  ignored.
	 */
	public static void main(final String[] args) {
		IntensityDemandMapping mapping = IntensityDemandMapping.read(new File(FileUtility.FILE_LOCATION, "calibration/aws.m1.small-simple.mapping"));
		JFreeChart chart = ChartGenerator.mappingChart(mapping);
		ChartGenerator.showChart(chart, "Graph");
//		ChartPDFWriter.writeChartToPDF(chart, 350,400, new File("mapping.pdf"));
	}

}
