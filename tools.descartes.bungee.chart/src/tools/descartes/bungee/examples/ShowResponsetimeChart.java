/*******************************************************************************
* Copyright (c) 2014 Andreas Weber, Nikolas Herbst
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/

package tools.descartes.bungee.examples;

import java.io.File;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.JFreeChart;

import tools.descartes.bungee.chart.ChartGenerator;
import tools.descartes.bungee.loadgeneration.RunResult;
import tools.descartes.bungee.utils.FileUtility;

public class ShowResponsetimeChart {
	/**
	 * Starting point for the demonstration application.
	 *
	 * @param args  ignored.
	 */
	public static void main(final String[] args) {
		RunResult result = RunResult.read(new File(FileUtility.FILE_LOCATION,"measurement/141.21.72.21_500000_2014.04.01_15.41.14/result.runresult"));
		JFreeChart chart = ChartGenerator.responseTimeChart(result,1);
		ChartGenerator.showChart(chart, "Graph");
		DescriptiveStatistics responseTimeStats = result.getResponseTimeStats();
		System.out.println("Max: " + responseTimeStats.getMax());
		//ChartPDFWriter.writeChartToPDF(chart, 800,300, new File("responseChart.pdf"));
	}

}
