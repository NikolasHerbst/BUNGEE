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

package tools.descartes.bungee.viewer;


import java.io.File;
import java.util.Iterator;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.JFreeChart;

import tools.descartes.bungee.chart.ChartFrame;
import tools.descartes.bungee.chart.ChartGenerator;
//import tools.descartes.bungee.chart.ChartPDFWriter;
import tools.descartes.bungee.loadgeneration.RunResult;
import tools.descartes.bungee.utils.FileUtility;


public class RunResultView extends ViewPart implements ISelectionListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "elasticitymeasurementviewer.views.RunResultView";

	private Button showScheduleChartButton;
	private Button saveScheduleChartButton;
	private File lastRunResultFile = null;
	private RunResult runResult = null;

	/**
	 * The constructor.
	 */
	public RunResultView() {
	}

	

	private Text timingMeanValue;
	private Text timingStdValue;
	private Text responseTimeMeanValue;
	private Text responseTimeStdValue;
	private Button sanityCheckValue;
	private Composite parent;
	private Button showResponseChartButton;
	private Button saveResponseChartButton;
	private JFreeChart scheduleChart;

	private JFreeChart responseChart;

	private Button scheduleCheckValue;

	private Button completedCheckValue;

	private Button successCheckValue;

	/**
	 * Shows the given selection in this view.
	 */
	public void showSelection(IWorkbenchPart sourcepart, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof IResource) {
					File file = ((IResource)element).getLocation().toFile();
					if (!file.equals(lastRunResultFile) && RunResult.isRunresult(file)) {
						lastRunResultFile = file;
						setContentDescription(file.toString());
						responseChart = null;
						scheduleChart = null;
						runResult = RunResult.read(file);
						update();
					}
				}
			}
		}
	}
	
	public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
		showSelection(sourcepart, selection);
	}

	private void update() {
		if (runResult != null) {
			sanityCheckValue.setSelection(runResult.passedSanityCheck());
			scheduleCheckValue.setSelection(runResult.passedScheduleCheck());
			completedCheckValue.setSelection(runResult.isRunCompleted());
			successCheckValue.setSelection(runResult.isRunSuccessful());

			DescriptiveStatistics timingStats = runResult.getTimingStats();
			if (timingStats.getN() > 0) {
				timingMeanValue.setText(String.format("%.3f", timingStats.getMean()));
				timingStdValue.setText(String.format("%.3f",timingStats.getStandardDeviation()));
			} else {
				timingMeanValue.setText("-");
				timingStdValue.setText("-");
			}
			
			DescriptiveStatistics responseStats = runResult.getResponseTimeStats();
			if (responseStats.getN() > 0) {
				responseTimeMeanValue.setText(String.format("%.3f",responseStats.getMean()));
				responseTimeStdValue.setText(String.format("%.3f",responseStats.getStandardDeviation()));				
			} else {
				responseTimeMeanValue.setText("-");
				responseTimeStdValue.setText("-");	
			}
		}
	}

	@Override  
	public void createPartControl(final Composite parent) { 
		VerifyListener numberVerifier = new VerifyListener() {  
			@Override  
			public void verifyText(VerifyEvent event) {  
				switch (event.keyCode) {  
				case SWT.BS:           // Backspace  
				case SWT.DEL:          // Delete  
				case SWT.HOME:         // Home  
				case SWT.END:          // End  
				case SWT.ARROW_LEFT:   // Left arrow  
				case SWT.ARROW_RIGHT:  // Right arrow  
					return;  
				}  

				if (!Character.isDigit(event.character)) {  
					event.doit = false;  // disallow the action  
				}  
			}
		};  

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		Composite compositeChecks1 = new Composite(composite, SWT.NONE);
		compositeChecks1.setLayout(new GridLayout(2,false));
		Text sanityText = new Text(compositeChecks1, SWT.NONE);
		sanityText.setText("Passed Sanity Check:");
		sanityCheckValue = new Button(compositeChecks1, SWT.CHECK);
		sanityCheckValue.setEnabled(false);
		Composite compositeSChecks2 = new Composite(composite, SWT.NONE);
		compositeSChecks2.setLayout(new GridLayout(2,false));
		Text scheduleText = new Text(compositeSChecks2, SWT.NONE);
		scheduleText.setText("Passed Schedule Check:");
		scheduleCheckValue = new Button(compositeSChecks2, SWT.CHECK);
		scheduleCheckValue.setEnabled(false);
		Text completedText = new Text(compositeChecks1, SWT.NONE);
		completedText.setText("Run completed:");
		completedCheckValue = new Button(compositeChecks1, SWT.CHECK);
		completedCheckValue.setEnabled(false);
		Text successText = new Text(compositeSChecks2, SWT.NONE);
		successText.setText("Run successful:");
		successCheckValue = new Button(compositeSChecks2, SWT.CHECK);
		successCheckValue.setEnabled(false);
		Text timingMeanText = new Text(composite, SWT.NONE);
		timingMeanText.setText("Schedule deviation mean: ");
		timingMeanValue = new Text(composite, SWT.SINGLE);
		timingMeanValue.setText("");
		Text timingStdText = new Text(composite, SWT.NONE);
		timingStdText.setText("Schedule deviation std: ");
		timingStdValue = new Text(composite, SWT.SINGLE);
		timingStdValue.setText("");
		Text responseTimeMeanText = new Text(composite, SWT.NONE);
		responseTimeMeanText.setText("Responsetime mean: ");
		responseTimeMeanValue = new Text(composite, SWT.SINGLE);
		responseTimeMeanValue.setText("");
		Text responseTimeStdText = new Text(composite, SWT.NONE);
		responseTimeStdText.setText("Responsetime std: ");
		responseTimeStdValue = new Text(composite, SWT.SINGLE);
		responseTimeStdValue.setText("");
		Text chartWidthText = new Text(composite, SWT.NONE);
		chartWidthText.setText("Chart width: ");
		final Text chartWidthValue = new Text(composite, SWT.BORDER);
		chartWidthValue.setText("800");
		chartWidthValue.addVerifyListener(numberVerifier);
		Text chartHeightText = new Text(composite, SWT.NONE);
		chartHeightText.setText("Chart height: ");
		final Text chartHeightValue = new Text(composite, SWT.BORDER);
		chartHeightValue.setText("400");
		chartHeightValue.addVerifyListener(numberVerifier);
		showScheduleChartButton = new Button (composite, SWT.NONE);
		showScheduleChartButton.setText ("Show Schedule Chart");
		showScheduleChartButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:
					showScheduleChart(Integer.parseInt(chartWidthValue.getText()), Integer.parseInt(chartHeightValue.getText()));
					break;
				}
			}
		});
		saveScheduleChartButton = new Button (composite, SWT.NONE);
		saveScheduleChartButton.setText ("Save Schedule Chart");
		saveScheduleChartButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
