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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import tools.descartes.bungee.utils.DateUtility;
import tools.descartes.bungee.utils.FileUtility;

/**
 * Represents a series of consecutive allocations
 *
 */
public abstract class AllocationSeries implements Cloneable {
	
	public enum CATEGORY {
		DEMAND, 
		SUPPLY;
	}
	
	private static final String CSV_HEADER = "timestamp"+FileUtility.CSV_SPLIT_BY+"amount";
	
	
	protected List<ResourceAllocation> allocations;
	private CATEGORY category;
	
	/**
	 * Protected constructor. Takes a list of allocations and a category (demand or supply)
	 * @param allocations
	 * @param category
	 */
	protected AllocationSeries(List<ResourceAllocation> allocations,
			CATEGORY category) {
		super();
		this.allocations = allocations;
		this.category = category;
	}
	
	/**
	 * Returns a list containing all allocations
	 * @return list with allocations
	 */
	public List<ResourceAllocation> getAllocations() {
		return allocations;
	}
	
	/**
	 * Returns the category for the returned allocations (demand or supply)
	 * @return enum for demand or supply
	 */
	public CATEGORY getCategory() {
		return category;
	}
	
	/**
	 * Moves the first allocation to the start date, if the first allocation has a date > startDate
	 * @param startDate
	 */
	public void prependStartDate(Date startDate) {
		if (allocations.size() > 0)
		{
			ResourceAllocation first = allocations.get(0);
			if (first.getDate().after(startDate)) {
				allocations.remove(0);
				allocations.add(0, new ResourceAllocation(startDate,first.getCurrentAmount()));
			}
		}
	}
	
	/**
	 * Creates a new allocation at startDate with initialAmount resources and treats
	 * all successive allocations as relative resource amounts to the initial resource amount.
	 * That means, the currentAmount attribute of each allocation is increase by the initialAmount parameter.
	 * @param startDate		date for the first allocation	
	 * @param initialAmount resource amount for the first allocation
	 */
	public void prependStartDate(Date startDate, int initialAmount) {
		if (allocations.size() > 0)
		{
			ResourceAllocation first = allocations.get(0);
			if (first.getDate().after(startDate)) {
				addAmountOffset(initialAmount);
				allocations.add(0, new ResourceAllocation(startDate, initialAmount));
			}
		} else {
			allocations.add(0, new ResourceAllocation(startDate, initialAmount));
		}
	}
	
	/**
	 * Adds a new allocation at the endDate
	 * @param endDate
	 */
	public void appendEndDate(Date endDate) {
		if (allocations.size() > 0)
		{
			int lastIndex = allocations.size()-1;
			ResourceAllocation last = allocations.get(lastIndex);
			if (last.getDate().before(endDate)) {
				allocations.add(lastIndex+1, new ResourceAllocation(endDate,last.getCurrentAmount()));
			}
		}
	}
	
	/**
	 * Delays all allocation events for the specified amount of milliseconds.
	 * Parameter can me negative in order to make the allocations happen earlier.
	 * @param millis
	 */
	public void addTimeOffset(long millis) {
		for (ResourceAllocation allocation : allocations)
		{
			allocation.setDate(new Date(allocation.getDate().getTime() + millis));
		}
	}
	
	/**
	 * Adds the specified offset on to each allocation
	 * @param offset
	 */
	protected void addAmountOffset(int offset) {
		for (ResourceAllocation allocation : allocations)
		{
			allocation.setCurrentAmount(allocation.getCurrentAmount() + offset);
		}
	}
	
