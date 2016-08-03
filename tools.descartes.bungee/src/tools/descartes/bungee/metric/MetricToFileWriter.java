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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import tools.descartes.bungee.allocation.DemandSupplyContainer;
import tools.descartes.bungee.allocation.SupplySeries;
import tools.descartes.bungee.utils.FileUtility;

public class MetricToFileWriter {
	public static List<Class<? extends Metric>> STD_METRIC_LIST = Arrays.asList(OverprovisionAccuracy.class, RelativeOverprovisionAccuracy.class, UnderprovisionAccuracy.class, RelativeUnderprovisionAccuracy.class, 
			OverprovisionTimeshare.class, UnderprovisionTimeshare.class, Jitter.class, Instability.class, Power.class);
	
	private static DecimalFormat format = new DecimalFormat( "#,###,###,##0.000" );
	
	public static void writeMetricsToFile(DemandSupplyContainer container, List<Class<? extends Metric>>  metrics, File file) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(file, FileUtility.ENOCDING);
			String header = generateMetricHeader(metrics);
			writer.println(header);
			for (SupplySeries supply : container.getAllSupplies()) {
				writer.print(supply.getName());
				for (Class<? extends Metric> Metric : metrics)
				{
					Constructor<? extends Metric> cons = Metric.getConstructor(List.class,List.class);
					Metric metric = cons.newInstance(container.getDemand().getAllocations(), supply.getAllocations());
					writer.print(FileUtility.CSV_SPLIT_BY + format.format(metric.result()));
					System.out.println(metric);
				}
				writer.println();
			}

			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private static String generateMetricHeader(List<Class<? extends Metric>>  metrics) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		String header = "";
		for (Class<? extends Metric> Metric : metrics)
		{
			Constructor<? extends Metric> cons = Metric.getConstructor(List.class,List.class);
			Metric metric = cons.newInstance(null, null);
			header +=  FileUtility.CSV_SPLIT_BY + metric.getName();
		}
		return header;
	}
}
