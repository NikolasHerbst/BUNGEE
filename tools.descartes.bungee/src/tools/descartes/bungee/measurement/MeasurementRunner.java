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

package tools.descartes.bungee.measurement;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import tools.descartes.bungee.allocation.DemandSeries;
import tools.descartes.bungee.allocation.DemandSupplyContainer;
import tools.descartes.bungee.allocation.ResourceAllocation;
import tools.descartes.bungee.allocation.SupplySeries;
import tools.descartes.bungee.analysis.IntensityDemandMapping;
import tools.descartes.bungee.cloud.CloudInfo;
import tools.descartes.bungee.cloud.ExtendedCloudInfo;
import tools.descartes.bungee.cloud.ResourceMonitor;
import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.loadgeneration.JMeterController;
import tools.descartes.bungee.loadgeneration.RunResult;
import tools.descartes.bungee.loadprofile.LoadProfile;
import tools.descartes.bungee.loadprofile.LoadProfileWithTimestampGeneration;
import tools.descartes.bungee.loadprofile.LoadProfileWithWarmUp;
import tools.descartes.bungee.utils.DateUtility;
import tools.descartes.dlim.generator.ArrivalRateTuple;


public class MeasurementRunner {
	private JMeterController jmeter;
	private CloudInfo cloudInfo;
	private ResourceMonitor monitor;
	

	public MeasurementRunner(JMeterController jmeter,
			CloudInfo cloudInfo) {
		this.jmeter = jmeter;
		this.cloudInfo = cloudInfo;
		monitor = new ResourceMonitor(cloudInfo);
	}


	public DemandSupplyContainer runMeasurement(File measurementFolder, Host host, Request request,
			IntensityDemandMapping mapping, LoadProfile loadProfile, double warmUpSeconds) {
		
		// add warm up period at beginning of model
		loadProfile = new LoadProfileWithWarmUp(loadProfile, warmUpSeconds);	
		
		int initalNumberOfGuests = cloudInfo.getNumberOfResources("bungee");
		System.out.println("Inital number of guests: " + initalNumberOfGuests);
		
		monitor.startMonitoring("bungee");
		RunResult runResult = runJMeter(measurementFolder, loadProfile, host, request, warmUpSeconds);
		monitor.stopMonitoring();
		
		// read start of run, start of measurement and calculate end of measurement;
		Date absoluteStartDate = runResult.getStartRunTime();
		Date absoluteEndDate = new Date(absoluteStartDate.getTime() + (long) (loadProfile.getDuration()*DateUtility.SECOND_TO_MILLI));
		System.out.println("Start Measurement: " + absoluteStartDate.toString());
		
		// get demand allocations
		DemandSeries demand = getDemand(mapping, loadProfile, absoluteStartDate, absoluteEndDate);
		// get supply allocations
		List<SupplySeries> supplyList = getSupply(host, initalNumberOfGuests, absoluteStartDate, absoluteEndDate);
		
		
		// save demand and supply in container and save it to disk
		DemandSupplyContainer container = new DemandSupplyContainer(demand, supplyList, new Date((long) (warmUpSeconds*DateUtility.SECOND_TO_MILLI)));
		container.save(new File(measurementFolder,"allocations.seriescontainer"));
		return container;
	}

	private DemandSeries getDemand(IntensityDemandMapping mapping,
			LoadProfile intensityModel, Date absoluteStartDate,
			Date absoluteEndDate) {
		// get demand events, add end event with unchanged demand at end of measurement
		List<ResourceAllocation> allocations = new LinkedList<>();
		int oldDemand = -1;
		for (ArrivalRateTuple tuple : intensityModel.getArrivalRates()) {
			int demand = mapping.getResourceDemand(tuple.getArrivalRate());
			if (demand != oldDemand) {
				oldDemand = demand;
				allocations.add(new ResourceAllocation(new Date((long) (absoluteStartDate.getTime() + tuple.getTimeStamp()*1000 - 500)), demand));
			}
		}
		DemandSeries demand = new DemandSeries(allocations);
		demand.appendEndDate(absoluteEndDate);
		demand.addTimeOffset(-absoluteStartDate.getTime());
		return demand;
	}

	private List<SupplySeries> getSupply(Host host, int initalNumberOfGuests,
			Date absoluteStartDate, Date absoluteEndDate) {
		List<SupplySeries> supplyList = new LinkedList<>();
		// If cloud supports a event history (ExtendedCloudResourceInfo) this information can be used
		addEventLogSupply(supplyList, initalNumberOfGuests, absoluteStartDate, absoluteEndDate, host);
		// polling of supply can be used always
		addPolledSupply(supplyList, absoluteStartDate, absoluteEndDate);
		return supplyList;
	}

	private void addPolledSupply(List<SupplySeries> supplyList,
			Date absoluteStartDate, Date absoluteEndDate) {
		SupplySeries monitoredSupply = monitor.getMonitoredSupply();
		monitoredSupply = (SupplySeries) monitoredSupply.extractMeasurements(absoluteStartDate, absoluteEndDate);
		monitoredSupply.addTimeOffset(-absoluteStartDate.getTime());
	    supplyList.add(monitoredSupply);
	}

	private RunResult runJMeter(File measurementFolder,
			LoadProfile loadProfile, Host host, Request request, double warmUpSeconds) {
		LoadProfileWithTimestampGeneration timestampModel = new LoadProfileWithTimestampGeneration(loadProfile);
		
		File timestampFile 		= new File(measurementFolder, "timestamps.txt");
		File responseFile 		= new File(measurementFolder, "responses.txt");
		timestampModel.createTimestampFile(timestampFile);
		jmeter.runJMeter(host, request, timestampFile, responseFile);
		RunResult runResult = new RunResult(timestampFile, responseFile, warmUpSeconds);
		runResult.save(new File(measurementFolder, "result.runresult"));
		return runResult;
	}

	private void addEventLogSupply(List<SupplySeries> supplyList,
			int initalNumberOfGuests, Date absoluteStartDate,
			Date absoluteEndDate, Host host) {
		if (cloudInfo instanceof ExtendedCloudInfo) {
			ExtendedCloudInfo extendedCloudInfo = (ExtendedCloudInfo) cloudInfo;
			// retrieve relative start / stop events
			supplyList.addAll(extendedCloudInfo.getResourceAllocations(absoluteStartDate, absoluteEndDate, host.getHostName()));
			// add absolute start and end of measurement
			for (SupplySeries supply : supplyList)
			{
				supply.prependStartDate(absoluteStartDate, initalNumberOfGuests);
				supply.appendEndDate(absoluteEndDate);
				supply.addTimeOffset(-absoluteStartDate.getTime());
			}
		}
	}
}
