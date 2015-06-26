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

import tools.descartes.bungee.allocation.DemandSupplyContainer;
import tools.descartes.bungee.chart.ChartGenerator;
import tools.descartes.bungee.utils.FileUtility;

public class ShowContainerChart {
	/**
	 * Starting point for the demonstration application.
	 *
	 * @param args  ignored.
	 */
	public static void main(final String[] args) {
		DemandSupplyContainer benchedSeries = DemandSupplyContainer.read(new File(FileUtility.FILE_LOCATION,"measurement/2014.05.29_14.57.41.measurement-oneDay-6h-B/allocations.seriescontainer"));
		JFreeChart chart = ChartGenerator.allocationChart(benchedSeries, false);
		ChartGenerator.showChart(chart, "Graph");
	}

}
