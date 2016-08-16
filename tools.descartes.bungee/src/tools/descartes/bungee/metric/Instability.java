/*******************************************************************************
Copyright 2016 Nikolas Herbst

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

import java.util.Iterator;
import java.util.List;

import tools.descartes.bungee.allocation.ResourceAllocation;
import tools.descartes.bungee.utils.FileUtility;

public class Instability extends AbstractAreaDuration {

	static final String NAME = "instability";

	String explanation = null;
	String csvString = null;

	public Instability(List<ResourceAllocation> demand, List<ResourceAllocation> supply) {
		super(demand, supply);
	}

	@Override
	protected double evaluate() {
		double result = 0;
		int counter = 1;
		if (demand.size() > 0 && supply.size() > 0) {
			Iterator<ResourceAllocation> demandIterator = demand.iterator();
			Iterator<ResourceAllocation> supplyIterator = supply.iterator();
			ResourceAllocation lastDemandAllocation = demandIterator.next();
			ResourceAllocation lastSupplyAllocation = supplyIterator.next();
			ResourceAllocation currentDemandAllocation = lastDemandAllocation;
			ResourceAllocation currentSupplyAllocation = lastSupplyAllocation;
			while ((demandIterator.hasNext() || supplyIterator.hasNext())
					|| currentDemandAllocation.getDate().compareTo(currentSupplyAllocation.getDate()) != 0) {
				if (currentDemandAllocation.getDate().before(currentSupplyAllocation.getDate())
						|| (currentDemandAllocation.getDate().equals(currentSupplyAllocation.getDate())
								&& !supplyIterator.hasNext())) {
					lastDemandAllocation = currentDemandAllocation;
					if (demandIterator.hasNext()) {
						currentDemandAllocation = demandIterator.next();
						if (!supplyIterator.hasNext()
								&& currentSupplyAllocation.getDate().before(currentDemandAllocation.getDate())) {
							currentSupplyAllocation = new ResourceAllocation(currentDemandAllocation.getDate(),
									currentSupplyAllocation.getCurrentAmount());
						}
					} else {
						currentDemandAllocation = new ResourceAllocation(currentSupplyAllocation.getDate(),
								currentDemandAllocation.getCurrentAmount());
					}
				} else {
					lastSupplyAllocation = currentSupplyAllocation;
					if (supplyIterator.hasNext()) {
						currentSupplyAllocation = supplyIterator.next();
						if (!demandIterator.hasNext()
								&& currentDemandAllocation.getDate().before(currentSupplyAllocation.getDate())) {
							currentDemandAllocation = new ResourceAllocation(currentSupplyAllocation.getDate(),
									currentDemandAllocation.getCurrentAmount());
						}
					} else {
						currentSupplyAllocation = new ResourceAllocation(currentDemandAllocation.getDate(),
								currentSupplyAllocation.getCurrentAmount());
					}
				}
				long start = Math.max(lastDemandAllocation.getDate().getTime(),lastSupplyAllocation.getDate().getTime());
				long end = Math.min(currentDemandAllocation.getDate().getTime(), currentSupplyAllocation.getDate().getTime());
				long time = end - start;
				int delta_demand = currentDemandAllocation.getCurrentAmount() - lastDemandAllocation.getCurrentAmount();
				int delta_supply = currentSupplyAllocation.getCurrentAmount() - lastSupplyAllocation.getCurrentAmount();
				double sig_demand = Math.signum(delta_demand);
				double sig_supply = Math.signum(delta_supply);
				double plus = Math.max(sig_supply - sig_demand, 0);
				result += Math.min(plus, 1)*time;
				counter++;
			}

		}
		long Tminus1 = getDuration()*Math.max((counter-1),1)/counter;
		result = result / Tminus1;
		generateCSV(result);
		gernerateExplanantion(result);
		return result;
	}

	private void generateCSV(double result) {
		csvString = NAME + FileUtility.CSV_SPLIT_BY + result + System.lineSeparator();
		csvString += "duration [min]" + FileUtility.CSV_SPLIT_BY + getDuration() / MINUTE_TO_MILLIS
				+ System.lineSeparator();
	}

	private void gernerateExplanantion(double result) {
		explanation = NAME +": " + result + System.lineSeparator();
		explanation += "duration [min]: " + getDuration() / MINUTE_TO_MILLIS + System.lineSeparator();
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
		return NAME;
	}

}
