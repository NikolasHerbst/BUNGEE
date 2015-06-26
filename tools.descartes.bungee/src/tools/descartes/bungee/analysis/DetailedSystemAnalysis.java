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

package tools.descartes.bungee.analysis;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import tools.descartes.bungee.analysis.intensitysearch.BinaryIntensitySearch;
import tools.descartes.bungee.analysis.intensitysearch.IntensitySearch;
import tools.descartes.bungee.cloud.Bounds;
import tools.descartes.bungee.cloud.CloudManagement;
import tools.descartes.bungee.cloud.ResourceWatch;
import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.loadgeneration.JMeterController;
import tools.descartes.bungee.slo.ServiceLevelObjective;
import tools.descartes.bungee.utils.FileUtility;

public class DetailedSystemAnalysis extends SystemAnalysis {
	private IntensitySearch search;
	private CloudManagement cloudManagement;
	private ResourceWatch resWatcher;
	
	private int startIntensity = 20;
	

	public DetailedSystemAnalysis(JMeterController jMeter, CloudManagement cloudManagement) {
		this.search = new BinaryIntensitySearch(jMeter);
		this.cloudManagement = cloudManagement;
		this.resWatcher = new ResourceWatch(cloudManagement);
	}
	
	@Override
	public IntensityDemandMapping analyzeSystem(Host host, Request request, ServiceLevelObjective... slos) 
	{
		int startWith = startIntensity;
		IntensityDemandMapping mapping = new IntensityDemandMapping();
		SimpleDateFormat formatter = new SimpleDateFormat("_yyyy.MM.dd_HH.mm.ss");		
		File calibrationFolder = new File(new File (FileUtility.FILE_LOCATION,"calibration"),"detailed_"+
		host.getHostName()+"_" + request.getProblemSize() +formatter.format(new Date()));
		boolean aborted = false;
		Bounds origBounds = cloudManagement.getScalingBounds(host.getHostName());
		boolean cloudOk = true;
		for (int cloudSize = 1; cloudSize <= maxResources && !aborted; cloudSize++)
		{
			cloudOk = reconfigureCloud(host, cloudSize);
			File cloudSizeFolder = new File(calibrationFolder, Integer.toString(cloudSize) + "_Instances");
			if (cloudOk) {
				int intensity = search.searchIntensity(host, request, startWith, cloudSizeFolder, slos);		
				if (intensity != IntensitySearch.ABORTED && intensity > mapping.getMaxIntensity()) {
					System.out.println("Found maximum intensity for " + cloudSize + " resources: " + intensity);
					mapping.addMaxIntensityForResourceAmount(intensity, cloudSize);
					startWith = (int) ((intensity + mapping.getMaxIntensity(1)) * 0.6);
				} else {
					aborted = true;
				}			} else {
				System.out.println("Could not configure Cloud correct, abort...");
				aborted = true;
			}	
		}
		if (!aborted) {
			System.out.println("System analysis complete!");
			System.out.println("Maximum intensity: " + mapping.getMaxIntensity());
		} else {
			System.out.println("Analysed system for up to " + mapping.getMaxAvailableResources() + 
					" instances (intensity:" +mapping.getMaxIntensity()+ ")");
		}
		if (cloudManagement.setScalingBounds(host.getHostName(), origBounds)) {
			resWatcher.waitForResourceAmount(host.getHostName(), origBounds);
		}
		mapping.save(new File(calibrationFolder, "mapping.mapping"));
		return mapping;
	}

	private boolean reconfigureCloud(Host host, int cloudSize) {
		boolean cloudOk;
		Bounds bounds = new Bounds(cloudSize,cloudSize);
		cloudOk = cloudManagement.setScalingBounds(host.getHostName(), bounds);
		if (cloudOk) {			
			cloudOk = resWatcher.waitForResourceAmount(host.getHostName(), bounds);
		}
		return cloudOk;
	}
}
