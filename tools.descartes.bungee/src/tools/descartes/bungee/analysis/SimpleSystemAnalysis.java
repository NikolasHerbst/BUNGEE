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
import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.loadgeneration.JMeterController;
import tools.descartes.bungee.slo.ServiceLevelObjective;
import tools.descartes.bungee.utils.FileUtility;

public class SimpleSystemAnalysis extends SystemAnalysis {
	private IntensitySearch search;
	private int startIntensity = 40;
	

	public SimpleSystemAnalysis(File jmeterProperties,
			File requestProperties) {
		this(new JMeterController(jmeterProperties));
	}
	
	public SimpleSystemAnalysis(JMeterController jMeter) {
		this.search = new BinaryIntensitySearch(jMeter);
	}
	
	@Override
	public IntensityDemandMapping analyzeSystem(Host host, Request request, ServiceLevelObjective... slos) 
	{
		SimpleDateFormat formatter = new SimpleDateFormat("_yyyy.MM.dd_HH.mm.ss");
		
		File calibrationFolder = new File(new File (FileUtility.FILE_LOCATION,"calibration"),host.getHostName()+"_" + request.getProblemSize() +formatter.format(new Date()));
		int intensity = search.searchIntensity(host, request, startIntensity, calibrationFolder, slos);		
		
		IntensityDemandMapping mapping = new IntensityDemandMapping();
		if (intensity != IntensitySearch.ABORTED) {
			System.out.println("System analyzed!");
			System.out.println("Maximum intensity: " + intensity);
			for (int i = 1; i <= maxResources; i++)
			{
				mapping.addMaxIntensityForResourceAmount(intensity * i, i);
			}
		} else {
			System.out.println("Calibration aborted");
		}
		mapping.save(new File(calibrationFolder, "mapping.mapping"));
		return mapping;
	}
}
