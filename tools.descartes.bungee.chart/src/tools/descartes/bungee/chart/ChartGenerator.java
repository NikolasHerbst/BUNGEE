/*******************************************************************************
* Copyright (c) 2014 Andreas Weber, Nikolas Herbst
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/

package tools.descartes.bungee.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.util.RelativeDateFormat;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import tools.descartes.bungee.allocation.AllocationSeries;
import tools.descartes.bungee.allocation.DemandSupplyContainer;
import tools.descartes.bungee.allocation.ResourceAllocation;
import tools.descartes.bungee.allocation.SupplySeries;
import tools.descartes.bungee.analysis.IntensityDemandMapping;
import tools.descartes.bungee.analysis.IntensityDemandMapping.IntensityResourcePair;
import tools.descartes.bungee.loadgeneration.AbstractResponse;
import tools.descartes.bungee.loadgeneration.JMeterResponse;
import tools.descartes.bungee.loadgeneration.RunResult;
import tools.descartes.bungee.utils.DateUtility;
import tools.descartes.dlim.generator.ArrivalRateTuple;

public class ChartGenerator {

	private static final Color VARIANT_A_COLOR = new Color(34,139,34);
	private static final Color VARIANT_B_COLOR = new Color(255,140,0);
	private static final Color VARIANT_C_COLOR = Color.RED;
	private static final Color VARIANT_D_COLOR = Color.MAGENTA;
	private static final int STROKE_WIDTH = 1;
	private static Date referenceDate = new Date(0);

	public static void showChart(JFreeChart chart, String title) {
		final ChartFrame frame = new ChartFrame(title, chart);
		frame.setVisible(true);
	}

	public static JFreeChart allocationChart(DemandSupplyContainer container, boolean splitUp) { 
		if (splitUp) {
			List<XYPlot> plots = allocationPlots(container);
			return createTimeSeriesChart(plots);
		} else {
			return createTimeSeriesChart(allocationPlot(container));
		}
	}

	public static List<XYPlot> allocationPlots(DemandSupplyContainer container) {
		List<XYPlot> plots = new LinkedList<XYPlot>();
		plots.add(allocationPlot(container.getDemand()));
		for (SupplySeries supply : container.getAllSupplies())
		{
			plots.add(allocationPlot(supply));
		}
		return plots;
	}
	
	public static JFreeChart allocationChart(DemandSupplyContainer container, final List<ArrivalRateTuple> intensities, boolean splitUp) { 
		List<XYPlot> plots = new LinkedList<XYPlot>();
		plots.add(createIntensityPlot(intensities));
		if (splitUp) {
			plots.add(allocationPlot(container.getDemand()));
			for (SupplySeries supply : container.getAllSupplies())
			{
				plots.add(allocationPlot(supply));
			}
		} else {
			plots.add(allocationPlot(container));
		}
		return createTimeSeriesChart(plots);
	}
	
	public static XYPlot allocationPlot(AllocationSeries series) { 
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(createTimeSeries(series));
		return createAllocationPlot(dataset);
	}
	
	public static XYPlot allocationPlot(SupplySeries series) { 
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(createTimeSeries(series));
		return createAllocationPlot(dataset);
	}
	
	public static XYPlot allocationPlot(DemandSupplyContainer container) { 
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(createTimeSeries(container.getDemand()));
		for (SupplySeries supplySeries : container.getAllSupplies()) {
			if (container.getAllSupplies().size() == 1 || (supplySeries.getType() != SupplySeries.TYPE.VM_COMPLETED && supplySeries.getType() != SupplySeries.TYPE.VM_SCHEDULED && supplySeries.getType() != SupplySeries.TYPE.MONITORED))
			{
				dataset.addSeries(createTimeSeries(supplySeries));
			}
			
		}
		return createAllocationPlot(dataset);
	}

	public static JFreeChart allocationChart(AllocationSeries allocations) { 
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		final TimeSeries series = createTimeSeries(allocations);
		dataset.addSeries(series);
		return createTimeSeriesChart(createAllocationPlot(dataset));
	}
	
	public static JFreeChart allocationChart(List<ResourceAllocation> allocations) { 
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		final TimeSeries series = createTimeSeries(allocations, "Resource Allocation");
		dataset.addSeries(series);
		return createTimeSeriesChart(createAllocationPlot(dataset));
	}

	private static void addResponseTimeToDataset(TimeTableXYDataset dataset, double granularityInSeconds,
			long windowStart, long summedResponseTimes, long summedWorkTimes,
			long numberOfElements, String name) {
		SimpleTimePeriod timePeriod = new SimpleTimePeriod(new Date(windowStart), new Date((long) (windowStart+granularityInSeconds*1000)));
		long overhead = (summedResponseTimes - summedWorkTimes) / numberOfElements;
		long workTime = summedWorkTimes / numberOfElements;
		dataset.add(timePeriod, overhead, "waiting time");
		dataset.add(timePeriod, workTime, "service time");
	}
	
	public static JFreeChart responseTimeChart(RunResult result) { 
		XYPlot plot = createResponseTimePlot(result,"");
		return createTimeSeriesChart(plot);
	}
	
	public static JFreeChart responseTimeChart(RunResult result, double granulatrityInSeconds) { 
		XYPlot plot = createResponseTimePlot(result,"", granulatrityInSeconds);
		return createTimeSeriesChart(plot);
	}
	
	public static XYPlot createResponseTimePlot(RunResult result, String name, double granularityInSeconds) {
		TimeTableXYDataset dataset = new TimeTableXYDataset();
		
		if (granularityInSeconds < 0.001) 
		{
			granularityInSeconds = 0;
		}

		long delta = result.getResponses().get(0).getRequestSubmitTime();
		long firstRequest = 0;
		double windowStart = firstRequest;
		long summedResponseTimes = 0;
		long summedWorkTimes = 0;
		long numberOfElements = 0;
		
		
		for (JMeterResponse response : result.getResponses()) {
			long requestSubmitTime = response.getRequestSubmitTime() - delta;
			if (requestSubmitTime - windowStart > granularityInSeconds*1000) {
				if (numberOfElements > 0)
				{
						addResponseTimeToDataset(dataset, granularityInSeconds, (long) windowStart,
								summedResponseTimes, summedWorkTimes, numberOfElements, name);
					numberOfElements = 0;
					summedResponseTimes = 0;
					summedWorkTimes = 0;
					firstRequest = requestSubmitTime;
				}
				if (granularityInSeconds > 0) {
					while (requestSubmitTime - windowStart > granularityInSeconds*1000)
					{
						windowStart += granularityInSeconds*1000;
					}
				} else {
					windowStart = requestSubmitTime;
				}	
			}
			numberOfElements++;
			summedResponseTimes += response.getResponseTime();
			summedWorkTimes += response.getRequestServiceTime();
		}
		if (numberOfElements > 0){
			addResponseTimeToDataset(dataset, granularityInSeconds, (long) windowStart,
					summedResponseTimes, summedWorkTimes, numberOfElements, name);
		}
		
		NumberAxis rangeAxis = new NumberAxis("Resp.Time [ms]");
		rangeAxis.setRange(0, 1100);
		StackedXYBarRenderer renderer = new StackedXYBarRenderer(0.10);
		renderer.setShadowVisible(false);
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setSeriesPaint(0, Color.GRAY);
		renderer.setSeriesPaint(1, colorForConfig(name));
		XYPlot plot = new XYPlot(dataset, null, rangeAxis, renderer);
		return plot;
	}

	public static XYPlot createResponseTimePlot(RunResult result, String name) {
		return createResponseTimePlot(result, name, 1);
	}

	public static JFreeChart scheduleChart(RunResult result) { 
		return scheduleChart(result, 1);
	}
	
	public static JFreeChart scheduleChart(RunResult result, double granularityInSeconds) { 
		XYPlot plot = createSchedulePlot(result, granularityInSeconds);
		return createTimeSeriesChart(plot);
	}

	private static XYPlot createSchedulePlot(RunResult result, double granularityInSeconds) {
		TimeTableXYDataset dataset = new TimeTableXYDataset();
		
		if (granularityInSeconds < 0.001) 
		{
			granularityInSeconds = 0;
		}

		long delta = result.getResponses().get(0).getRequestSubmitTime();
		long firstRequest = 0;
		double windowStart = firstRequest;
		long summedDiffs = 0;
		long numberOfElements = 0;
		Iterator<JMeterResponse> responseIterator = result.getResponses().iterator();
		Iterator<Long> diffIterator = result.getTimestampDiffs().iterator();
		while (responseIterator.hasNext() && diffIterator.hasNext()) {
			AbstractResponse response = responseIterator.next();
			Long diff = diffIterator.next();
			long requestSubmitTime = response.getRequestSubmitTime() - delta;
			if (requestSubmitTime - windowStart > granularityInSeconds*1000) {
				if (numberOfElements > 0)
				{
					addDiffToDataset(granularityInSeconds, dataset,
							windowStart, summedDiffs, numberOfElements);
					numberOfElements = 0;
					summedDiffs = 0;
					firstRequest = requestSubmitTime;
				}
				if (granularityInSeconds > 0) {
					while (requestSubmitTime - windowStart > granularityInSeconds*1000)
					{
						windowStart += granularityInSeconds*1000;
					}
				} else {
					windowStart = requestSubmitTime;
				}	
			}
			numberOfElements++;
			summedDiffs += diff;
		}
		if (numberOfElements > 0){
			addDiffToDataset(granularityInSeconds, dataset,
					windowStart, summedDiffs, numberOfElements);
		}
		NumberAxis rangeAxis = new NumberAxis("Request Delay [ms]");
		StackedXYBarRenderer renderer = new StackedXYBarRenderer(0.10);
		renderer.setShadowVisible(false);
		renderer.setBarPainter(new StandardXYBarPainter());
		XYPlot plot = new XYPlot(dataset, null, rangeAxis, renderer);
		return plot;
	}

	private static void addDiffToDataset(double granularityInSeconds,
			TimeTableXYDataset dataset, double windowStart, long summedDiffs,
			long numberOfElements) {
		long diffAverage = summedDiffs / numberOfElements;
		SimpleTimePeriod timePeriod = new SimpleTimePeriod(new Date((long)windowStart), new Date((long) (windowStart+granularityInSeconds*1000)));
		dataset.add(timePeriod, diffAverage, "Request Submission Delay");
	}

	private static TimeSeries createTimeSeries(AllocationSeries series) {
		return createTimeSeries(series.getAllocations(), series.getCategory().toString());
	}

	private static TimeSeries createTimeSeries(SupplySeries series) {
		String name = series.getName();
		return createTimeSeries(series.getAllocations(), name);
	}

	private static TimeSeries createTimeSeries(
			List<ResourceAllocation> allocations, String label) {
		Integer lastAmount = null;
		final TimeSeries series = new TimeSeries(label);
		for (ResourceAllocation allocation : allocations) {
			if (lastAmount != null)
			{
				series.addOrUpdate(new FixedMillisecond(new Date(DateUtility.toSecondPrecision(allocation.getDate()).getTime()-1)), lastAmount);
			}
			series.addOrUpdate(new FixedMillisecond(DateUtility.toSecondPrecision(allocation.getDate())), allocation.getCurrentAmount());
			lastAmount = allocation.getCurrentAmount();
		}
		return series;
	}

	public static JFreeChart mappingChart(final IntensityDemandMapping mapping) {
		final XYSeriesCollection dataset = new XYSeriesCollection();
		final XYSeries mappingSeries = new XYSeries("mapping function");
		double lastIntentsity = 0;
		for (IntensityResourcePair pair : mapping.getMappingList())
		{
			mappingSeries.add(lastIntentsity, pair.resourceAmount);
			mappingSeries.add(pair.maxIntensity, pair.resourceAmount);
			lastIntentsity = pair.maxIntensity;
		}
		dataset.addSeries(mappingSeries);

		final JFreeChart chart = ChartFactory.createXYLineChart(
				"",
				"Load Intensity", 
				"Resource Amount",
				dataset
				);

		
		chartCustomization(chart);
		chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE);
		chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(STROKE_WIDTH));  
		NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
		rangeAxis.setTickUnit(new NumberTickUnit(1));
		return chart;
	}

	public static JFreeChart intensityChart(final List<ArrivalRateTuple> intensities) {
		final CombinedDomainXYPlot plot = createRelativeTimeSeriesPlot();
		JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chartCustomization(chart);

		final XYPlot intensityPlot = createIntensityPlot(intensities);
		plot.add(intensityPlot, 1);

		return chart;
	}

	public static XYPlot createIntensityPlot(final List<ArrivalRateTuple> intensities) {
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		final TimeSeries series = new TimeSeries("load intensity");
		for (ArrivalRateTuple intensity : intensities) {
			long milliseconds = (referenceDate.getTime()/1000 + (long) Math.floor(intensity.getTimeStamp())) * 1000;
			series.add(new FixedMillisecond(milliseconds), intensity.getArrivalRate());
		}
		dataset.addSeries(series);

		XYStepRenderer renderer = new XYStepRenderer();
		//renderer.setSeriesStroke(0, (new BasicStroke(2.0F)));   
		renderer.setSeriesPaint(0, Color.GRAY);
		final NumberAxis rangeAxis = new NumberAxis("Arrival Rate [1/s]");
		//rangeAxis.setRange(0, 220);
		final XYPlot intensityPlot = new XYPlot(dataset, null, rangeAxis, renderer);
		return intensityPlot;
	}

	public static JFreeChart demandChart(final List<ArrivalRateTuple> intensities, final IntensityDemandMapping mapping, boolean showIntensities) {
		List<XYPlot> plots = new LinkedList<XYPlot>();
		if (showIntensities) {
			plots.add(createIntensityPlot(intensities));
		}
		plots.add(createDemandPlot(intensities, mapping));
		return createTimeSeriesChart(plots);
	}

	private static CombinedDomainXYPlot createRelativeTimeSeriesPlot() {
		DateAxis timeAxis = new DateAxis("Time");
		RelativeDateFormat relativeDateFormat = new RelativeDateFormat(referenceDate);
		relativeDateFormat.setSecondFormatter(new DecimalFormat("0"));
		relativeDateFormat.setShowZeroHours(false);
		timeAxis.setDateFormatOverride(relativeDateFormat);
		final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(timeAxis);
		return plot;
	}

	public static XYPlot createDemandPlot(
			final List<ArrivalRateTuple> intensities,
			final IntensityDemandMapping mapping) {
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		final TimeSeries series = new TimeSeries("resource demand");
		for (ArrivalRateTuple intensity : intensities) {
			long milliseconds = (referenceDate.getTime()/1000 + (long) Math.floor(intensity.getTimeStamp())) * 1000;
			series.add(new FixedMillisecond(milliseconds), mapping.getResourceDemand(intensity.getArrivalRate()));
		}
		dataset.addSeries(series);

		XYStepRenderer renderer = new XYStepRenderer();
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesStroke(0, new BasicStroke(STROKE_WIDTH));  
		final NumberAxis rangeAxis = new NumberAxis("Resource Amount");
		rangeAxis.setAutoRangeIncludesZero(true);
		//rangeAxis.setRange(0,2.2);
		rangeAxis.setTickUnit(new NumberTickUnit(2));
		final XYPlot amountPlot = new XYPlot(dataset, null, rangeAxis, renderer);
		return amountPlot;
	}

	private static void chartCustomization(final JFreeChart chart) {
		chart.setBackgroundPaint(Color.white);
		final XYPlot plot = chart.getXYPlot();
		customizePlot(plot);
	}

	private static void customizePlot(final XYPlot plot) {
		plot.setOutlinePaint(null);
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setDomainCrosshairVisible(false);
		plot.setRangeCrosshairVisible(false);
		//plot.setShadowGenerator(new DefaultShadowGenerator());
		Font font = new Font("Dialog", Font.PLAIN, 20);
		Font labelFont = new Font("Dialog", Font.PLAIN, 16);
		plot.getDomainAxis().setLabelFont(labelFont);
		plot.getRangeAxis().setLabelFont(labelFont);
		plot.getDomainAxis().setTickLabelFont(font);
		plot.getRangeAxis().setTickLabelFont(font);
	}

	private static XYPlot createAllocationPlot(final TimeSeriesCollection dataset) {


		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setBaseShapesVisible(true);
		renderer.setBaseShapesFilled(false);
		final NumberAxis rangeAxis = new NumberAxis("Resource Amount");
		rangeAxis.setAutoRangeIncludesZero(true);
		//rangeAxis.setTickUnit(new NumberTickUnit(1));
		double lower = Math.min(1, dataset.getRangeBounds(false).getLowerBound()) - 0.2;
		double upper = Math.max(2, dataset.getRangeBounds(false).getUpperBound()) + 0.2;
		rangeAxis.setRange(lower, upper);
		final XYPlot allocationPlot = new XYPlot(dataset, null, rangeAxis, renderer);
		allocationPlot.setSeriesRenderingOrder(SeriesRenderingOrder.REVERSE);
		
		for (int i = 0; i < dataset.getSeriesCount(); i++)
		{
			TimeSeries series = dataset.getSeries(i);
			//addEndSeriesItem(lastDate, series);
			String description = series.getKey().toString();
			renderer.setSeriesStroke(i, new BasicStroke(STROKE_WIDTH));  
			if (description.equals(AllocationSeries.CATEGORY.DEMAND.toString()))
			{
				renderer.setSeriesShapesVisible(i, false);
				renderer.setSeriesPaint(i, Color.RED);
			} else if (description.equals(SupplySeries.TYPE.VM_SCHEDULED.toString()))
			{
				renderer.setSeriesPaint(i, VARIANT_C_COLOR);
			} else if (description.equals(SupplySeries.TYPE.VM_COMPLETED.toString()))
			{
				renderer.setSeriesPaint(i,VARIANT_B_COLOR);
			} else if (description.equals(SupplySeries.TYPE.LB_RULE_ADAPTION.toString()))
			{
				if (dataset.getSeriesCount() != 2) {
					renderer.setSeriesPaint(i, VARIANT_A_COLOR);
				} else {
					renderer.setSeriesPaint(i, Color.BLUE);
				}
			} else if (description.equals(SupplySeries.TYPE.MONITORED.toString()))
			{
				if ( dataset.getSeriesCount() != 2) {
					renderer.setSeriesPaint(i, VARIANT_D_COLOR);
				} else {
					renderer.setSeriesPaint(i, Color.BLUE);
				}
			} else 
			{
				renderer.setSeriesPaint(i, colorForConfig(description));
			}
		}
		
		return allocationPlot;
	}
	
	private static Color colorForConfig(String configName) {
		if (configName.contains("config-a"))
		{
			return VARIANT_A_COLOR;
		}  else if (configName.contains("config-b"))
		{
			return VARIANT_B_COLOR;
		}  else if (configName.contains("config-c"))
		{
			return VARIANT_C_COLOR;
		} else if (configName.contains("config-d"))
		{
			return VARIANT_D_COLOR;
		} else {
			return Color.BLUE;
		}
	}

	public static JFreeChart createTimeSeriesChart(final XYPlot plot) {
		List<XYPlot> plots = new LinkedList<XYPlot>();
		plots.add(plot);
		return createTimeSeriesChart(plots);
	}
	
	public static JFreeChart createTimeSeriesChart(final List<XYPlot> plots) {
		final CombinedDomainXYPlot plot = createRelativeTimeSeriesPlot();
		plot.setGap(20);
		JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		boolean manyCharts = plots.size() > 3;
		int num = 0;
		for (XYPlot xyPlot : plots)
		{
			num++;
			if (manyCharts && xyPlot.getRangeAxis().getLabel() == "Arrival Rate [1/s]") {
				plot.add(xyPlot, 3);
			} else {
				plot.add(xyPlot, 1);
			}
			
			chart.setBackgroundPaint(Color.white);
			customizePlot(xyPlot);
			if (manyCharts && xyPlot.getRangeAxis().getLabel() != "Arrival Rate [1/s]"  && num != 4 && num != 7) {
				xyPlot.getRangeAxis().setLabel("");
			}
		}
		return chart;
	}
}
