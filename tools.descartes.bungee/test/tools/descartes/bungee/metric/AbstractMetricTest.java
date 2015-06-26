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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;

import tools.descartes.bungee.allocation.ResourceAllocation;

public class AbstractMetricTest {

	protected List<ResourceAllocation> demand;
	protected List<ResourceAllocation> supply;
	
	protected static final double DOUBLE_PRECISION = 0.000001;

	public AbstractMetricTest() {
		super();
	}

	@Before
	public void emptyLists() {
		demand = new LinkedList<ResourceAllocation>();
		supply = new LinkedList<ResourceAllocation>();
	}
	
	public void genereateComplexDemandAndSupply() {
		demand.add(new ResourceAllocation(new Date(0),1));
		demand.add(new ResourceAllocation(new Date(7),5));
		demand.add(new ResourceAllocation(new Date(16),3));
		demand.add(new ResourceAllocation(new Date(17),2));
		demand.add(new ResourceAllocation(new Date(19),4));
		supply = createComplexSeries();
	}
	
	public void genereateComplexPerfectMatch() {
		demand = createComplexSeries();
		supply = createComplexSeries();
	}
	
	public void generatePermanentUnderprovisioning() {
		supply = createComplexSeries();
		for (ResourceAllocation supAlloc : supply) {
			demand.add(new ResourceAllocation(supAlloc.getDate(), supAlloc.getCurrentAmount() + 1));
		}
	}
	
	public void generatePermanentOverprovisioning() {
		demand = createComplexSeries();
		for (ResourceAllocation supAlloc : demand) {
			supply.add(new ResourceAllocation(supAlloc.getDate(), supAlloc.getCurrentAmount() + 1));
		}
	}

	private List<ResourceAllocation> createComplexSeries() {
		List<ResourceAllocation> series = new LinkedList<ResourceAllocation>();
		series.add(new ResourceAllocation(new Date(0),1));
		series.add(new ResourceAllocation(new Date(2),2));
		series.add(new ResourceAllocation(new Date(3),1));
		series.add(new ResourceAllocation(new Date(5),3));
		series.add(new ResourceAllocation(new Date(9),7));
		series.add(new ResourceAllocation(new Date(11),6));
		series.add(new ResourceAllocation(new Date(12),8));
		series.add(new ResourceAllocation(new Date(14),2));
		series.add(new ResourceAllocation(new Date(20),2));
		return series;
	}

}