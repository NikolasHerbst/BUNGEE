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

package tools.descartes.bungee.utils;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import tools.descartes.bungee.loadgeneration.AbstractResponse;

public class DiffUtil {

	public static <T> Stats statisticsForDiffs(List<T> objects) {
		DescriptiveStatistics statistics = createDescriptiveStatistics(objects);

		Stats stats = new Stats();
		// Compute some statistics
		stats.mean = statistics.getMean();
		stats.std = statistics.getStandardDeviation();
		stats.max = Math.max(statistics.getMax(), -statistics.getMin());
		return stats;
	}

	public static <T> DescriptiveStatistics createDescriptiveStatistics(
			List<T> objects) {
		// Get a DescriptiveStatistics instance
		DescriptiveStatistics statistics = new DescriptiveStatistics();
		// Add the data from the array
		for( T object : objects) {
			if(object instanceof Long)
			{
				statistics.addValue((Long) object);
			} else if (object instanceof Double)
			{
				statistics.addValue((Double) object);
			} else if (object instanceof AbstractResponse) {
				statistics.addValue(((AbstractResponse) object).getResponseTime());
			}
		}
		return statistics;
	}
	
	public static <T> StatsPercentile statisticsForDiffs(List<T> objects, double percent) {
		// Get a DescriptiveStatistics instance
		DescriptiveStatistics statistics = new DescriptiveStatistics();
		// Add the data from the array
		for( T object : objects) {
			if(object instanceof Long)
			{
				statistics.addValue((Long) object);
			} else if (object instanceof Double)
			{
				statistics.addValue((Double) object);
			} else if (object instanceof AbstractResponse) {
				statistics.addValue(((AbstractResponse) object).getResponseTime());
			}
		}

		StatsPercentile stats = new StatsPercentile();
		// Compute some statistics
		stats.mean = statistics.getMean();
		stats.std = statistics.getStandardDeviation();
		stats.max = Math.max(statistics.getMax(), -statistics.getMin());
		stats.percent = percent;
		stats.percentile = statistics.getPercentile(percent);
		return stats;
	}
	


}
