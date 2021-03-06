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

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import tools.descartes.bungee.allocation.ResourceAllocation;
import tools.descartes.bungee.metric.UnderprovisionTimeshare;



public class UnderprovisionTimeshareTest extends AbstractMetricTest {
	
	@Test
	public void testComplexPerfectMatch() {
		genereateComplexPerfectMatch();
		UnderprovisionTimeshare metric = new UnderprovisionTimeshare(demand, supply);
		assertEquals(0, metric.result(), DOUBLE_PRECISION);
	}
	
	@Test
	public void testMetricEqualDemandSupplyLength() {
		genereateComplexDemandAndSupply();
		demand.add(new ResourceAllocation(new Date(20),4));
		UnderprovisionTimeshare metric = new UnderprovisionTimeshare(demand, supply);
		assertEquals(6.0f/20, metric.result(), DOUBLE_PRECISION);
	}
	
	@Test
	public void testPermanentOverprovisioning() {
		generatePermanentOverprovisioning();
		UnderprovisionTimeshare metric = new UnderprovisionTimeshare(demand, supply);
		assertEquals(0, metric.result(), DOUBLE_PRECISION);
	}
	
	@Test
	public void testPermanentUnderprovisioning() {
		generatePermanentUnderprovisioning();
		UnderprovisionTimeshare metric = new UnderprovisionTimeshare(demand, supply);
		assertEquals(1, metric.result(), DOUBLE_PRECISION);
	}
	
	@Test
	public void testMetricDifferentDemandSupplyLength() {
		genereateComplexDemandAndSupply();
		UnderprovisionTimeshare metric = new UnderprovisionTimeshare(demand, supply);
		assertEquals(6.0f/20, metric.result(), DOUBLE_PRECISION);
	}

}
