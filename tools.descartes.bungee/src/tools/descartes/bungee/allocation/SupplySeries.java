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
import java.util.List;

public class SupplySeries extends AllocationSeries implements Comparable<SupplySeries> {

	/*
	 * Types of supply events
	 */
	public enum TYPE {
		MONITORED,
		VM_SCHEDULED,
		VM_COMPLETED,
		LB_RULE_ADAPTION, 
		UNSPECIFIED;
	}
	
	private TYPE type;
	private String name;
	
	/**
	 * Creates a SupplySeries containing the specified allocations assigns a specific supply type.
	 * @param allocations
	 * @param type
	 */
	public SupplySeries(List<ResourceAllocation> allocations, TYPE type) {
		super(allocations, CATEGORY.SUPPLY);
		this.type = type;
		this.name = type.toString();
	}
	
	/**
	 * Creates a SupplySeries containing the specified allocations assigns a name.
	 * If name matches a specified type, the type attribute is set accordingly.
	 * @param allocations
	 * @param type
	 */
	public SupplySeries(List<ResourceAllocation> allocations, String name) {
		super(allocations, CATEGORY.SUPPLY);
		setTypeByName(name);
		this.name = name;
	}

	private void setTypeByName(String name) {
		try {
			this.type = SupplySeries.TYPE.valueOf(name);
		} catch (Exception e)
		{
			this.type = TYPE.UNSPECIFIED;
		}
	}
	
	public TYPE getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		setTypeByName(name);
	}

	@Override
	public int compareTo(SupplySeries other) {
		int typeCompare = type.compareTo(other.type);
		if (typeCompare != 0)
		{
			return typeCompare;
		} else {
			return name.compareTo(other.name);
		}
		
	}

	/**
	 * Reads a SupplySeries from a file.
	 * @param file
	 * @param name
	 * @return
	 */
	public static SupplySeries read(File file, String name) {
		return new SupplySeries(AllocationSeries.readAllocations(file), name);
	}

}
