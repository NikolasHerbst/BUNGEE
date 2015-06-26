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

package tools.descartes.bungee.analysis;

import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.slo.ServiceLevelObjective;

public abstract class SystemAnalysis {
	protected int maxResources = 1;
	
	public abstract IntensityDemandMapping analyzeSystem(Host host, Request request, ServiceLevelObjective... slos);

	public int getMaxResources() {
		return maxResources;
	}

	public void setMaxResources(int maxResources) {
		this.maxResources = maxResources;
	}
}
