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

package tools.descartes.bungee.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import tools.descartes.bungee.utils.FileUtility;

public class IntensityDemandMapping {
	private static final String CSV_HEADER = "maxIntensity"+FileUtility.CSV_SPLIT_BY+"resourceAmount";

	public class IntensityResourcePair {
		public double maxIntensity;
		public int resourceAmount;
		
		IntensityResourcePair(double maxIntensity, int resourceAmount) {
			super();
			this.maxIntensity = maxIntensity;
			this.resourceAmount = resourceAmount;
		}
	}
	
	private List<IntensityResourcePair> function;
	
	public IntensityDemandMapping() {
		function = new LinkedList<IntensityResourcePair>();
	}
	
	public void addMaxIntensityForResourceAmount(double intensity, int resources) {
		function.add(new IntensityResourcePair(intensity, resources));
	}
	
	public int getMaxAvailableResources()
	{
		int resources = 0;
		if (function.size() != 0)
		{
			resources = function.get(function.size()-1).resourceAmount;
		}
		return resources;
	}
	
	public double getMaxIntensity()  {
		double intensity = 0;
		if (function.size() != 0)
		{
			intensity = function.get(function.size()-1).maxIntensity;
		}
		return intensity;
	}
	
	public double getMaxIntensity(int resources)  {
		double maxIntensity = 0;
		for (IntensityResourcePair pair : function) {
			if (pair.resourceAmount >= resources)
			{
				maxIntensity = pair.maxIntensity;
				break;
			}
		}
		return maxIntensity;
	}
	
	public int getResourceDemand(double intensity)  {
		int demand = getMaxAvailableResources();
		for (IntensityResourcePair pair : function) {
			if (intensity <= pair.maxIntensity)
			{
				demand = pair.resourceAmount;
				break;
			}
		}
		return demand;
	}
	
	public List<IntensityResourcePair> getMappingList()  {
		return function;
	}
	
	public boolean save(File file) {
		boolean success = false;
		try {
			PrintWriter scheduledVMsFileWriter = new PrintWriter(file, FileUtility.ENOCDING);
			scheduledVMsFileWriter.println(CSV_HEADER);
			for (IntensityResourcePair pair : function)
			{
				scheduledVMsFileWriter.println(pair.maxIntensity + FileUtility.CSV_SPLIT_BY + pair.resourceAmount);
			}
			scheduledVMsFileWriter.close();
			success = true;
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return success;
	}
	
	public static IntensityDemandMapping read(File file) {
		IntensityDemandMapping mappingFunction = new IntensityDemandMapping();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			//jump over header line
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] pair = line.split(FileUtility.CSV_SPLIT_BY);
				mappingFunction.addMaxIntensityForResourceAmount(Double.parseDouble(pair[0]),Integer.parseInt(pair[1]));
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
		return mappingFunction;
	}
	
	public boolean isValid() {
		return function.size() != 0;
	}
	
	public static boolean isMappingFunction(File file) {
		boolean isSeriesContainer = false;
		if (file.exists() && file.isFile() && file.getName().endsWith(".mapping"))
		{
			isSeriesContainer = true;
		}
		return isSeriesContainer;
	}
}