//				optional window
				Display display = Display.getDefault();
				Shell dialogShell = new Shell(display, SWT.APPLICATION_MODAL);
				MessageBox dialog = 
				  new MessageBox(dialogShell, SWT.ICON_WARNING | SWT.OK);
				dialog.setText("Licence Problem");
				dialog.setMessage("This feature is currently disabled! \n If you want to reenable it, check out the code.");
				dialog.open();
//				end of optional window
				switch (e.type) {
				case SWT.Selection:
					saveScheduleChart(Integer.parseInt(chartWidthValue.getText()), Integer.parseInt(chartHeightValue.getText()));
					break;
				}
			}
		});
		showResponseChartButton = new Button (composite, SWT.NONE);
		showResponseChartButton.setText ("Show Response Chart");
		showResponseChartButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.Selection:
					showResponseChart(Integer.parseInt(chartWidthValue.getText()), Integer.parseInt(chartHeightValue.getText()));
					break;
				}
			}
		});
		saveResponseChartButton = new Button (composite, SWT.NONE);
		saveResponseChartButton.setText ("Save Response Chart");
		saveResponseChartButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
//				optional window
				Display display = Display.getDefault();
				Shell dialogShell = new Shell(display, SWT.APPLICATION_MODAL);
				MessageBox dialog = 
				  new MessageBox(dialogShell, SWT.ICON_WARNING | SWT.OK);
				dialog.setText("Licence problem");
				dialog.setMessage("This feature is currently disabled! \n If you want to reenable it, check out the code.");
				dialog.open();
//				end of optional window
				switch (e.type) {
				case SWT.Selection:
					saveResponseChart(Integer.parseInt(chartWidthValue.getText()), Integer.parseInt(chartHeightValue.getText()));
					break;
				}
			}
		});
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		composite.setLayout(new GridLayout(2,false));
		this.parent = parent;
	}  

	private void showScheduleChart(int width, int height) {
		if (runResult != null)
		{
			generateScheduleChart();
			new ChartFrame("Schedule Chart", scheduleChart, width, height, true);
		}
	}

	private void generateScheduleChart() {
		if (scheduleChart == null)
		{
			scheduleChart = ChartGenerator.scheduleChart(runResult);
		}
	}

	private void showResponseChart(int width, int height) {
		if (runResult != null)
		{
			generateResponseChart();
			new ChartFrame("Responsetime Chart", responseChart, width, height, true);
		}
	}

	private void generateResponseChart() {
		if (responseChart == null)
		{
			responseChart = ChartGenerator.responseTimeChart(runResult);
		}
	}

	private void saveScheduleChart(int width, int height) {
		if (runResult != null)
		{
			generateScheduleChart();
//			This option is disabled because of the license used for itext
//			ChartPDFWriter.writeChartToPDF(scheduleChart, width, height, new File(FileUtility.getFileNameWithoutExtension(lastRunResultFile)+"ScheduleGraph.pdf"));
		}
	}
	
	private void saveResponseChart(int width, int height) {
		if (runResult != null)
		{
			generateResponseChart();
//			This option is disabled because of the license used for itext
//			ChartPDFWriter.writeChartToPDF(responseChart, width, height, new File(FileUtility.getFileNameWithoutExtension(lastRunResultFile)+"ResponseGraph.pdf"));
		}
	}

	public void dispose() {
		// important: We need do unregister our listener when the view is disposed
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		super.dispose();
	}

	@Override
	public void setFocus() {
		parent.setFocus();
	}
}