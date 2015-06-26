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
import java.util.LinkedList;
import java.util.List;

import tools.descartes.bungee.cloud.CloudManagement;
import tools.descartes.bungee.cloud.aws.AWSManagement;
import tools.descartes.bungee.cloud.cloudstack.CloudstackControllerImpl;
import tools.descartes.bungee.cloud.cloudstack.CloudstackManagement;
import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.loadgeneration.JMeterController;
import tools.descartes.bungee.slo.ServiceLevelObjective;
import tools.descartes.bungee.slo.SuccessRateSLO;
import tools.descartes.bungee.utils.FileUtility;


public class ExperimentRunner {
	public static void main(String[] args) {
		File fileLocation = FileUtility.FILE_LOCATION;
		File cloudControllerFile = new File(fileLocation,"propertyFiles/cloudstack.prop");
		File jmeterProperties = new File(fileLocation,"propertyFiles/jmeter.prop");
		File requestPropertiesFile 	= new File(FileUtility.FILE_LOCATION, "propertyFiles/request.prop");
		File cloudStackHostFile 	= new File(FileUtility.FILE_LOCATION, "propertyFiles/host.prop");
		File awsSmallHostFile 		= new File(FileUtility.FILE_LOCATION, "propertyFiles/hostAWS-m1.small.prop");
		File awsMediumHostFile 		= new File(FileUtility.FILE_LOCATION, "propertyFiles/hostAWS-m1.medium.prop");
		Host cloudStackHost = Host.load(cloudStackHostFile);
		Host awsHost = Host.load(awsSmallHostFile);
		JMeterController jmeter = new JMeterController(jmeterProperties);
		CloudstackControllerImpl cloudController = new CloudstackControllerImpl(cloudControllerFile);
		CloudManagement cloudStack = new CloudstackManagement(cloudController);
		CloudManagement awsManagement = new AWSManagement();
		Request request = Request.load(requestPropertiesFile);
		ServiceLevelObjective slo = new SuccessRateSLO(95, 500);
		
		AbstractExperiment timeshareU = new TimeshareUEvaluation(cloudController);
		AbstractExperiment timeshareO = new TimeshareOEvaluation(cloudController);
		AbstractExperiment accuracyO = new AccuracyOEvaluation(cloudController);
		AbstractExperiment accuracyU = new AccuracyUEvaluation(cloudController);
		AbstractExperiment jitterPos = new JitterPosEvaluation(cloudController);
		AbstractExperiment jitterNeg = new JitterNegEvaluation(cloudController);
		AbstractExperiment repro1Core = new ScalabilityReproducibilityEvaluation(new JMeterController(jmeterProperties), new CloudstackControllerImpl(cloudControllerFile), "cloudSettings1Core.prop", Request.load(requestPropertiesFile), 0.95, 0.05);
		AbstractExperiment repro2Core = new ScalabilityReproducibilityEvaluation(new JMeterController(jmeterProperties), new CloudstackControllerImpl(cloudControllerFile), "cloudSettings2Core.prop", Request.load(requestPropertiesFile), 0.95, 0.05);
		AbstractExperiment repro4Core = new ScalabilityReproducibilityEvaluation(new JMeterController(jmeterProperties), new CloudstackControllerImpl(cloudControllerFile), "cloudSettings4Core.prop", Request.load(requestPropertiesFile), 0.95, 0.05);
		AbstractExperiment linearity1Core = new ScalabilityLinearityEvaluation(jmeter, cloudController, "cloudSettings1Core.prop", request, 12,1);
		AbstractExperiment linearity2Core = new ScalabilityLinearityEvaluation(jmeter, cloudController, "cloudSettings2Core.prop", request, 20,1);
		AbstractExperiment linearity4Core = new ScalabilityLinearityEvaluation(jmeter, cloudController, "cloudSettings4Core.prop", request, 20,1);
		AbstractExperiment detailed = new DetailedScalabilityLinearityEvaluation(jmeter, awsManagement, awsHost, request, 10, 8, slo);
		
		List<AbstractExperiment> experimentList = new LinkedList<AbstractExperiment>();
		experimentList.add(detailed);
//		experimentList.add(jitterPos);
//		experimentList.add(accuracyU);
//		experimentList.add(linearity2Core);
//		experimentList.add(linearity1Core);
//		experimentList.add(linearity1Core);
//		experimentList.add(linearity4Core);
//		experimentList.add(accuracyO);
//		experimentList.add(jitterNeg);
//		experimentList.add(repro1Core);
//		experimentList.add(repro2Core);
//		experimentList.add(repro4Core);
//		experimentList.add(timeshareU);
//		experimentList.add(timeshareO);
		runExperiments(experimentList);
	}

	private static void runExperiments(List<AbstractExperiment> experimentList) {
		for (AbstractExperiment experiment : experimentList)
		{
			runExperiment(experiment);
		}
	}
	
	public static void runExperiment(AbstractExperiment experiment)
	{
		try {
			experiment.before();
			experiment.run();
			experiment.after();
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("catched exception, experiment aborted");
		}
		
	}
}
