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

import tools.descartes.bungee.cloud.CloudManagement;
import tools.descartes.bungee.cloud.aws.AWSManagement;
import tools.descartes.bungee.examples.RunBenchmark;
import tools.descartes.bungee.utils.FileUtility;

public class RunBenchmarkOnAWS {
	private static File fileLocation = FileUtility.FILE_LOCATION;
	private static CloudManagement cloud = new AWSManagement();
	
	public static void main(String[] args) {
		File measurementConfigFile  = new File(fileLocation,"propertyFiles/measurement.prop");
		RunBenchmark.runBenchmarkManagedCloud(cloud, measurementConfigFile, 1);
	}
}
