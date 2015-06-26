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

package tools.descartes.bungee.slo;

import java.util.List;

import tools.descartes.bungee.loadgeneration.AbstractResponse;
import tools.descartes.bungee.utils.DiffUtil;
import tools.descartes.bungee.utils.StatsPercentile;

public class ResponsetimePercentileSLO implements ServiceLevelObjective {

	private double percent;
	private int maxRespTimeMS;
	
	
	
	
	public ResponsetimePercentileSLO(double percent, int maxRespTimeMS) {
		super();
		this.percent = percent;
		this.maxRespTimeMS = maxRespTimeMS;
	}

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	public double getResponseTime() {
		return maxRespTimeMS;
	}

	public void setResponseTime(int responseTime) {
		this.maxRespTimeMS = responseTime;
	}

	@Override
	public boolean evaluate(List<? extends AbstractResponse> responses) {
		StatsPercentile stats = DiffUtil.statisticsForDiffs(responses, percent);
		boolean compliant = (stats.percentile <= maxRespTimeMS);
		System.out.println("SLA-compliance: " + compliant + " percentile(" + percent + "%)=" + stats.percentile);
		return compliant;
	}

}