	/**
	 * Returns a clone of this AllocationSeries that only describes the allocation changes that happen at startOfPeriod or ealrier
	 * @param startOfPeriod
	 * @return
	 */
	public AllocationSeries removeEarlyMeasurments(Date startOfPeriod)
	{
		AllocationSeries clone = null;
		try {
			clone = (AllocationSeries) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		List<ResourceAllocation> cuttedAllocations = cutEarlyMeasurments(startOfPeriod, allocations);
		clone.allocations = cuttedAllocations;
		return clone;
	}

	private List<ResourceAllocation> cutEarlyMeasurments(Date startOfPeriod, List<ResourceAllocation> allocations) {
		List<ResourceAllocation> cuttedAllocations = new LinkedList<ResourceAllocation>();
		ResourceAllocation lastAllocationBeforeStart = null;
		for (ResourceAllocation allocation : allocations)
		{
			if (DateUtility.beforeOrEqual(startOfPeriod, allocation.getDate())) {
				cuttedAllocations.add(allocation.clone());
			} else {
				lastAllocationBeforeStart = allocation;
			}
		}
		// element at beginning
		if (lastAllocationBeforeStart != null) {
			cuttedAllocations.add(0, new ResourceAllocation(startOfPeriod, lastAllocationBeforeStart.getCurrentAmount()));
		}
		return cuttedAllocations;
	}
	
	/**
	 * Returns a clone that only describes allocation changes that occur before or exactly at endOfPeriod
	 * @param endOfPeriod
	 * @return
	 */
	public AllocationSeries removeLateMeasurments(Date endOfPeriod)
	{
		AllocationSeries clone = null;
		try {
			clone = (AllocationSeries) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		List<ResourceAllocation> cuttedAllocations = cutLateAllocations(endOfPeriod, allocations);
		clone.allocations = cuttedAllocations;
		return clone;
	}
	
	/**
	 * Returns a clone that only describes the allocation changes that occur between (inclusive) startOfPeriod and endOfPeriod
	 * @param startOfPeriod
	 * @param endOfPeriod
	 * @return
	 */
	public AllocationSeries extractMeasurements(Date startOfPeriod, Date endOfPeriod)
	{
		AllocationSeries clone = null;
		try {
			clone = (AllocationSeries) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		List<ResourceAllocation> cuttedAllocations = cutEarlyMeasurments(startOfPeriod, allocations);
		cuttedAllocations = cutLateAllocations(endOfPeriod, cuttedAllocations);
		clone.allocations = cuttedAllocations;
		return clone;
	}

	private List<ResourceAllocation> cutLateAllocations(Date endOfPeriod, List<ResourceAllocation> allocations) {
		List<ResourceAllocation> cuttedAllocations = new LinkedList<ResourceAllocation>();
		for (ResourceAllocation allocation : allocations)
		{
			if (DateUtility.afterOrEqual(endOfPeriod, allocation.getDate())) {
				cuttedAllocations.add(allocation.clone());
			}
		}
		if (cuttedAllocations.size() > 0)
		{
			ResourceAllocation last = cuttedAllocations.get(cuttedAllocations.size()-1);
			if (last.getDate().before(endOfPeriod)) {
				cuttedAllocations.add(new ResourceAllocation(endOfPeriod, last.getCurrentAmount()));
			}
		}
		return cuttedAllocations;
	}
	
	
	/**
	 * Creates a clone
	 */
	public AllocationSeries clone() throws CloneNotSupportedException {
		AllocationSeries clone = (AllocationSeries) super.clone();
		clone.category = this.category;
		clone.allocations = new LinkedList<ResourceAllocation>();
		for (ResourceAllocation allocation : allocations)
		{
			clone.allocations.add(allocation.clone());
		}
        return clone;
    }

	/**
	 * Reads a list of allocations from a file
	 * @param file
	 * @return
	 */
	public static List<ResourceAllocation> readAllocations(File file) {
		List<ResourceAllocation> allocations = new LinkedList<ResourceAllocation>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			//jump over header line
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] allocationString = line.split(FileUtility.CSV_SPLIT_BY);
				Date date		= new Date(Long.parseLong(allocationString[0]));
				int allocation 	= Integer.parseInt(allocationString[1]);
				allocations.add(new ResourceAllocation(date, allocation));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return allocations;
	}

	/**
	 * Saves a list of allocations into a file
	 * @param allocations
	 * @param file
	 * @return
	 */
	protected static boolean writeAllocationsToFile(
			List<ResourceAllocation> allocations, File file) {
		boolean success = false;
		try {
			PrintWriter scheduledVMsFileWriter = new PrintWriter(file, FileUtility.ENOCDING);
			scheduledVMsFileWriter.println(AllocationSeries.CSV_HEADER);
			for (ResourceAllocation allocation : allocations)
			{
				scheduledVMsFileWriter.println(allocation.getDate().getTime() + FileUtility.CSV_SPLIT_BY + allocation.getCurrentAmount());
			}
			scheduledVMsFileWriter.close();
			success = true;
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * Checks if the file is a correct allocation file.
	 * @param file
	 * @return
	 */
	public static boolean isAllocationFile(File file)
	{
		boolean isAllocationFile = false;
		if (file.exists() && file.isFile() && file.getName().endsWith(".csv"))
		{
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
				String line = br.readLine();
				if (line != null && line.equals(AllocationSeries.CSV_HEADER)) {
					isAllocationFile = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return isAllocationFile;
	}
}


