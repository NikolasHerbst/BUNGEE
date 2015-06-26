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

package tools.descartes.bungee.calibration;

import java.util.LinkedList;

import tools.descartes.bungee.analysis.IntensityDemandMapping;
import tools.descartes.bungee.calibration.adjustmentfunction.PiecewiseLinearFunction;
import tools.descartes.bungee.calibration.adjustmentfunction.Segment;
import tools.descartes.bungee.loadprofile.AdjustmentFunction;


public class PiecewiseLinearAdjustmentGenerator extends AdjustmentFunctionGeneration {
	
	public AdjustmentFunction getAdjustmentFunction(IntensityDemandMapping mapping, int maxResources, double maxIntensity) {
		double idealStepLength = maxIntensity / maxResources;
		LinkedList<Segment> segments = new LinkedList<Segment>();
		LinkedList<Double> borders = new LinkedList<Double>();
		
		for (int i = 1; i <= mapping.getMaxAvailableResources(); i++)
		{
			Segment segment = new Segment(0, 0);
			double startIntensity = 0;
			double endIntensity = mapping.getMaxIntensity(i);
			if (i > 1) {
				startIntensity = mapping.getMaxIntensity(i-1);
				borders.add(idealStepLength * (i-1));
			}
			double stepLength = endIntensity - startIntensity;
			segment.b = startIntensity - stepLength * (i-1);
			segment.m = stepLength / (idealStepLength);
			segments.add(segment);
		}
		
		
		PiecewiseLinearFunction scaler = new PiecewiseLinearFunction(segments, borders);
		return scaler;
	}

}
