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

package tools.descartes.bungee.allocation;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a list of consecutive demand allocations
 *
 */
public class DemandSeries extends AllocationSeries {

	/**
	 * Creates a DemandSeries containing the given list of allocations
	 * @param allocations
	 */
	public DemandSeries(List<ResourceAllocation> allocations) {
		super(allocations, CATEGORY.DEMAND);
	}

	/**
	 * Creates an empty DemandSeries
	 */
	public DemandSeries() {
		super(new LinkedList<ResourceAllocation>(), CATEGORY.DEMAND);
	}

	/**
	 * Reads a DemandSeries from a file
	 * @param file
	 * @return
	 */
	public static DemandSeries read(File file) {
		DemandSeries demandSeries = new DemandSeries(AllocationSeries.readAllocations(file));
		return demandSeries;
	}
}
