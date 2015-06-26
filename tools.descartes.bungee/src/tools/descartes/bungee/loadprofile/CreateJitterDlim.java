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

public class CreateJitterDlim {

	public static void main(String[] args) {
		double initialDuration = 7*60;
		double lowIntensity = 50;
		double highIntensity = 150;
		double alpha = 0.85;
		double iterations = 10;
		
		double duration = initialDuration;
		
		DlimAdapter result = createLowHigh(duration, lowIntensity,
				highIntensity);
		for (int i = 2; i <= iterations; i++)
		{
			duration *= alpha;
			DlimAdapter lowHigh = createLowHigh(duration, lowIntensity,
					highIntensity);
			result = DlimModelFactory.concatenate(result, lowHigh);
		}
		
		result.save(new File("files/model/evaluation/jitterNegative.dlim"));
	}

	private static DlimAdapter createLowHigh(double duration,
			double lowIntensity, double highIntensity) {
		DlimAdapter constantLow = DlimModelFactory.createConstantLoad(duration, lowIntensity);
		DlimAdapter constantHigh = DlimModelFactory.createConstantLoad(duration, highIntensity);
		DlimAdapter lowHigh = DlimModelFactory.concatenate(constantLow, constantHigh);
		return lowHigh;
	}

}
