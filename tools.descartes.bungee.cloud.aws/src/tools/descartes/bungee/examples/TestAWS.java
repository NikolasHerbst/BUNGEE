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

import tools.descartes.bungee.cloud.Bounds;
import tools.descartes.bungee.cloud.CloudManagement;
import tools.descartes.bungee.cloud.ResourceWatch;
import tools.descartes.bungee.cloud.aws.AWSManagement;

public class TestAWS {

	public static void main(String[] args) {
		String hostName = "testBalancer-2044919475.eu-west-1.elb.amazonaws.com";
		Bounds bounds = new Bounds(1,1);
		
		CloudManagement cloudManagement = new AWSManagement();
		ResourceWatch resWatch = new ResourceWatch(cloudManagement);
		resWatch.setWaitForStabilization(false);
		long startTrigger = System.currentTimeMillis();
		System.out.println(cloudManagement.setScalingBounds(hostName, bounds));
		long endTrigger = System.currentTimeMillis();
		resWatch.waitForResourceAmount(hostName, bounds);
		long correctAmount =  System.currentTimeMillis();
		System.out.println("complete (s): " + (double)(correctAmount - startTrigger)/1000);
		System.out.println("after change (s): " + (double)(correctAmount - endTrigger)/1000);
	}

}
