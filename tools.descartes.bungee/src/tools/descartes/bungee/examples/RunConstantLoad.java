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

import tools.descartes.bungee.analysis.intensitysearch.BinaryIntensitySearch;
import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.loadgeneration.RunResult;
import tools.descartes.bungee.utils.FileUtility;

public class RunConstantLoad {

	public static void main(String[] args) {
		File fileLocation = FileUtility.FILE_LOCATION;
		File testFolder = new File(fileLocation,"test");
		File propertyFolder = new File(fileLocation,"propertyFiles");
		
		File jmeterPropertiesFile 	= new File(propertyFolder, "jmeter.prop");
		File hostPropertiesFile 	= new File(propertyFolder, "host.prop");
		File requestPropertiesFile 	= new File(propertyFolder, "request.prop");
		int warmUpSeconds = 60;
		int intensity = 500;
		int duration = 10;

		testFolder.mkdirs();
		
		BinaryIntensitySearch intensitySearch = new BinaryIntensitySearch(jmeterPropertiesFile);
		RunResult runResult = intensitySearch.runConstantLoad(Host.load(hostPropertiesFile), Request.load(requestPropertiesFile), warmUpSeconds, intensity, duration, new File(testFolder,"timestamps.txt"),  new File(testFolder,"responses.txt"));
		runResult.save(new File(testFolder,"testrun.runresult"));
	}
}
