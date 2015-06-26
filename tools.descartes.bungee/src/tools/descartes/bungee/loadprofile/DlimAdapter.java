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

package tools.descartes.bungee.loadprofile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import tools.descartes.dlim.DlimPackage;
import tools.descartes.dlim.Sequence;
import tools.descartes.dlim.exporter.utils.ArrivalRateGenerator;
import tools.descartes.dlim.generator.ArrivalRateTuple;
import tools.descartes.dlim.generator.ModelEvaluator;

public class DlimAdapter implements LoadProfile {
	private static boolean initialized = false;
	private double secondsPerTimeUnit = 1;

	
	private Sequence rootSequence;
	private ModelEvaluator evaluator;

	protected DlimAdapter(Sequence model, double secondsPerTimeUnit)
	{
		this.rootSequence = model;
		evaluator = new ModelEvaluator(model);
		this.setSecondsPerTimeUnit(secondsPerTimeUnit);
	}

	@Override
	public double getIntensity(double time) {
		return evaluator.getArrivalRateAtTime(time/secondsPerTimeUnit)/secondsPerTimeUnit;
	}

	public static DlimAdapter read(File file, double timeUnitSizeInSeconds) {
		initDLIM();
		
		// Obtain a new resource set
		ResourceSet resSet = new ResourceSetImpl();
		// Get the resource
		Resource resource = resSet.getResource(URI.createFileURI(file.toString()), true);
		// Get the first model element and cast it to the right type, in my
		// example everything is hierarchical included in this first node
		Sequence model = (Sequence) resource.getContents().get(0);
		return new DlimAdapter(model, timeUnitSizeInSeconds);
	}

	public void save(File file) {
		initDLIM();
		
		// Obtain a new resource set
		ResourceSet resSet = new ResourceSetImpl();
		Resource resource = resSet.createResource(URI.createFileURI(file.toString()));
		resource.getContents().add(rootSequence);

		// now save the content.
		try {
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<ArrivalRateTuple> getArrivalRates() {
		List<ArrivalRateTuple> arrivalRates = ArrivalRateGenerator.generateArrivalRates(evaluator, 1.0f / secondsPerTimeUnit);
		for (ArrivalRateTuple rate : arrivalRates) {
			rate.setArrivalRate(rate.getArrivalRate()/secondsPerTimeUnit);
			rate.setTimeStamp(rate.getTimeStamp()*secondsPerTimeUnit);
		}
		return arrivalRates;
	}

	@Override
	public double getDuration()
	{
		return (evaluator.getDuration()*secondsPerTimeUnit);
	}

	public double getMaxIntensity() {
		List<ArrivalRateTuple> arrivalRates = getArrivalRates();
		double max = 0;
		for (ArrivalRateTuple rate : arrivalRates) {
			max = Math.max(max, rate.getArrivalRate());
		}
		return max;
	}

	private static void initDLIM() {
		if (!initialized)
		{
			DlimPackage.eINSTANCE.eClass();
			Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
			Map<String, Object> m = reg.getExtensionToFactoryMap();
			m.put("dlim", new XMIResourceFactoryImpl());
			initialized = true;
		}
	}

	public static boolean isDLIMFile(File file) {
		boolean isDlimFile = false;
		if (file.exists() && file.isFile() && file.getName().endsWith(".dlim"))
		{
			isDlimFile = true;
		}
		return isDlimFile;
	}

	Sequence getRootSequence() {
		return rootSequence;
	}

	public double getSecondsPerTimeUnit() {
		return secondsPerTimeUnit;
	}

	public void setSecondsPerTimeUnit(double secondsPerTimeUnit) {
		this.secondsPerTimeUnit = secondsPerTimeUnit;
	}

}