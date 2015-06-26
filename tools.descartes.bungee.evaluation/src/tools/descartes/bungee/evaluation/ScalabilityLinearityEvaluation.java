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

import tools.descartes.bungee.analysis.IntensityDemandMapping;
import tools.descartes.bungee.analysis.SimpleSystemAnalysis;
import tools.descartes.bungee.cloud.Bounds;
import tools.descartes.bungee.cloud.cloudstack.CloudSettings;
import tools.descartes.bungee.cloud.cloudstack.CloudstackControllerImpl;
import tools.descartes.bungee.cloud.cloudstack.CloudstackManagement;
import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.loadgeneration.JMeterController;
import tools.descartes.bungee.slo.ResponsetimePercentileSLO;
import tools.descartes.bungee.slo.ServiceLevelObjective;
import tools.descartes.bungee.utils.FileUtility;

public class ScalabilityLinearityEvaluation extends CloudStackExperiment {

	private SimpleSystemAnalysis analysis;
	private ServiceLevelObjective  slo;
	private int resourceNum;
	private String settingsName;
	private int step;
	private Request request;
	private CloudstackManagement cloudManagement;

	public ScalabilityLinearityEvaluation(JMeterController jmeter, CloudstackControllerImpl cloudController, String settingsName, Request request, int resourceNum, int step) {
		super(cloudController);
		analysis = new SimpleSystemAnalysis(jmeter);
		slo = new ResponsetimePercentileSLO(95, 500);
		expFolder = new File(FileUtility.FILE_LOCATION, "evaluation/scalabilityLinearity");
		this.settingsName = settingsName;
		cloudSettings = CloudSettings.load(new File(expFolder,settingsName));
		this.resourceNum = resourceNum;
		this.step = step;
		this.request = request;
		evaluateExistingMeasurement = false;
		CloudstackManagement staticCloudstackController = new CloudstackManagement(cloudController);
		staticCloudstackController.setCloudSettings(cloudSettings);
		cloudManagement = staticCloudstackController;
	}

	public void before() {
		measurementFolder = new File(expFolder,"2014.05.21_17.37.11_SmallOneCore");
		createMeasurementFolderAndSaveConfig();
	}


	@Override
	public void run() {
		if (!evaluateExistingMeasurement) {
			System.out.println("Analysis Linearity...");
			System.out.println("Create cloud for base measurement...");
			boolean createdCloud = cloudManagement.createStaticCloud(cloudSettings.getIp(), 1);
			if (createdCloud) {
				for (int i = 1; i <= resourceNum; i += step)
				{
					if (i != 1) {
						boolean adaptionOk = cloudManagement.setScalingBounds(cloudSettings.getIp(), new Bounds(i,i));
						if (!adaptionOk) {
							System.out.println("Reconfiguration of cloud for " + i + " resources faild, abort");
							break;
						}
					}
					Host host = new Host(cloudSettings.getIp(),cloudSettings.getPublicPort());
					analysis.setMaxResources(resourceNum);
					IntensityDemandMapping mapping = analysis.analyzeSystem(host, request, slo);
					if (mapping.isValid()) {
						mapping.save(new File(measurementFolder,cloudSettings.getOffering() + "-mapping"+i+".mapping"));					
					} else {
						System.out.println("Cloud not analyze system for resource amount: " + i + ", abort.");
						// not possible to create mapping, don't try it for bigger clouds.
						break;
					}
					// reset cloud config
					cloudSettings = CloudSettings.load(new File(expFolder,settingsName));
				}
				cloudManagement.destroyCloud(cloudSettings.getIp());
				System.out.println("Finished Linearity Evaluation run");
			} else {
				System.out.println("Cloud creation failed, abort experiment.");
			}
		}
	}



	private void writeEvaluationResultsToFile() {
		File baseFile = new File(measurementFolder,cloudSettings.getOffering() + "-mapping1.mapping");
		if (baseFile.exists()) {
			IntensityDemandMapping mappingBase = IntensityDemandMapping.read(baseFile);
			double baseResult = mappingBase.getMaxIntensity(1);
			PrintWriter writer;
			try {
				writer = new PrintWriter(new File(measurementFolder,cloudSettings.getOffering() + "-evaluationResults.csv"), FileUtility.ENOCDING);
				writer.println("n" +  FileUtility.CSV_SPLIT_BY  + "1 instance" +  FileUtility.CSV_SPLIT_BY  + "n instances (extrapolated)"  +  FileUtility.CSV_SPLIT_BY + "n instances "  +  FileUtility.CSV_SPLIT_BY + "abs. error" + FileUtility.CSV_SPLIT_BY + "rel. error");
				for (int i = 2; i <= resourceNum; i += step)
				{
					File file = new File(measurementFolder,cloudSettings.getOffering() + "-mapping"+i+".mapping");
					if (file.exists()) {
						IntensityDemandMapping mappingManyInstances = IntensityDemandMapping.read(file);
						double extraPolatedResult = mappingBase.getMaxIntensity(i);
						double measuredResult = mappingManyInstances.getMaxIntensity(1);				
						double absError = extraPolatedResult - measuredResult;
						writer.println(i + FileUtility.CSV_SPLIT_BY + baseResult +  FileUtility.CSV_SPLIT_BY + extraPolatedResult + FileUtility.CSV_SPLIT_BY + measuredResult +  FileUtility.CSV_SPLIT_BY + absError + FileUtility.CSV_SPLIT_BY + (absError / measuredResult));
					}
				}
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}


	public void after() {
		writeEvaluationResultsToFile();
	}
}
