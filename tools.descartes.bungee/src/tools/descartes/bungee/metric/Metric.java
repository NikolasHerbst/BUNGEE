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

public abstract class Metric {
	protected List<ResourceAllocation> demand;
	protected List<ResourceAllocation> supply;
	
	private Double result = null;
	private String textShort = null;
	private String csvString = null;
	static final int MINUTE_TO_MILLIS = 1000 * 60;
	static final String NAME = "Test";
	
	protected abstract double evaluate();
	protected abstract String textShort();
	protected abstract String csvString();
	
	
	
	protected Metric(List<ResourceAllocation> demand,
			List<ResourceAllocation> supply) {
		assert(demand.size() > 0 && supply.size() > 0);
		assert(demand.get(0).getDate().compareTo(supply.get(0).getDate())) == 0;
		
		this.demand = demand;
		this.supply = supply;
	}
	
	protected long getDuration() {
		long startDemand = Long.MAX_VALUE;
		long startSupply = Long.MAX_VALUE;
		long endDemand = 0;
		long endSupply = 0;
		if (demand.size() > 0) 
		{
			startDemand = demand.get(0).getDate().getTime();
			endDemand = demand.get(demand.size()-1).getDate().getTime();
		}
		if (supply.size() > 0) 
		{
			startSupply = supply.get(0).getDate().getTime();
			endSupply = supply.get(supply.size()-1).getDate().getTime();
		}
		long duration = Math.max(endDemand, endSupply) - Math.min(startDemand, startSupply);
		if (duration < 0)
		{
			duration = 0;
		}
		return duration;
	}
	
	public double result()
	{
		if (result == null) {
			result = evaluate();
		}
		return result;
	}
	
	public String toString()
	{
		if (textShort == null) {
			textShort = textShort();
		}
		return textShort;
	}
	
	public String toCSV()
	{
		if (csvString == null) {
			csvString = csvString();
		}
		return csvString;
	}
	
	public abstract String getName();
}
