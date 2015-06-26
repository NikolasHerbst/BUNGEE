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

package tools.descartes.bungee.cloud.aws;

import tools.descartes.bungee.cloud.Bounds;
import tools.descartes.bungee.cloud.CloudInfo;
import tools.descartes.bungee.cloud.CloudManagement;

public class AWSManagement implements CloudInfo, CloudManagement {
	
	private AWSImpl awsCloud = new AWSImpl();
	
	@Override
	public int getNumberOfResources(String hostName) {
		ResourcesInfo resources = awsCloud.getResourceInfo(hostName);
		return resources.inService;
	}
	
	@Override
	public Bounds getScalingBounds(String hostName) {
		return awsCloud.getScalingBounds(hostName);
	}

	@Override
	public boolean setScalingBounds(String hostName, Bounds bounds) {
		boolean success = awsCloud.setScalingBounds(hostName, bounds);
		return success;
	}
}
