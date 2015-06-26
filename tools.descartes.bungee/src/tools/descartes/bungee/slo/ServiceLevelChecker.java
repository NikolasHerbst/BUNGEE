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

public class ServiceLevelChecker {

	public static boolean checkSLOs(List<? extends AbstractResponse> responses, ServiceLevelObjective... slos) {
		boolean compliant = true;
		for (ServiceLevelObjective slo : slos) {
			compliant &= slo.evaluate(responses);
		}
		return compliant;
	}

}
