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

import java.util.List;

import tools.descartes.bungee.allocation.ResourceAllocation;

public class Power extends AbstractAreaDuration {
	static final String NAME = "estimatedWatts";
	
	String explanation = null;
	String csvString = null;
	
	public Power(List<ResourceAllocation> demand, List<ResourceAllocation> supply) {
		super(demand, supply);
	}

	@Override
	protected double evaluate() {
		// TODO Auto-generated method stub
		return 0;
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
