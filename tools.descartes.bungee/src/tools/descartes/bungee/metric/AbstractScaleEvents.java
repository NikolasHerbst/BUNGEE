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

public abstract class AbstractScaleEvents extends Metric {

	public class ScaleEvents {
		public int scaleUpEvents = 0;
		public int scaleDownEvents = 0;
	}
	
	
	public AbstractScaleEvents(List<ResourceAllocation> demand,
			List<ResourceAllocation> supply) {
		super(demand, supply);
	}

	protected ScaleEvents getScaleEvents(List<ResourceAllocation> allocations) {
		ScaleEvents events = new ScaleEvents();
		if (allocations.size() > 0) {
			int oldAmount = allocations.get(0).getCurrentAmount();
			for (ResourceAllocation allocation : allocations) {
				if (allocation.getCurrentAmount() != oldAmount) {
					int amountDiff = allocation.getCurrentAmount() - oldAmount;
					if (amountDiff > 0)
					{
						events.scaleUpEvents += amountDiff;
					} else {
						events.scaleDownEvents -= amountDiff;
					}
					oldAmount = allocation.getCurrentAmount();
				}
			}
		}
		return events;
	}

}