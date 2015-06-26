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

package tools.descartes.bungee.cloud;

import tools.descartes.bungee.utils.DateUtility;

public class ResourceWatch {
	private static final int SLEEP_BETWEEN_RESOURCE_AMOUNT_TESTS_MILLI= 1000;
	private static final long MAX_WAIT_TIME_FOR_CHANGE_MILLI = 6*DateUtility.MINUTE_TO_MILLI;
	private static final long SLEEP_FOR_STABILIZATIION_MILLI = 3*DateUtility.MINUTE_TO_MILLI;
	
	private boolean waitForStabilization = true;
	private CloudInfo cloud;
	
	public ResourceWatch(CloudInfo cloud) {
		this.cloud = cloud;
	}

	public boolean isWaitForStabilization() {
		return waitForStabilization;
	}

	public void setWaitForStabilization(boolean waitForStabilization) {
		this.waitForStabilization = waitForStabilization;
	}



	public boolean waitForResourceAmount(String hostName, Bounds bounds) {
		int resources = cloud.getNumberOfResources(hostName);
		if (!resAmountInBounds(resources, bounds)) {
			System.out.println("Wait until cloud resource amount is between " + bounds.getMin() + " and " + bounds.getMax() + " now: " + resources);
			long startTime = System.currentTimeMillis();
			while (!resAmountInBounds(resources, bounds) && System.currentTimeMillis() - startTime < MAX_WAIT_TIME_FOR_CHANGE_MILLI)
			{
				sleep(SLEEP_BETWEEN_RESOURCE_AMOUNT_TESTS_MILLI);
				int oldAmount = resources;
				resources = cloud.getNumberOfResources(hostName);
				if (oldAmount != resources) {
					startTime = System.currentTimeMillis();
					System.out.println("Wait until cloud resource amount is between " + bounds.getMin() + " and " + bounds.getMax() + " now: " + resources);
				}
			}
			if (!resAmountInBounds(resources, bounds)) {
				System.out.println("Waiting for resource adaption not successful (now: " + resources+"). Abort waiting.");
			} else {
				if (waitForStabilization) {
					waitForStabilization(resources);
				}
			}
		}
		return (resAmountInBounds(resources, bounds));
	}
	
	private boolean resAmountInBounds(int amount, Bounds bounds) {
		return (amount >= bounds.getMin() && amount <= bounds.getMax());
	}
	
	private void waitForStabilization(int amount)
	{
			//sleep some time to let the cloud stabilize
			System.out.println("Configured Cloud ( " + amount + " instances) successfully. Wait " 
					+ (SLEEP_FOR_STABILIZATIION_MILLI / DateUtility.MINUTE_TO_MILLI)+ " min. to let cloud stabilize.");
			sleep(SLEEP_FOR_STABILIZATIION_MILLI);
	}
	
	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
}
