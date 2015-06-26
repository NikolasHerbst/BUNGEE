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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import tools.descartes.bungee.analysis.DetailedSystemAnalysis;
import tools.descartes.bungee.analysis.IntensityDemandMapping;
import tools.descartes.bungee.analysis.SystemAnalysis;
import tools.descartes.bungee.cloud.CloudManagement;
import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.loadgeneration.JMeterController;
import tools.descartes.bungee.slo.ServiceLevelObjective;
import tools.descartes.bungee.utils.FileUtility;

public class DetailedScalabilityLinearityEvaluation extends AbstractExperiment {

	private SystemAnalysis analysis;
	private ServiceLevelObjective[]  slos;
	private int resourceNum;
	private int reps;
	private Request request;
	private int start;
	protected File measurementFolder;
	protected boolean evaluateExistingMeasurement;
	protected File expFolder;
	protected Host host;
	private static DecimalFormat format = new DecimalFormat( "#,###,###,##0" );

	public DetailedScalabilityLinearityEvaluation(JMeterController jmeter, CloudManagement cloudManagement, Host host, Request request, int resourceNum, int reps, ServiceLevelObjective... slos) {
		analysis = new DetailedSystemAnalysis(jmeter, cloudManagement);
		this.slos = slos;
		expFolder = new File(FileUtility.FILE_LOCATION, "evaluation/scalabilityLinearityDetailed");
		this.resourceNum = resourceNum;
		this.reps = reps;
		this.request = request;
		this.host = host;
		evaluateExistingMeasurement = false;
		start = 1;
	}

	public void before() {
		measurementFolder = new File(expFolder,"m1-small-combined");
		createMeasurementFolderAndSaveConfig();
	}


	@Override
	public void run() {
		if (!evaluateExistingMeasurement) {
			System.out.println("Analysis Linearity...");
			for (int run = start; run <= reps; run++) {
				analysis.setMaxResources(resourceNum);
				IntensityDemandMapping mapping = analysis.analyzeSystem(host, request, slos);
				if (mapping.isValid()) {
					mapping.save(new File(measurementFolder, host.getHostName() + "-mapping"+run+".mapping"));					
				} else {
					System.out.println("Cloud not analyze system for run: " + run + ", abort.");
					break;
				}
			}
			System.out.println("Finished Linearity Evaluation run");
		}
	}


	private void writeEvaluationResultsToFile() {
		PrintWriter writer;
		try {
			writer = new PrintWriter(new File(measurementFolder,host.getHostName() + "-evaluationResults.csv"), FileUtility.ENOCDING);
			writer.print("run");
			for (int i = 1; i <= resourceNum; i += 1)
			{
				writer.print(FileUtility.CSV_SPLIT_BY+i);
			}
			writer.println();
			for (int run = start; run <= reps; run++)
			{
				File file = new File(measurementFolder,host.getHostName() + "-mapping"+run+".mapping");
				if (file.exists()) {
					writer.print(run);
					IntensityDemandMapping mapping = IntensityDemandMapping.read(file);
					for (int i = 1; i <= mapping.getMaxAvailableResources(); i += 1)
					{
						writer.print(FileUtility.CSV_SPLIT_BY + format.format(mapping.getMaxIntensity(i)));
					}
					writer.println();
				}
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	protected void createMeasurementFolderAndSaveConfig() {
		if (!evaluateExistingMeasurement) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
			measurementFolder = new File(expFolder,formatter.format(new Date())+"_"+host.getHostName());
			measurementFolder.mkdirs();			
		}
	}

	public void after() {
		writeEvaluationResultsToFile();
	}
}
