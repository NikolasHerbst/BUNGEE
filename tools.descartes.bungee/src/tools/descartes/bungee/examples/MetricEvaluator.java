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
import java.util.List;

import tools.descartes.bungee.allocation.DemandSupplyContainer;
import tools.descartes.bungee.allocation.ResourceAllocation;
import tools.descartes.bungee.metric.Jitter;
import tools.descartes.bungee.metric.MetricToFileWriter;
import tools.descartes.bungee.metric.OverprovisionAccuracy;
import tools.descartes.bungee.metric.OverprovisionTimeshare;
import tools.descartes.bungee.metric.UnderprovisionAccuracy;
import tools.descartes.bungee.metric.UnderprovisionTimeshare;
import tools.descartes.bungee.utils.FileUtility;

public class MetricEvaluator {

	public static void main(String[] args) {
		DemandSupplyContainer benchedSeries = DemandSupplyContainer.read(new File(FileUtility.FILE_LOCATION, "measurement/2014.07.01_11.23.13.measurement-oneDay-6h-aws.small.simple2_65p_60s_40p_60s_60qs_add2/allocations.seriescontainer"));
		List<ResourceAllocation> demand = benchedSeries.getDemand().getAllocations();
		List<ResourceAllocation> supply = benchedSeries.getSupply().getAllocations();
		
		OverprovisionAccuracy overprovisionAccuracyMetric = new OverprovisionAccuracy(demand, supply);
		UnderprovisionAccuracy underprovisionAccuracy = new UnderprovisionAccuracy(demand, supply);
		UnderprovisionTimeshare underprovisionTimeshareMetric = new UnderprovisionTimeshare(demand, supply);
		OverprovisionTimeshare overprovisionTimeshareMetric = new OverprovisionTimeshare(demand, supply);
		Jitter jitter = new Jitter(demand, supply);
		
		System.out.println(jitter);
		System.out.println(overprovisionAccuracyMetric);
		System.out.println(underprovisionAccuracy);
		System.out.println(overprovisionTimeshareMetric);
		System.out.println(underprovisionTimeshareMetric);
		
		MetricToFileWriter.writeMetricsToFile(benchedSeries,MetricToFileWriter.STD_METRIC_LIST, new File("metrics.csv"));
	}

}
