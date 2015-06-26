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
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;

import tools.descartes.bungee.analysis.IntensityDemandMapping;
import tools.descartes.bungee.calibration.AdjustmentFunctionGeneration;
import tools.descartes.bungee.calibration.PiecewiseLinearAdjustmentGenerator;
import tools.descartes.bungee.chart.ChartGenerator;
import tools.descartes.bungee.loadprofile.AdjustedLoadProfile;
import tools.descartes.bungee.loadprofile.DlimAdapter;
import tools.descartes.bungee.loadprofile.LoadProfile;
import tools.descartes.dlim.generator.ArrivalRateTuple;


public class DemandView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "elasticitymeasurementviewer.views.DemandView";

	private Composite parent;
	private ChartComposite chartComposite;
	private IntensityDemandMapping mapping;
	private List<ArrivalRateTuple> intensities;
	private List<ArrivalRateTuple> scaledIntensities;
	private Button showIntensityButton;
	private Button doCalibrationButton;
	private AdjustmentFunctionGeneration calibrator;

	/**
	 * The constructor.
	 */
	public DemandView() {
		calibrator = new PiecewiseLinearAdjustmentGenerator();
	}

	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	private ISelectionListener listener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			showSelection(sourcepart, selection);
		}
	};

	/**
	 * Shows the given selection in this view.
	 */
	public void showSelection(IWorkbenchPart sourcepart, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IntensityDemandMapping tempMapping = null;
			LoadProfile intensityModel = null;
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof IResource) {
					File file = ((IResource)element).getLocation().toFile();
					if (IntensityDemandMapping.isMappingFunction(file))
					{
						tempMapping = IntensityDemandMapping.read(file);
					} else if (DlimAdapter.isDLIMFile(file))
					{
						intensityModel = DlimAdapter.read(file,1);
					}
				}
			}
			if (tempMapping != null && intensityModel != null)
			{
				mapping = tempMapping;  
				intensities =  intensityModel.getArrivalRates();
				scaledIntensities = new AdjustedLoadProfile(intensityModel, calibrator.getAdjustmentFunction(mapping, intensityModel)).getArrivalRates();
				redrawChart();
			}
		}
	}

	private void redrawChart() {
		if (chartComposite != null)
		{
			chartComposite.dispose();
		}
		JFreeChart chart;
		if (doCalibrationButton.getSelection()) {
			chart = ChartGenerator.demandChart(scaledIntensities, mapping, showIntensityButton.getSelection());
		} else {
			chart = ChartGenerator.demandChart(intensities, mapping, showIntensityButton.getSelection());
		}
		chartComposite = new ChartComposite(parent, SWT.NONE, chart, false); 
		parent.layout(true);
	}

	@Override  
	public void createPartControl(final Composite parent) { 
		Composite composite = new Composite(parent, SWT.NONE);
		Composite chartParent = new Composite(composite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		chartParent.setLayoutData(gridData);
		showIntensityButton = new Button (composite, SWT.CHECK);
		showIntensityButton.setText ("Show Intensity");
		showIntensityButton.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		        switch (e.type) {
		        case SWT.Selection:
		        	redrawChart();
		          break;
		        }
		      }
		    });
		doCalibrationButton = new Button (composite, SWT.CHECK);
		doCalibrationButton.setText ("Do Calibration");
		doCalibrationButton.setSelection(true);
		doCalibrationButton.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) {
		        switch (e.type) {
		        case SWT.Selection:
		        	redrawChart();
		          break;
		        }
		      }
		    });
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);
		composite.setLayout(new GridLayout());
		chartParent.setLayout(new FillLayout());
		this.parent = chartParent;
	}  

	public void setFocus() {	
		if (chartComposite != null)
		{
			chartComposite.setFocus();
		}
	}

	public void dispose() {
		// important: We need do unregister our listener when the view is disposed
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listener);
		super.dispose();
	}
}