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

package tools.descartes.bungee.examples;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import tools.descartes.bungee.loadgeneration.JMeterResponse;
import tools.descartes.bungee.loadgeneration.RunResult;
import tools.descartes.bungee.utils.FileUtility;

public class DiffsAndViolations {
	/**
	 * Starting point for the demonstration application.
	 *
	 * @param args  ignored.
	 */
	public static void main(final String[] args) {
		File measurementFolder = new File(FileUtility.FILE_LOCATION, "measurement/141.21.72.21_500000_2014.04.01_15.41.14");
		
		diffsAndViolations(measurementFolder);
	}

	static void diffsAndViolations(File measurementFolder) {
		RunResult result = RunResult.read(new File(measurementFolder, "result.runresult"));
		
		List<Long> timestampDiffs = result.getTimestampDiffs();
		try {
			File file = new File(measurementFolder, "diffs.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (long diff : timestampDiffs) {
				bw.write(Long.toString(diff) + System.getProperty("line.separator"));
			}
			bw.close();
 
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		List<JMeterResponse> responses = result.getResponses();
		int count = 0;
		for (JMeterResponse response: responses) {
			if (response.getResponseTime() > 500 || !response.isSuccess()) {
				count++;
			}
		}
		double violationRatio = (double) count /  responses.size();
		System.out.println("Violations: " + violationRatio);
		 
		try {
			File file = new File(measurementFolder, "violations.csv");
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("violations" + FileUtility.CSV_SPLIT_BY + "total" + FileUtility.CSV_SPLIT_BY + "ratio" + System.getProperty("line.separator"));
			bw.write(count + FileUtility.CSV_SPLIT_BY  + responses.size()+ FileUtility.CSV_SPLIT_BY + violationRatio);
			bw.close();
 
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
