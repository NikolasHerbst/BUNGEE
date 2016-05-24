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

package tools.descartes.bungee.examples;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import tools.descartes.bungee.allocation.DemandSupplyContainer;
import tools.descartes.bungee.calibration.LoadProfileAdjustment;
import tools.descartes.bungee.cloud.Bounds;
import tools.descartes.bungee.cloud.CloudInfo;
import tools.descartes.bungee.cloud.CloudManagement;
import tools.descartes.bungee.cloud.ResourceWatch;
import tools.descartes.bungee.config.MeasurementConfig;
import tools.descartes.bungee.loadgeneration.JMeterController;
import tools.descartes.bungee.loadprofile.LoadProfile;
import tools.descartes.bungee.measurement.MeasurementRunner;
import tools.descartes.bungee.metric.MetricToFileWriter;
import tools.descartes.bungee.utils.FileUtility;

public class RunBenchmark {
	private static LoadProfileAdjustment adjustment = new LoadProfileAdjustment();
	private static File fileLocation = FileUtility.FILE_LOCATION;
	private static JMeterController jmeter = new JMeterController(new File(fileLocation,"propertyFiles/jmeter.prop"));
	
	public static void runBenchmark(CloudInfo cloud, File configFile, int loops) {
		ResourceWatch watcher = new ResourceWatch(cloud);
		MeasurementConfig config = MeasurementConfig.load(configFile);
		for (int i = 1; i <= loops; i++) {
			if (watcher.waitForResourceAmount(config.getHost().getHostName(), new Bounds(1,1))) {
				// create measurement folder
				File measurementFolder = createMeasurementFolder(fileLocation, configFile);
				runBenchmark(cloud, measurementFolder, config);
			} else {
				System.out.println("Abort Benchmarking!");
			}
		}
	}

/*	public static void runBenchmarkManagedCloud(CloudManagement cloud, File configFile, int loops) {
		MeasurementConfig config = MeasurementConfig.load(configFile);
		cloud.setScalingBounds(config.getHost().getHostName(), new Bounds(1,config.getNumberOfBenchmarkedResources()));
		runBenchmark(cloud, configFile, loops);
		// save costs and deallocate resources;
		cloud.setScalingBounds(config.getHost().getHostName(), new Bounds(0,0));
	}*/
	
	public static void runBenchmark(CloudInfo cloud, File measurementFolder, MeasurementConfig config) {
		// save config
		config.save(new File(measurementFolder, "measurement.prop"));
		
		//adjust load profile
		LoadProfile loadProfile = config.getLoadProfile();
		loadProfile = adjustment.adjustLoadProfile(loadProfile, config.getMapping(), config.getNumberOfBenchmarkedResources(), config.getMaxIntensity());
		
		// run measurement
		MeasurementRunner runner = new MeasurementRunner(jmeter,cloud);
		DemandSupplyContainer container = runner.runMeasurement(measurementFolder, config.getHost(), config.getRequest(), config.getMapping(), loadProfile, config.getWarmUpDurationInSeconds());
		
		// generate metric file
		MetricToFileWriter.writeMetricsToFile(container,MetricToFileWriter.STD_METRIC_LIST, new File(measurementFolder,"metrics.csv"));
		
		// create chart
		//CombinedChart.createCombinedChart(measurementFolder);
		
		// generate scheduleDiffs and violations
		DiffsAndViolations.diffsAndViolations(measurementFolder);
	}
	

	private static File createMeasurementFolder(File fileLocation, File configFile) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
		File measurementFolder = new File(new File (fileLocation,"measurement"),formatter.format(new Date())+"."+FileUtility.getFileNameWithoutExtension(new File(configFile.getName())));
		measurementFolder.mkdirs();
		return measurementFolder;
	}
}
