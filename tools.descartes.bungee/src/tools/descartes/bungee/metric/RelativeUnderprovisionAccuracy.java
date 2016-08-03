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

package tools.descartes.bungee.metric;

import java.util.List;

import tools.descartes.bungee.allocation.ResourceAllocation;
import tools.descartes.bungee.utils.FileUtility;

public class RelativeUnderprovisionAccuracy extends AbstractRelativeAreaDuration {
	String explanation = null;
	String csvString = null;
	
	
	public RelativeUnderprovisionAccuracy(List<ResourceAllocation> demand,
			List<ResourceAllocation> supply) {
		super(demand, supply);
	}

	@Override
	protected double evaluate() {
		Provisioning provision = getProvisioning();
		long duration = getDuration();
		double result = provision.amountUnder / duration;
		gernerateExplanantion(provision.amountUnder, result);
		generateCSV(provision.amountUnder, result);
		return result;
	}
	
	private void generateCSV(double underprovisionArea, double result) {
		csvString = "relative_underprovision_accuracy" + FileUtility.CSV_SPLIT_BY + result + System.lineSeparator();
		csvString += "duration [min]" + FileUtility.CSV_SPLIT_BY + getDuration() / MINUTE_TO_MILLIS + System.lineSeparator();
		csvString += "relative_underprovision_area" + FileUtility.CSV_SPLIT_BY + underprovisionArea + System.lineSeparator();
		
	}

	private void gernerateExplanantion(double overprovisionArea, double result) {
		explanation = "relative_underprovision_accuracy: " + result + System.lineSeparator();
		explanation += "duration [min]: " + getDuration() / MINUTE_TO_MILLIS  + System.lineSeparator();
		explanation += "relative_underprovision_area: " + overprovisionArea + System.lineSeparator();
	}

	@Override
	protected String textShort() {
		if (explanation == null) {
			evaluate();
		}
		return explanation;
	}

	@Override
	protected String csvString() {
		if (csvString == null) {
			evaluate();
		}
		return csvString;
	}

	@Override
	public String getName() {
		return "relative_accuracy_U";
	}

}
