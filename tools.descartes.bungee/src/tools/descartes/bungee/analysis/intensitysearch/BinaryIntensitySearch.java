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

package tools.descartes.bungee.analysis.intensitysearch;

import java.io.File;

import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.loadgeneration.JMeterController;
import tools.descartes.bungee.loadgeneration.RunResult;
import tools.descartes.bungee.loadprofile.DlimModelFactory;
import tools.descartes.bungee.loadprofile.LoadProfile;
import tools.descartes.bungee.loadprofile.LoadProfileWithTimestampGeneration;
import tools.descartes.bungee.loadprofile.LoadProfileWithWarmUp;
import tools.descartes.bungee.slo.ServiceLevelChecker;
import tools.descartes.bungee.slo.ServiceLevelObjective;

public class BinaryIntensitySearch implements IntensitySearch{

	private static final int MAX_RUNS = 3;
	private JMeterController jMeter;


	private int warmUpSeconds = 180;//240; // 30
	private int duration = 120;//60; // 10


	public BinaryIntensitySearch(File jmeterProperties) {
		this(new JMeterController(jmeterProperties));
	}

	public BinaryIntensitySearch(JMeterController jMeter) {
		this.jMeter = jMeter;
	}

	public int searchIntensity(Host host, Request request, double startIntensity, File calibrationFolder, ServiceLevelObjective... slos) {
		boolean finished = false;
		boolean foundLowerBound = false;
		boolean foundUpperBound = false;
		int lowerBound = ABORTED;
		int upperBound = ABORTED;
		double intervalThreshold = 1;

		int intensity = 1;

		while (!finished)
		{
			if (!foundLowerBound && !foundUpperBound)
			{
				intensity = (int) startIntensity;
			}
			System.out.println("Test system with intensity: " + intensity);
			boolean utilizableRun = false;
			boolean compliant = false;
			int run=1;
			RunResult result = null;
			while (!utilizableRun && run <= MAX_RUNS) {
				File intensityFolder = new File(calibrationFolder,"intensity."+Integer.toString(intensity)+".run"+run);
				intensityFolder.mkdirs();

				File timestampFile 		= new File(intensityFolder, "timestamps.csv");
				File responseFile 		= new File(intensityFolder, "responses.csv");
				File resultFile 		= new File(intensityFolder, "result.runresult");

				result = runConstantLoad(host, request, warmUpSeconds, intensity, duration, timestampFile, responseFile);
				result.save(resultFile);


				run++;
				if (result.isRunCompleted()) {
					boolean runCompliant = ServiceLevelChecker.checkSLOs(result.getResponses(), slos);
					utilizableRun = result.isRunCompleted() && ( result.isRunSuccessful() || !runCompliant);
					if (utilizableRun) {
						compliant = runCompliant;
					}
				} else {
					System.out.println("Run was not logged completely. Try again...");
				}
				try {
					Thread.sleep(1000*30);
					System.out.println("Wait for cool down");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (utilizableRun && result.passedSanityCheck())
			{
				if (compliant) {
					foundLowerBound = true;
					lowerBound = intensity;
					if (!foundUpperBound)
					{
						intensity *= 2;
					}
				} else {
					foundUpperBound = true;
					upperBound = intensity;
					if (!foundLowerBound)
					{
						intensity /= 2;
					}
				}
				if (foundLowerBound && foundUpperBound)
				{
					double intervalSize = upperBound - lowerBound;
					intensity = (lowerBound + upperBound) / 2;
					if (intervalSize <= intervalThreshold)
					{
						finished = true;
						intensity = lowerBound;
					}
				}
			} else {
				System.out.println("Either sanity check was not successful, or run was not completely logged. Abort Itensity search...");
				finished = true;
				intensity = ABORTED;
			}
		}
		return intensity;
	}

	public RunResult runConstantLoad(Host host, Request request, double warmUpSeconds, double intensity, double duration, File timestampFile, File responseFile) {
		LoadProfile constantLoad = DlimModelFactory.createConstantLoad(duration, intensity);
		constantLoad =  new LoadProfileWithWarmUp(constantLoad, warmUpSeconds);

		LoadProfileWithTimestampGeneration timestampModel = new LoadProfileWithTimestampGeneration(constantLoad);
		timestampModel.createTimestampFile(timestampFile);

		// run JMeter with created timestamp file
		jMeter.runJMeter(host, request, timestampFile, responseFile);
		return new RunResult(timestampFile, responseFile, warmUpSeconds);
	}

}
