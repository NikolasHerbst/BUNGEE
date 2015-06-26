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

public class SuccessRateSLO implements ServiceLevelObjective{
	private double successPercent;
	private double maxRespTimeMS;
	
	public SuccessRateSLO(double successPercent, double maxRespTimeMS) {
		this.successPercent = successPercent;
		this.maxRespTimeMS = maxRespTimeMS;
	}

	public double getSuccessPercent() {
		return successPercent;
	}

	public void setSuccessPercent(double successPercent) {
		this.successPercent = successPercent;
	}

	public double getMaxTimeMS() {
		return maxRespTimeMS;
	}

	public void setMaxTimeMS(double maxTimeMS) {
		this.maxRespTimeMS = maxTimeMS;
	}


	@Override
	public boolean evaluate(List<? extends AbstractResponse> responses) {
		long count = 0;
		for (AbstractResponse response : responses)
		{
			if (response.isSuccess() && (response.getResponseTime() <= maxRespTimeMS)) {
				count++;
			}
		}
		double successRate = (double) count / responses.size();
		boolean compliant = successRate >= this.successPercent / 100;
		System.out.println("SLO-compliance: " + compliant + " successRate: " + successRate);
		return compliant;
	}

}
