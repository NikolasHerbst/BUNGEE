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

import java.util.Iterator;
import java.util.List;

import tools.descartes.bungee.allocation.ResourceAllocation;

public abstract class AbstractRelativeAreaDuration extends Metric {

	class Provisioning {
		double amountUnder = 0;
		double amountOver = 0;
		double timeUnder = 0;
		double timeOver = 0;
	}

	public AbstractRelativeAreaDuration(List<ResourceAllocation> demand,
			List<ResourceAllocation> supply) {
		super(demand, supply);
	}

	protected Provisioning getProvisioning() {
		Provisioning provisioning = new Provisioning();
		if (demand.size() > 0 && supply.size() > 0) {
			Iterator<ResourceAllocation> demandIterator = demand.iterator();
			Iterator<ResourceAllocation> supplyIterator = supply.iterator();
			ResourceAllocation lastDemandAllocation = demandIterator.next();
			ResourceAllocation lastSupplyAllocation = supplyIterator.next();
			ResourceAllocation currentDemandAllocation = lastDemandAllocation;
			ResourceAllocation currentSupplyAllocation = lastSupplyAllocation;
			while ((demandIterator.hasNext() || supplyIterator.hasNext()) || currentDemandAllocation.getDate().compareTo(currentSupplyAllocation.getDate()) != 0)
			{
				if (currentDemandAllocation.getDate().before(currentSupplyAllocation.getDate()) || (currentDemandAllocation.getDate().equals(currentSupplyAllocation.getDate()) && !supplyIterator.hasNext()))
				{
					lastDemandAllocation = currentDemandAllocation;
					if (demandIterator.hasNext()) {
						currentDemandAllocation = demandIterator.next();
						if (!supplyIterator.hasNext() && currentSupplyAllocation.getDate().before(currentDemandAllocation.getDate())) {
							currentSupplyAllocation = new ResourceAllocation(currentDemandAllocation.getDate(), currentSupplyAllocation.getCurrentAmount());
						}
					} else {
						currentDemandAllocation = new ResourceAllocation(currentSupplyAllocation.getDate(), currentDemandAllocation.getCurrentAmount());
					}
				} else {
					lastSupplyAllocation = currentSupplyAllocation;
					if (supplyIterator.hasNext()) {
						currentSupplyAllocation = supplyIterator.next();
						if (!demandIterator.hasNext()  && currentDemandAllocation.getDate().before(currentSupplyAllocation.getDate())) {
							currentDemandAllocation = new ResourceAllocation(currentSupplyAllocation.getDate(), currentDemandAllocation.getCurrentAmount());
						}
					} else {
						currentSupplyAllocation = new ResourceAllocation(currentDemandAllocation.getDate(), currentSupplyAllocation.getCurrentAmount());
					}
				}

				long start = Math.max(lastDemandAllocation.getDate().getTime(),lastSupplyAllocation.getDate().getTime());
				long end = Math.min(currentDemandAllocation.getDate().getTime(), currentSupplyAllocation.getDate().getTime());
				long height = lastSupplyAllocation.getCurrentAmount() - lastDemandAllocation.getCurrentAmount();
				long time = end - start;
				long area = time * height;
				if (height > 0)
				{
					provisioning.timeOver += time;
					provisioning.amountOver += area;
				} else if (height < 0) {
					provisioning.timeUnder += time;
					provisioning.amountUnder -= area;
					
				}
			}
			
		}
		return provisioning;
	}
}