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

import java.util.List;

import tools.descartes.dlim.generator.ArrivalRateTuple;

public class LoadProfileWithWarmUp extends LoadProfileDecorator {
	private LoadProfile warmUpProfile;
	private double warmUpSeconds;

	public LoadProfileWithWarmUp(LoadProfile model, double warmUpSeconds) {
		super(model);
		warmUpProfile = DlimModelFactory.createConstantLoad(warmUpSeconds, model.getIntensity(0)); 
		this.warmUpSeconds = warmUpSeconds;
	}
	
	@Override
	public double getIntensity(double time) {
		if (time < warmUpSeconds) {
			return warmUpProfile.getIntensity(time);
		} else {
			return super.getIntensity(time-warmUpSeconds);
		}
		
	}

	@Override
	public List<ArrivalRateTuple> getArrivalRates() {
		List<ArrivalRateTuple> arrivalRates = warmUpProfile.getArrivalRates();
		List<ArrivalRateTuple> modelArrivalRates = super.getArrivalRates();
		for (ArrivalRateTuple tuple : modelArrivalRates) {
			tuple.setTimeStamp(tuple.getTimeStamp()+warmUpSeconds);
		}
		arrivalRates.addAll(modelArrivalRates);
		return arrivalRates;
	}

	@Override
	public double getDuration() {
		return warmUpProfile.getDuration() + super.getDuration();
	}
	
	@Override
	public double getMaxIntensity() {
		return Math.max(warmUpProfile.getMaxIntensity(), super.getMaxIntensity());
	}
	
	/**
	 * Return the duration of the warm up period in seconds
	 * @return duration of the warm up period [seconds]
	 */
	public double getWarmUpSeconds() {
		return warmUpSeconds;
	}
}
