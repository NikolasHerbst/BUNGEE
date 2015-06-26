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

package tools.descartes.bungee.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for reading timestamp files
 * Timestamp files must contain relative timestamps in seconds. Each line in the timestamp file must
 * contain one timestamp which is followed by a semicolon (;).
 * One option for creating timestamp files is the workload modeling tool LIMBO:
 * @see <a href="http://sdqweb.ipd.kit.edu/mediawiki-descartes/index.php/Tools#LIMBO:_Load_Intensity_Modeling_Tool">LIMBO Website</a>
 * 
 * Examplary file content for a timestamp file:
 * 0.5;
 * 1;
 * 1,5;
 * 2;
 * 
 * 
 * @author Andreas Weber <andreas.weber4@student.kit.edu>
 */
public class TimestampUtils {

	private static final int SECONDS_TO_MILLISECONDS = 1000;

	/**
	 * Read a timestamp file and adds the timestamps into a list
	 * @param file location of timestamp file
	 * @param csvSplitBy symbol which ends each line (e.g. semicolon)
	 * @param timestamps list where timestamps (long values which specify timestamp in milliseconds) will be added.
	 * @return true if and only if it was possible to open and parse the timestamp file
	 */
	public static boolean readTimestampFile(File file,
			String csvSplitBy, List<Long> timestamps) {
		boolean result = false;
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				String[] timestampString = line.split(csvSplitBy);
				long timestamp		= (long) (SECONDS_TO_MILLISECONDS * Double.parseDouble(timestampString[0]));
				timestamps.add(timestamp);
			}
			result = true;
		} catch (FileNotFoundException e) {
			System.out.println("Could not find file: " + file);
		} catch (IOException e) {
			System.out.println("Error reading file: " + file);
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

}
