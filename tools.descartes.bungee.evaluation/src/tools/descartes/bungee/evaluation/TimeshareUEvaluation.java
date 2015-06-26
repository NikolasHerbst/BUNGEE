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

import tools.descartes.bungee.cloud.cloudstack.CloudstackControllerImpl;
import tools.descartes.bungee.examples.RunBenchmark;
import tools.descartes.bungee.utils.FileUtility;

public class TimeshareUEvaluation extends CloudStackExperiment {
	
	private int[] durations;

	public TimeshareUEvaluation(CloudstackControllerImpl cloudController) {
		super(cloudController);
		expFolder = new File(FileUtility.FILE_LOCATION, "evaluation/timeshareU");
		measurementConfigFile = new File(expFolder,"measurement.prop");
		evaluateExistingMeasurement = false;
	}

	public void before() {
		measurementFolder = new File(expFolder,"2014.05.05_06.48.08");
		
		loadCloudAndMeasurmentConfig();
		createMeasurementFolderAndSaveConfig();
		durations = loadVariations(new File(expFolder,"durations.txt"));
		
		cleanUp();
	}


	@Override
	public void run() {
		if (!evaluateExistingMeasurement)
		{
			System.out.println("Start Experiment: TimeshareU Evaluation");
			for (int duration : durations)
			{
				cloudSettings.getScaleUp().setDuration(duration);
				String id = cloudController.createAutoScaleVMGroup(cloudSettings);
				System.out.println("Created Auto Scale VM Group: " + id);
				String publicIP = remapIP(cloudSettings.getIp()); 
				System.out.println("Wait for 10 Minutes... to ensure everything started correctly");
				try {
					Thread.sleep(10 * 60 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				waitForIpToComeOnline(publicIP);
				waitForCorrectResourceAmount();

				System.out.println("Run for scale up duration: " + duration);
				File runFolder  = new File(measurementFolder,Integer.toString(duration));
				RunBenchmark.runBenchmark(cloudInfo, runFolder, measurmentConfig);
				
				// delte autoscalegroup again
				cloudController.deleteAutoScaleVMGroupByIp(cloudSettings.getIp());
				// reset config for next run
				loadCloudAndMeasurmentConfig();
			}	
		}
	}

	public void after() {
		createCombinedMetricAndGraphFiles(durations);
	}
}
