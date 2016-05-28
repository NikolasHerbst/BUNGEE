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
import tools.descartes.bungee.analysis.SimpleSystemAnalysis;
import tools.descartes.bungee.analysis.SystemAnalysis;
import tools.descartes.bungee.cloud.cloudstack.CloudstackManagement;
import tools.descartes.bungee.cloud.cloudstack.DetailedSystemAnalysisWithoutLoadBalancer;
import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.loadgeneration.JMeterController;
import tools.descartes.bungee.slo.ResponsetimePercentileSLO;
import tools.descartes.bungee.slo.ServiceLevelObjective;
import tools.descartes.bungee.utils.FileUtility;

public class DetailedCloudStackAnalysis {
	
	
	
	public static void main(String[] args) {
		File jmeterPropertiesFile 	= new File(FileUtility.FILE_LOCATION, "propertyFiles/jmeter.prop");
		//File hostPropertiesFile 	= new File(FileUtility.FILE_LOCATION, "propertyFiles/bungeeservlet.prop");
		File hostPropertiesFile 	= new File(FileUtility.FILE_LOCATION, "propertyFiles/host.prop");
		File requestPropertiesFile 	= new File(FileUtility.FILE_LOCATION, "propertyFiles/request.prop");
		File cloudStackPropsFile 	= new File(FileUtility.FILE_LOCATION, "propertyFiles/cloudstack.prop");
		File cloudSettingFile		= new File(FileUtility.FILE_LOCATION, "propertyFiles/cloudSettings.prop");
		
		int maxResources = 5;
		double percent = 95;
		int responseTime = 500;

		ServiceLevelObjective slo = new ResponsetimePercentileSLO(percent, responseTime);

		JMeterController jMeter = new JMeterController(jmeterPropertiesFile);
		Request request = Request.load(requestPropertiesFile);
		Host host = Host.load(hostPropertiesFile);
		CloudstackManagement cloudManagement = new CloudstackManagement(cloudStackPropsFile);
		cloudManagement.setCloudSettings(cloudSettingFile);
		
		
		
		//SystemAnalysis analysis = new SimpleSystemAnalysis(jMeter);
		SystemAnalysis analysis = new DetailedSystemAnalysisWithoutLoadBalancer(jMeter, cloudManagement);
		
		//host.setHostName("10.1.1.10");
		
		analysis.setMaxResources(maxResources);
		analysis.analyzeSystem(host, request, slo);
	}
}
