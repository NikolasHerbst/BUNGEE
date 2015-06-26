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
package tools.descartes.bungee.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

//import tools.descartes.bungee.chart.ChartPDFWriter;
import tools.descartes.bungee.allocation.DemandSeries;
import tools.descartes.bungee.allocation.DemandSupplyContainer;
import tools.descartes.bungee.allocation.SupplySeries;
import tools.descartes.bungee.chart.ChartGenerator;
import tools.descartes.bungee.cloud.CloudInfo;
import tools.descartes.bungee.cloud.cloudstack.CloudSettings;
import tools.descartes.bungee.cloud.cloudstack.CloudstackControllerImpl;
import tools.descartes.bungee.cloud.cloudstack.CloudstackManagement;
import tools.descartes.bungee.config.MeasurementConfig;
import tools.descartes.bungee.loadgeneration.IPMap;
import tools.descartes.bungee.loadgeneration.RunResult;
import tools.descartes.bungee.metric.MetricToFileWriter;
import tools.descartes.dlim.generator.*;

public abstract class CloudStackExperiment extends AbstractExperiment {
	protected CloudstackControllerImpl cloudController;
	//TODO Remove completely? Was only required for FZI Setup
	//protected NetscalerController netscaler;
	
	protected CloudSettings cloudSettings;
	protected MeasurementConfig measurmentConfig;
	protected File measurementFolder;
	protected boolean evaluateExistingMeasurement;
	
	protected File expFolder;
	protected File measurementConfigFile;
	protected int width = 800;
	protected int height = 300;
	protected int heightIntensity = 400;
	protected boolean splitGraph = true;
	protected CloudInfo cloudInfo;
	
	public CloudStackExperiment(CloudstackControllerImpl cloudController) {
		super();
		this.cloudController = cloudController;
//		TODO Remove completely? Was only required for FZI Setup
//		this.netscaler = NetscalerController.getInstance();
		cloudInfo =  new CloudstackManagement(cloudController);
	}
	
	protected void waitForIpToComeOnline(String ip) {
		System.out.println("Wait for " + ip + " to become available...");
//		TODO Remove completely? Was only required for FZI Setup
//		while (!netscaler.isIpUp(ip)) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		System.out.println("Ip is online now!");
	}
	
	protected String remapIP(String ip) {
		String publicIp = IPMap.getInstance().getPublicIP(ip);
		boolean mapSuccess = true;
//		TODO Remove completely? Was only required for FZI Setup
//		boolean mapSuccess = netscaler.remapLbIp(ip, publicIp);
		System.out.println("IP remap from: " + ip + " to " + publicIp + " ok: " + mapSuccess);
		return publicIp;
	}
	
	protected void loadCloudAndMeasurmentConfig() {
		cloudSettings = CloudSettings.load(new File(expFolder,"cloudSettings.prop"));
		measurmentConfig = MeasurementConfig.load(measurementConfigFile);
		measurmentConfig.getHost().setHostName(cloudSettings.getIp());
		measurmentConfig.getHost().setPort(cloudSettings.getPublicPort());
	}

