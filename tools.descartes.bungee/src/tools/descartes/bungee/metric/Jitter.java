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
package tools.descartes.bungee.metric;

import java.util.List;

import tools.descartes.bungee.allocation.ResourceAllocation;
import tools.descartes.bungee.utils.FileUtility;

public class Jitter extends AbstractScaleEvents {

	String explanation = null;
	String csvString = null;
	
	public Jitter(List<ResourceAllocation> demand,
			List<ResourceAllocation> supply) {
		super(demand, supply);
	}
	
	@Override
	protected double evaluate() {
		ScaleEvents demandEvents = getScaleEvents(demand);
		ScaleEvents supplyEvents = getScaleEvents(supply);
		long demandEventSum = demandEvents.scaleDownEvents + demandEvents.scaleUpEvents;
		long supplyEventSum = supplyEvents.scaleDownEvents + supplyEvents.scaleUpEvents;
		double result = ((double) (supplyEventSum - demandEventSum)) / getDuration() * Metric.MINUTE_TO_MILLIS;
		generateExplanation(demandEventSum, supplyEventSum, result);
		generateCSV(demandEventSum, supplyEventSum, result);
		return result;
	}

	private void generateCSV(long demandEvents, long supplyEvents, double result) {
		csvString = "jitter" + FileUtility.CSV_SPLIT_BY + result + System.lineSeparator();
		csvString += "demand_events" + FileUtility.CSV_SPLIT_BY + demandEvents  + System.lineSeparator();
		csvString += "supply_events" + FileUtility.CSV_SPLIT_BY + supplyEvents + System.lineSeparator();
		csvString += "duration [min]" + FileUtility.CSV_SPLIT_BY + getDuration() / Metric.MINUTE_TO_MILLIS + System.lineSeparator();
	}

	private void generateExplanation(long demandEvents, long supplyEvents, double result) {
		explanation = "jitter: " + result + System.lineSeparator();
		explanation += "demand_events: " + demandEvents  + System.lineSeparator();
		explanation += "supply_events: " + supplyEvents + System.lineSeparator();
		explanation += "duration [min]: "+ getDuration() / Metric.MINUTE_TO_MILLIS + System.lineSeparator();
	}

	@Override
	protected String textShort() {
		if (explanation == null) {
			evaluate();
		}
		return explanation;
	}

	@Override
	protected String csvString() {
		if (csvString == null) {
			evaluate();
		}
		return csvString;
	}

	@Override
	public String getName() {
		return "jitter";
	}
}
