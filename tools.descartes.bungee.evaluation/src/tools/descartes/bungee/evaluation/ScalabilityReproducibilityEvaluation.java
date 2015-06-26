/*******************************************************************************
Copyright 2015 Andreas Weber, Nikolas Herbst

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*******************************************************************************/
package tools.descartes.bungee.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import tools.descartes.bungee.analysis.IntensityDemandMapping;
import tools.descartes.bungee.analysis.SimpleSystemAnalysis;
import tools.descartes.bungee.analysis.SystemAnalysis;
import tools.descartes.bungee.cloud.cloudstack.CloudSettings;
import tools.descartes.bungee.cloud.cloudstack.CloudstackControllerImpl;
import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.loadgeneration.JMeterController;
import tools.descartes.bungee.slo.ResponsetimePercentileSLO;
import tools.descartes.bungee.slo.ServiceLevelObjective;
import tools.descartes.bungee.utils.FileUtility;

public class ScalabilityReproducibilityEvaluation extends CloudStackExperiment {

	private double confidence;
	private double diffPercent;
	private int minRuns = 10;
	private int maxRuns = 30;
	private SystemAnalysis analysis;
	private ServiceLevelObjective  slo;
	private Request request;

	public ScalabilityReproducibilityEvaluation(JMeterController jmeter, CloudstackControllerImpl cloudController, String settingsName, Request request,double confidence, double diffPercent) {
		super(cloudController);
		this.confidence = confidence;
		this.diffPercent = diffPercent;
		this.request = request;
		slo = new ResponsetimePercentileSLO(95, 500);
		expFolder = new File(FileUtility.FILE_LOCATION, "evaluation/scalabilityReproducability");
		cloudSettings = CloudSettings.load(new File(expFolder,settingsName));
		analysis = new SimpleSystemAnalysis(jmeter);
		evaluateExistingMeasurement = false;
	}

