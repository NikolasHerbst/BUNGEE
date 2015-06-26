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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import tools.descartes.bungee.allocation.SupplySeries.TYPE;
import tools.descartes.bungee.utils.FileUtility;

/**
 * Container Object that is result of each elasticity measurement run.
 */
public class DemandSupplyContainer {
	
	private static final TYPE STANDARD_SUPPLY_TYPE = SupplySeries.TYPE.LB_RULE_ADAPTION;
	private DemandSeries fullDemand;
	private List<SupplySeries> fullSupplyList;
	private DemandSeries demand;
	private List<SupplySeries> supplyList;
	private Date startMeasuring;
	
	/**
	 * Creates a container with an empty DemandSeries and several SupplySeries
	 * @param supplyList
	 */
	public DemandSupplyContainer(List<SupplySeries> supplyList) {
		this(new DemandSeries(), supplyList);
	}
	
	/**
	 * Creates a container with one DemandSeries
	 * @param demand
	 * @param supplyList
	 */
	public DemandSupplyContainer(DemandSeries demand,
			List<SupplySeries> supplyList) {
		this(demand, supplyList, new Date(0));
	}
	
	/**
	 * Creates a container that contains a DemandSeries and several SupplySeries and saves the start of the actual
	 * measurement. Allocations that happened before startMeasuring are assumed to happen during the warmUp Period.
	 * @param demand
	 * @param supplyList
	 * @param startMeasuring
	 */
	public DemandSupplyContainer(DemandSeries demand,
			List<SupplySeries> supplyList, Date startMeasuring) {
		super();
		Collections.sort(supplyList);
		this.fullDemand = demand;
		this.fullSupplyList = supplyList;
		this.startMeasuring = startMeasuring;
		this.demand = (DemandSeries) demand.removeEarlyMeasurments(startMeasuring);
		this.demand.addTimeOffset(-startMeasuring.getTime());
		this.supplyList = new LinkedList<SupplySeries>();
		for (SupplySeries fullSupply : supplyList)
		{
			SupplySeries cuttedSupply = (SupplySeries) fullSupply.removeEarlyMeasurments(startMeasuring);
			cuttedSupply.addTimeOffset(-startMeasuring.getTime());
			this.supplyList.add(cuttedSupply);
			
		}
	}
	
	public DemandSeries getDemand() {
		return demand;
	}
	
	public DemandSeries getDemandIncludingWarmUp() {
		return fullDemand;
	}
	
	public SupplySeries getSupply() {
		SupplySeries supply = supplyList.get(0);
		for (SupplySeries currentSupply : supplyList) {
			if (currentSupply.getType() == STANDARD_SUPPLY_TYPE) {
				supply = currentSupply;
			}
		}
		return supply;
	}
	
	public SupplySeries getSupplyIncludingWarmUp() {
		SupplySeries supply = fullSupplyList.get(0);
		for (SupplySeries currentSupply : fullSupplyList) {
			if (currentSupply.getType() == STANDARD_SUPPLY_TYPE) {
				supply = currentSupply;
			}
		}
		return supply;
	}
	
	public List<SupplySeries> getAllSupplies() {
		return supplyList;
	}
	
	public Date getStartMeasuring() {
		return startMeasuring;
	}

	/**
	 * Saves the container into a file. The Demand- and AllocationSeries that exist in the container are saved
	 * in a folder that has the same name as the file (without extension)
	 * @param file
	 */
	public void save(File file) 
	{
		Properties properties = new Properties();
		File folder = new File(FileUtility.getFileNameWithoutExtension(file));
		folder.mkdir();
		File relativePath = new File(folder.getName());
		String name = "demand.csv";
		File demandFile = new File(folder,name);
		AllocationSeries.writeAllocationsToFile(fullDemand.getAllocations(), demandFile);
		properties.setProperty("startMeasuring", Long.toString(startMeasuring.getTime()));
		properties.setProperty("demand", new File(relativePath, name).toString());
		for (SupplySeries supply : fullSupplyList)
		{
			name = supply.getName().toLowerCase() + ".csv";
			File supplyFile = new File(folder,name);
			AllocationSeries.writeAllocationsToFile(supply.getAllocations(), supplyFile);
			properties.setProperty("supply."+supply.getName(),  new File(relativePath, name).toString());
		}
		FileUtility.saveProperties(properties, file);
	}

	
	/**
	 * Checks if the file is a container file
	 * @param file
	 * @return
	 */
	public static boolean isSeriesContainer(File file) 
	{
		boolean isSeriesContainer = false;
		if (file.exists() && file.isFile() && file.getName().endsWith(".seriescontainer"))
		{
			isSeriesContainer = true;
		}
		return isSeriesContainer;
	}
	
	/**
	 * Reads a container from a file
	 * @param file
	 * @return
	 */
	public static DemandSupplyContainer read(File file) 
	{
		Properties properties = FileUtility.loadProperties(file);
		
		List<SupplySeries> supplyList = new LinkedList<SupplySeries>();
		DemandSeries demand = new DemandSeries();
		Date startMeasuring = new Date(0);
		
		Set<Object> keySet = properties.keySet();
		for (Object o : keySet)
		{
			String key = (String) o;
			File parentFile = file.getParentFile();
			if (key.equals("demand")) {
				File path = new File(parentFile,properties.getProperty(key));
				demand = DemandSeries.read(path);
			} else if (key.startsWith("supply.")) {
				File path = new File(parentFile,properties.getProperty(key));
				supplyList.add(SupplySeries.read(path, key.substring(7, key.length())));
			} else if (key.startsWith("startMeasuring")) {
				startMeasuring = new Date(Long.parseLong(properties.getProperty(key)));
			}
		}
		Collections.sort(supplyList);
		return new DemandSupplyContainer(demand, supplyList, startMeasuring);
		
	}
}
