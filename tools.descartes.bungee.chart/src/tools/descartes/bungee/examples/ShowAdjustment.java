/*******************************************************************************
* Copyright (c) 2014 Andreas Weber, Nikolas Herbst
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/

package tools.descartes.bungee.examples;

import java.io.File;
import java.util.List;

import org.jfree.chart.JFreeChart;

//import tools.descartes.bungee.chart.ChartPDFWriter;
import tools.descartes.bungee.analysis.IntensityDemandMapping;
import tools.descartes.bungee.calibration.AdjustmentFunctionGeneration;
import tools.descartes.bungee.calibration.PiecewiseLinearAdjustmentGenerator;
import tools.descartes.bungee.chart.ChartGenerator;
import tools.descartes.bungee.loadprofile.AdjustedLoadProfile;
import tools.descartes.bungee.loadprofile.AdjustmentFunction;
import tools.descartes.bungee.loadprofile.DlimAdapter;
import tools.descartes.bungee.loadprofile.LoadProfile;
import tools.descartes.bungee.utils.FileUtility;
import tools.descartes.dlim.generator.*;

public class ShowAdjustment {
	public static void main(String[] args) {
		AdjustmentFunctionGeneration calibrator = new PiecewiseLinearAdjustmentGenerator();
		File fileLocation = FileUtility.FILE_LOCATION;
		
		
		File mappingFile = new File(new File(fileLocation, "calibration"),"detailed_10.1.3.4_500000_2014.05.20_16.43.31/mapping.mapping");
		File dlimFile = new File(new File(fileLocation, "model"), "DLIM_ibmtrx_weekday_simple_Trendlength1_noNoise_OneDay.dlim");
		
		LoadProfile model = DlimAdapter.read(dlimFile, 900);
		
		List<ArrivalRateTuple> intensities = model.getArrivalRates();
		IntensityDemandMapping mapping = IntensityDemandMapping.read(mappingFile);
		
		System.out.println("Max Intensity: " + model.getMaxIntensity());
		
		
		// adjust intensities
		AdjustmentFunction adjustmentFunction = calibrator.getAdjustmentFunction(mapping, 10, model.getMaxIntensity());
		LoadProfile adjustedIntensityModel = new AdjustedLoadProfile(model, adjustmentFunction);
		intensities = adjustedIntensityModel.getArrivalRates();
		
		System.out.println("Adusted Intensity: " + adjustedIntensityModel.getMaxIntensity());
		
		// show demand with adjustedIntensities
		JFreeChart chart = ChartGenerator.demandChart(intensities, mapping, true);
		ChartGenerator.showChart(chart, "Normal");
//		ChartPDFWriter.writeChartToPDF(chart, 800,400, new File("demandChart2.pdf"));
	}

}