	public void before() {
		measurementFolder = new File(expFolder,"2014.04.28_12.25.46");

		createMeasurementFolderAndSaveConfig();

		cleanUp();
		if (!evaluateExistingMeasurement) {			
			String id = cloudController.createAutoScaleVMGroup(cloudSettings);
			System.out.println("Created Auto Scale VM Group: " + id);
			String publicIP = remapIP(cloudSettings.getIp()); 
			System.out.println("Wait for 5 Minutes... to ensure everything started correctly");
			try {
				Thread.sleep(5 * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			waitForIpToComeOnline(publicIP);
			waitForCorrectResourceAmount();
			System.out.println("Disable Auto Scaling before Scalability Analysis");
			cloudController.disableAutoScaleGroup(cloudSettings.getIp());
			remapIP(cloudSettings.getIp()); 
		}
	}


	@Override
	public void run() {
		System.out.println("Analysis started...");
		List<Double> firstStepResults = new LinkedList<Double>();
		Host host = new Host(cloudSettings.getIp(),cloudSettings.getPrivatePort());
		analysis.setMaxResources(cloudSettings.getMaxInstances());
		int runs = 0;
		boolean finished = false;
		
		while (runs < minRuns || (runs < maxRuns && !finished)) {
			runs++;
			System.out.println("run number: " + runs);
			IntensityDemandMapping mapping = analysis.analyzeSystem(host, request, slo);
			double result = mapping.getMaxIntensity(1);
			firstStepResults.add(result);
			writeAnalysisResultsToFile(firstStepResults);
			
			if (runs > 1) {
				SummaryStatistics summaryStats = createSummaryStatistics(firstStepResults);
				double wantedLower = (1-diffPercent) * summaryStats.getMean();
				double wantedUpper = (1+diffPercent) * summaryStats.getMean();
				double width = getConfidenceIntervalWidth(summaryStats, confidence);				
				double lowerConfidence = summaryStats.getMean() - width;
				double upperConfidence = summaryStats.getMean() + width;
				finished = lowerConfidence > wantedLower;
				printStatistics(summaryStats, lowerConfidence, upperConfidence, wantedLower, wantedUpper, finished);
				writeEvaluationResultsToFile(firstStepResults, summaryStats, lowerConfidence, upperConfidence, wantedLower, wantedUpper, finished);
			}
		}
		
		
	}

	private void printStatistics(SummaryStatistics summaryStats,
			double lowerConfidence, double upperConfidence, double wantedLower,
			double wantedUpper, boolean finished) {
		System.out.println("total runs: " + summaryStats.getN());
		System.out.println("mean: " + summaryStats.getMean());
		System.out.println("stdDev: " + summaryStats.getStandardDeviation());
		System.out.println("confidence interval [" + lowerConfidence + "," + upperConfidence + "]");
		System.out.println("wanted interval [" + wantedLower + "," + wantedUpper + "]");
		System.out.println("success: " + finished);
	}

	private void writeEvaluationResultsToFile(List<Double> firstStepResults,
			SummaryStatistics summaryStats, double lowerConfidence,
			double upperConfidence, double wantedLower, double wantedUpper,
			boolean finished) {
		String resultsString = analysisResultCSVString(firstStepResults);
		PrintWriter writer;
		try {
			writer = new PrintWriter(new File(measurementFolder,cloudSettings.getOffering() + "-evaluationResults.csv"), FileUtility.ENOCDING);
			writer.println(resultsString);
			writer.println("runs: " +  FileUtility.CSV_SPLIT_BY + summaryStats.getN());
			writer.println("mean" +  FileUtility.CSV_SPLIT_BY + summaryStats.getMean());
			writer.println("stdDev" +  FileUtility.CSV_SPLIT_BY + summaryStats.getStandardDeviation());
			writer.println(Double.toString(diffPercent*100)+ "%-interval" + FileUtility.CSV_SPLIT_BY + Double.toString(wantedLower) + FileUtility.CSV_SPLIT_BY + Double.toString(wantedUpper));
			writer.println(Double.toString(confidence*100)+ "% confidence interval" + FileUtility.CSV_SPLIT_BY + Double.toString(lowerConfidence) + FileUtility.CSV_SPLIT_BY + Double.toString(upperConfidence));
			writer.println("confidence interval small enough" +  FileUtility.CSV_SPLIT_BY + finished);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void writeAnalysisResultsToFile(List<Double> firstStepResults) {
		String resultsString = analysisResultCSVString(firstStepResults);
		PrintWriter writer;
		try {
			writer = new PrintWriter(new File(measurementFolder,cloudSettings.getOffering() + "-analysisResults.csv"), FileUtility.ENOCDING);
			writer.println(resultsString);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private String analysisResultCSVString(List<Double> firstStepResults) {
		String resultsString = "";
		for (double result : firstStepResults) {
			resultsString += FileUtility.CSV_SPLIT_BY + Double.toString(result);
		}
		resultsString = resultsString.substring(1);
		return resultsString;
	}

	private SummaryStatistics createSummaryStatistics(List<Double> firstStepResults) {
		SummaryStatistics summaryStats = new SummaryStatistics();
		for (double x : firstStepResults) {
			summaryStats.addValue(x);
		}
		return summaryStats;
	}
	
	private double getConfidenceIntervalWidth(SummaryStatistics summaryStatistics, double confidence) {
		double significance = 1 - confidence;
		TDistribution tDist = new TDistribution(summaryStatistics.getN() - 1);
		double a = tDist.inverseCumulativeProbability(1.0 - significance/2);
		return a * summaryStatistics.getStandardDeviation() / Math.sqrt(summaryStatistics.getN());
	}

	public void after() {
		if (!evaluateExistingMeasurement)
		{
			// delte autoscalegroup again
			cloudController.deleteAutoScaleVMGroupByIp(cloudSettings.getIp());
		}
	}
}
