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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import tools.descartes.bungee.allocation.ResourceAllocation;
import tools.descartes.bungee.allocation.SupplySeries;
import tools.descartes.bungee.allocation.SupplySeries.TYPE;

public class ResourceMonitor {
	private static final long POLLING_INTERVAL_MS = 1000;
	private CloudInfo cloudInfo;
	private ScheduledExecutorService executor;
	private String ip;
	private int lastNumberOfResources;
	private List<ResourceAllocation> allocations;
	
	class Task implements Runnable
	{
		@Override public void run()
		{
			int currentNumberOfResources = cloudInfo.getNumberOfResources(ip);
			if (currentNumberOfResources != lastNumberOfResources)
			{
				if (lastNumberOfResources != -1)
				{
					System.out.println("Detected demand change: " + lastNumberOfResources + " ->  " + currentNumberOfResources);
				}
				lastNumberOfResources = currentNumberOfResources;
				allocations.add(new ResourceAllocation(new Date(), currentNumberOfResources));
			}
		}
	}
	
	/**
	 * @param cloudInfo
	 */
	public ResourceMonitor(CloudInfo cloudInfo) {
		this.cloudInfo = cloudInfo;
		this.allocations = new LinkedList<ResourceAllocation>();
	}

	public void startMonitoring(String ip)
	{
		if (executor != null)
		{
			shutdownExecutor();
		}
		this.ip = ip;
		lastNumberOfResources = -1;
		allocations = new LinkedList<ResourceAllocation>();
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new Task(), 0, POLLING_INTERVAL_MS, TimeUnit.MILLISECONDS);
	}

	
	public void stopMonitoring()
	{
		shutdownExecutor();
	}
	
	public SupplySeries getMonitoredSupply() {
		return new SupplySeries(allocations, TYPE.MONITORED);
	}
	
	private void shutdownExecutor() {
		boolean terminated = false;
		executor.shutdown();
		try {
			terminated = executor.awaitTermination(POLLING_INTERVAL_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
		if (!terminated) {
			executor.shutdownNow();			
		}
	}

}
