package tools.descartes.bungee.chart;
/*******************************************************************************
* Copyright (c) 2014 Andreas Weber, Nikolas Herbst
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/

//package tools.descartes.bungee.chart;
//
//
//import java.awt.Graphics2D;
//import java.awt.geom.Rectangle2D;
//import java.io.File;
//import java.io.FileOutputStream;
//
//import org.jfree.chart.JFreeChart;
//
//import tools.descartes.bungee.allocation.DemandSupplyContainer;
//import tools.descartes.bungee.loadprofile.DlimAdapter;
//
//import com.itextpdf.awt.DefaultFontMapper;
//import com.itextpdf.text.Document;
//import com.itextpdf.text.Rectangle;
//import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfTemplate;
//import com.itextpdf.text.pdf.PdfWriter;
//
//
//public class ChartPDFWriter {
//
//    
//	public static void main(final String[] args) {
//		//List<ResourceAllocation> allocations = ResourceAllocationUtils.readAllocationsFromFile("completedVMs.csv");
//		//JFreeChart chart = allocationChart(allcations);
//		DemandSupplyContainer benchedSeries = DemandSupplyContainer.read(new File("files/evaluation/accuracyU/2014.05.04_20.30.37/result.seriescontainer"));
//		//RunResult result = RunResult.loadFromFile(new File("files/measurement/141.21.72.21_500000_2014.04.07_14.38.27/result.runresult"));
//		//RunResult result = RunResult.loadFromFile(new File("files/measurement/2014.04.22_18.40.59/result.runresult"));
//		//IntensityDemandMapping mapping = IntensityDemandMapping.load(new File("files/calibration/smallOneCore_2014.04.25_11.50.21/smallOneCore.mapping"));
//		DlimAdapter dlim = DlimAdapter.read(new File("files/evaluation/accuracyU/accuracy_U.dlim"), 1);
//
//
//		//JFreeChart chart = allocationChart(completedVMs);
//		//JFreeChart chart = allocationChart(benchedSeries, true);
//		JFreeChart chart = ChartGenerator.allocationChart(benchedSeries, dlim.getArrivalRates(), true);
//		//JFreeChart chart = runResultChart(result,1);
//		//JFreeChart chart = responseTimeChart(result);
//		//JFreeChart chart = scheduleChart(result);
//		//JFreeChart chart = mappingChart(mapping);
//		//JFreeChart chart = demandChart(dlim.getArrivalRates(), mapping, true);
//
//		writeChartToPDF(chart, 800, 400, new File("files/evaluation/accuracyU/2014.05.04_20.30.37/result.pdf"));
//	}
//	
//	public static void writeChartToPDF(JFreeChart chart, int width, int height, File fileName) {
//	    PdfWriter writer = null;
//	    com.itextpdf.text.Rectangle pagesize = new Rectangle(width,height);
//	    Document document = new Document(pagesize);
//	 
//	    try {
//	        writer = PdfWriter.getInstance(document, new FileOutputStream(
//	                fileName));
//	        document.open();
//	        PdfContentByte contentByte = writer.getDirectContent();
//	        PdfTemplate template = contentByte.createTemplate(width, height);
//			@SuppressWarnings("deprecation")
//			Graphics2D graphics2d = template.createGraphics(width, height, new DefaultFontMapper());
//	        Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width,
//	                height);
//	 
//	        chart.draw(graphics2d, rectangle2d);
//	         
//	        graphics2d.dispose();
//	        contentByte.addTemplate(template, 0, 0);
//	 
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	    }
//	    document.close();
//	}
//}
