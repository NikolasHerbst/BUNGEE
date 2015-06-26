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

package tools.descartes.bungee.calibration.adjustmentfunction;

import java.util.ArrayList;
import java.util.List;

import tools.descartes.bungee.loadprofile.AdjustmentFunction;

public class PiecewiseLinearFunction implements AdjustmentFunction {
	
	private ArrayList<Segment> lines;
	private ArrayList<Double> borders;
	

	/**
	 * @param lines
	 * @param borders
	 */
	public PiecewiseLinearFunction(List<Segment> lines,
			List<Double> borders) {
		this.lines = new ArrayList<Segment>(lines);
		this.borders = new ArrayList<Double>(borders);
	}


	public void addSegment(double start, double m, double b) {
		borders.add(start);
		lines.add(new Segment(m,b));
	}


	@Override
	public double adjustIntensity(double intensity) {
		double adjustedIntensity = intensity;
		if (lines.size() > 0)
		{
			Segment segment = lines.get(lines.size()-1);
			for (int i = 0; i < borders.size(); i++)
			{
				if (intensity < borders.get(i)) {
					segment = lines.get(i);
					break;
				}
			}
			adjustedIntensity = segment.m * intensity + segment.b;
		} 
		return adjustedIntensity;
	}
}
