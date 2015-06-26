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

import tools.descartes.bungee.analysis.DetailedSystemAnalysis;
import tools.descartes.bungee.analysis.SystemAnalysis;
import tools.descartes.bungee.cloud.aws.AWSManagement;
import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.loadgeneration.JMeterController;
import tools.descartes.bungee.slo.ServiceLevelObjective;
import tools.descartes.bungee.slo.SuccessRateSLO;
import tools.descartes.bungee.utils.FileUtility;

public class DetailedAWSAnalysis {
	
	
	
	public static void main(String[] args) {
		File jmeterPropertiesFile 	= new File(FileUtility.FILE_LOCATION, "propertyFiles/jmeter.prop");
		File hostPropertiesFile 	= new File(FileUtility.FILE_LOCATION, "propertyFiles/host.prop");
		File requestPropertiesFile 	= new File(FileUtility.FILE_LOCATION, "propertyFiles/request.prop");
		
		int maxResources = 3;
		double percent = 95;
		double responseTime = 500;

		ServiceLevelObjective slo = new SuccessRateSLO(percent, responseTime);

		JMeterController jMeter = new JMeterController(jmeterPropertiesFile);
		Request request = Request.load(requestPropertiesFile);
		Host host = Host.load(hostPropertiesFile);
		AWSManagement cloudManagement = new AWSManagement();
		
		//SystemAnalysis analysis = new SimpleSystemAnalysis(jMeter);
		SystemAnalysis analysis = new DetailedSystemAnalysis(jMeter, cloudManagement);
		
		
		analysis.setMaxResources(maxResources);
		analysis.analyzeSystem(host, request, slo);
	}
}
