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

public abstract class LoadProfileDecorator implements LoadProfile {
	private LoadProfile loadProfile;
	
	protected LoadProfileDecorator(LoadProfile intensityModel) {
		this.loadProfile = intensityModel;
	}

	@Override
	public double getIntensity(double time) {
		return loadProfile.getIntensity(time);
	}

	@Override
	public List<ArrivalRateTuple> getArrivalRates() {
		return loadProfile.getArrivalRates();
	}

	@Override
	public double getDuration() {
		return loadProfile.getDuration();
	}
	
	@Override
	public double getMaxIntensity() {
		return loadProfile.getMaxIntensity();
	}
	
}
