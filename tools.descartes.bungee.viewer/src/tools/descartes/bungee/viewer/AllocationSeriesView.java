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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;

import tools.descartes.bungee.allocation.AllocationSeries;
import tools.descartes.bungee.allocation.DemandSupplyContainer;
import tools.descartes.bungee.allocation.ResourceAllocation;
import tools.descartes.bungee.chart.ChartGenerator;
import tools.descartes.bungee.loadprofile.DlimAdapter;
import tools.descartes.dlim.generator.util.*;


public class AllocationSeriesView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "elasticitymeasurementviewer.views.AllocationSeriesView";

	private Composite parent;
	private ChartComposite chartComposite;

	/**
	 * The constructor.
	 */
	public AllocationSeriesView() {
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
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			List<ResourceAllocation> allocations = null;
			DemandSupplyContainer benchedSeries = null;
			DlimAdapter dlim = null;
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof IResource) {
					File file = ((IResource)element).getLocation().toFile();
					if (AllocationSeries.isAllocationFile(file)) {
						allocations = AllocationSeries.readAllocations(file);
						setContentDescription(file.toString());
					} else if (DemandSupplyContainer.isSeriesContainer(file)) {
						benchedSeries = DemandSupplyContainer.read(file);
						setContentDescription(file.toString());
					} else if (DlimAdapter.isDLIMFile(file)) {
						dlim = DlimAdapter.read(file,1);
					}
				}
			}
			if (allocations != null || benchedSeries != null)
			{
				if (chartComposite != null)
				{
					chartComposite.dispose();
				}
				JFreeChart chart; 
				if (benchedSeries != null) {
					if (dlim != null) {
						chart = ChartGenerator.allocationChart(benchedSeries, dlim.getArrivalRates(), false);
					} else {
						chart = ChartGenerator.allocationChart(benchedSeries, false);
					}
				} else {
					chart = ChartGenerator.allocationChart(allocations); 
				}
				chartComposite = new ChartComposite(parent, SWT.NONE, chart, false); 
				// repaint
				parent.layout(true);
			}
		}
	}

	@Override  
	public void createPartControl(final Composite parent) {  
		this.parent = parent;
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);
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