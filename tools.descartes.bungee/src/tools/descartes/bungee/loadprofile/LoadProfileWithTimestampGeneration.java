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
import java.util.List;

import tools.descartes.dlim.exporter.utils.EqualDistanceTimestampWriter;
import tools.descartes.dlim.exporter.utils.TimeStampWriter;
import tools.descartes.dlim.generator.ArrivalRateTuple;

public class LoadProfileWithTimestampGeneration extends LoadProfileDecorator {
	private static final double AR_DEVISOR = 1.0;
	private static final double STRETCH = 1.0;
	private static final int DECIMAL_PLACES = 3;
	
	private TimeStampWriter writer = new EqualDistanceTimestampWriter();
	
	
	public LoadProfileWithTimestampGeneration(LoadProfile model) {
		super(model);
	}
	
	public void createTimestampFile(File file) {
		List<ArrivalRateTuple> arrList = getArrivalRates();
		writer.generateTimeStampsFromArrivalRates(file, arrList, DECIMAL_PLACES, STRETCH, AR_DEVISOR);
	}
}
