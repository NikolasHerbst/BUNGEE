/*******************************************************************************
* Copyright (c) 2014 Andreas Weber, Nikolas Herbst
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/

package tools.descartes.bungee.examples;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

//import tools.descartes.bungee.chart.ChartPDFWriter;
import tools.descartes.bungee.allocation.DemandSupplyContainer;
import tools.descartes.bungee.calibration.LoadProfileAdjustment;
import tools.descartes.bungee.chart.ChartGenerator;
import tools.descartes.bungee.config.MeasurementConfig;
import tools.descartes.bungee.loadgeneration.RunResult;
import tools.descartes.bungee.loadprofile.LoadProfile;
import tools.descartes.bungee.utils.FileUtility;

public class CombinedChart {
	/**
	 * Starting point for the demonstration application.
	 *
	 * @param args  ignored.
	 */
	public static void main(final String[] args) {
		File measurementFolder = new File(FileUtility.FILE_LOCATION, "measurement/2014.07.01_17.59.09.measurement-oneDay-6h-aws.small.simple2_80p_60s_50p_60s_60qs_add3-K1");
		
		createCombinedChart(measurementFolder);
	}

	public static void createCombinedChart(File measurementFolder) {
		MeasurementConfig measurmentConfig = MeasurementConfig.load(new File(measurementFolder, "measurement.prop"));
		DemandSupplyContainer benchedSeries = DemandSupplyContainer.read(new File(measurementFolder, "allocations.seriescontainer"));

		LoadProfileAdjustment adjustment = new LoadProfileAdjustment();
		LoadProfile loadProfile = measurmentConfig.getLoadProfile();
		LoadProfile adjustedProfile = adjustment.adjustLoadProfile(loadProfile, measurmentConfig.getMapping(), measurmentConfig.getNumberOfBenchmarkedResources(), measurmentConfig.getMaxIntensity());
//		RunResult result = RunResult.read(new File(measurementFolder, "result.runresult"));
		
		// create plots
		XYPlot intensityPlot = ChartGenerator.createIntensityPlot(loadProfile.getArrivalRates());
		XYPlot adjustedPlot = ChartGenerator.createIntensityPlot(adjustedProfile.getArrivalRates());
		XYPlot allocationPlot = ChartGenerator.allocationPlot(benchedSeries);
//		XYPlot responsePlot = ChartGenerator.createResponseTimePlot(result, "");
		List<XYPlot> plots = new LinkedList<XYPlot>();
		
		plots.add(allocationPlot);
//		plots.add(responsePlot);
		JFreeChart chart = ChartGenerator.createTimeSeriesChart(plots);
		
//		ChartPDFWriter.writeChartToPDF(chart, 800,360, new File(measurementFolder,"AllocationResponseGraph.pdf"));
		
		plots.add(0, intensityPlot);
		plots.add(0, adjustedPlot);
		chart = ChartGenerator.createTimeSeriesChart(plots);
//		ChartPDFWriter.writeChartToPDF(chart, 800,510, new File(measurementFolder,"AllocationIntensityResponseGraph.pdf"));
		ChartGenerator.showChart(chart, "Graph");
	}

}