	protected void createMeasurementFolderAndSaveConfig() {
		if (!evaluateExistingMeasurement) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
			measurementFolder = new File(expFolder,formatter.format(new Date())+"_"+cloudSettings.getOffering());
			measurementFolder.mkdirs();
			cloudSettings.save(new File(measurementFolder,"cloudSettings.prop"));			
		}
	}
	
	protected void cleanUp() {
		if (!evaluateExistingMeasurement)
		{
			String groupId = cloudController.getAutoScaleGroupId(cloudSettings.getIp()); 
			if (groupId != null)
			{
				boolean success = cloudController.deleteAutoScaleVMGroupByIp(cloudSettings.getIp());
				System.out.println("Cleanup: Delete VM group " + groupId + " successful: " + success);
			}
	
			String lbId = cloudController.getLBId(cloudSettings.getIp()); 
			if (lbId != null)
			{
				boolean success = cloudController.deleteLoadBalancerRuleByIp(cloudSettings.getIp());
				System.out.println("Cleanup: Delete LB Rule " + lbId + " successful: " + success);
			}
		}
	}
	
	protected int[] loadVariations(File file) {
		int[] variations = new int[0];
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			if ((line) != null) {
				String[] variationStrings = line.split(" ");
				variations = new int[variationStrings.length];
				for (int i = 0; i < variationStrings.length; i++) {
					variations[i] = Integer.parseInt(variationStrings[i]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return variations;
	}
	
	protected void waitForCorrectResourceAmount() {
		int resources = cloudController.getNumberOfResources(cloudSettings.getIp());
		while (resources != cloudSettings.getMinInstances())
		{
			System.out.println("Wait until cloud resource amount is back at " + cloudSettings.getMinInstances() + " now: " + resources);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			resources = cloudController.getNumberOfResources(cloudSettings.getIp());
		}
	}
	
	protected void createCombinedMetricAndGraphFiles(int[] variations) {
		DemandSeries demand = null;
		Date startMeasurment = null;
		List<XYPlot> responsePlots = new LinkedList<XYPlot>();
		List<SupplySeries> supplyList = new LinkedList<SupplySeries>();
		char variant = 'a';
		for (int variation : variations)
		{
			File runFolder  = new File(measurementFolder,Integer.toString(variation));
			File file = new File(runFolder,"allocations.seriescontainer");
			if (file.exists()) {
				DemandSupplyContainer container = DemandSupplyContainer.read(file);
				demand = container.getDemandIncludingWarmUp();
				startMeasurment = container.getStartMeasuring();
				SupplySeries supply = container.getSupplyIncludingWarmUp();
				supply.setName("supply-config-"+variant);
				supplyList.add(supply);
			}
			file = new File(runFolder,"result.runresult");
			if (file.exists()) {
				RunResult result = RunResult.read(file);
				XYPlot responseTimePlot = ChartGenerator.createResponseTimePlot(result,"-config-"+variant);
				responsePlots.add(responseTimePlot);
			}
			variant++;
		}
		if (demand != null)
		{
			// create container and save it
			DemandSupplyContainer container = new DemandSupplyContainer(demand, supplyList, startMeasurment);
			container.save(new File(measurementFolder,"result.seriescontainer"));
			MetricToFileWriter.writeMetricsToFile(container, MetricToFileWriter.STD_METRIC_LIST, new File(measurementFolder,cloudSettings.getName()+"MetricResults.csv"));
			
			// create plots
			XYPlot intensityPlot = ChartGenerator.createIntensityPlot(measurmentConfig.getLoadProfile().getArrivalRates());
			List<XYPlot> allocationPlots = ChartGenerator.allocationPlots(container);
			List<XYPlot> plots = new LinkedList<XYPlot>(allocationPlots);
			plots.add(0, intensityPlot);
			plots.addAll(responsePlots);
			
			// create charts
			//JFreeChart chartAllocIntensity = ChartGenerator.allocationChart(container, measurmentConfig.getLoadProfile().getArrivalRates(), splitGraph);
			//JFreeChart chartallocations = ChartGenerator.allocationChart(container, splitGraph);
			JFreeChart chartAllocIntensityResponse = ChartGenerator.createTimeSeriesChart(plots);
//			ChartPDFWriter.writeChartToPDF(chartAllocIntensityResponse, width, height+heightIntensity, new File(measurementFolder,cloudSettings.getName()+"AllocationIntensityResponseGraph.pdf"));
			//ChartPDFWriter.writeChartToPDF(chartAllocIntensity, width, heightIntensity, new File(measurementFolder,cloudSettings.getName()+"AllocationIntensityGraph.pdf"));
			//ChartPDFWriter.writeChartToPDF(chartallocations, width, height, new File(measurementFolder,cloudSettings.getName()+"AllocationGraph.pdf"));
		}
	}
	
}
