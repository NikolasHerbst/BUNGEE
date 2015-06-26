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

import tools.descartes.dlim.Constant;
import tools.descartes.dlim.DlimFactory;
import tools.descartes.dlim.DlimPackage;
import tools.descartes.dlim.Sequence;
import tools.descartes.dlim.TimeDependentFunctionContainer;

public class DlimModelFactory {

	public static DlimAdapter createConstantLoad(double duration, double intensity) {
		TimeDependentFunctionContainer container = DlimModelFactory.getDlimFactory().createTimeDependentFunctionContainer();
		container.setDuration(duration);
		Constant constantLoad = DlimModelFactory.getDlimFactory().createConstant();
		constantLoad.setConstant(intensity);
		container.setFunction(constantLoad);
		
		Sequence constantSequence = DlimModelFactory.getDlimFactory().createSequence();
		constantSequence.setTerminateAfterLoops(1);
		constantSequence.getSequenceFunctionContainers().add(0, container);
		constantSequence.setName("Constant" + intensity);
		
		DlimAdapter dlim = new DlimAdapter(constantSequence, 1);
		return dlim;
	}
	
	public static DlimAdapter concatenate(DlimAdapter dlim1, DlimAdapter dlim2) {
		double secondsPerTimeUnit1 = dlim1.getSecondsPerTimeUnit();
		double secondsPerTimeUnit2 = dlim2.getSecondsPerTimeUnit();
		DlimAdapter dlim = null;
		if (secondsPerTimeUnit1 == secondsPerTimeUnit2) {
			Sequence concatenatedSequence = concatenateToSequence(dlim1, dlim2);
			dlim = new DlimAdapter(concatenatedSequence, secondsPerTimeUnit1);
		}
		return dlim;
	}

    static Sequence concatenateToSequence(DlimAdapter dlim1, DlimAdapter dlim2) {	
		Sequence concatenatedSequence = DlimModelFactory.getDlimFactory().createSequence();
		concatenatedSequence.setTerminateAfterLoops(1);
		concatenatedSequence.setName("Concatenated");
		concatenatedSequence.getSequenceFunctionContainers().addAll(0, dlim2.getRootSequence().getSequenceFunctionContainers());
		concatenatedSequence.getSequenceFunctionContainers().addAll(0, dlim1.getRootSequence().getSequenceFunctionContainers());
		return concatenatedSequence;
	}

	static DlimFactory getDlimFactory() {
		return DlimPackage.eINSTANCE.getDlimFactory();
	}
}
