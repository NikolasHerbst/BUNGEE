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

package tools.descartes.bungee.loadgeneration;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import tools.descartes.bungee.utils.DateUtility;
import tools.descartes.bungee.utils.DiffUtil;
import tools.descartes.bungee.utils.FileUtility;
import tools.descartes.bungee.utils.TimestampUtils;

public class RunResult {
	
	private static final String TIMESTAMP_FILE_KEY = "timestampFile";
	private static final String RESPONSE_FILE_KEY = "responseFile";
	private static final String WARM_UP_SECONDS_KEY = "warmUpSeconds";
	private static final double PERCENTILE = 95;
	private static final double ALLOWED_PERCENTILE_DELAY = 5;
	private boolean 		successful;
	private File 			timestampsFile;
	private File 			responseFile;
	private int 		  	warmUpRequests;
	private double 			warmUpSeconds;
	private boolean 		sanityCheck;
	private boolean 		runCompleted;
	
	private DescriptiveStatistics timingStats;
	private DescriptiveStatistics responseTimeStats;
	private Date startRun;
	
	List<JMeterResponse> responses;
	List<Long> relativeTimestamps;
	List<Long> timestampDiffs;
	private boolean timingOK;
	
	public RunResult(File timestampsFile, File responesFile, double warmUpSeconds) {
		super();
		this.successful = false;
		this.timestampsFile = timestampsFile;
		this.responseFile = responesFile;
		this.sanityCheck = false;
		this.runCompleted = false;
		this.warmUpSeconds = warmUpSeconds;
		
		responses = new LinkedList<JMeterResponse>();
		relativeTimestamps = new LinkedList<Long>();
		timestampDiffs = new LinkedList<Long>();
		readResult();
	}

	private void readResult() {
		boolean readResponesOk = JMeterResponse.readJMeterResponseFile(this.responseFile, FileUtility.CSV_SPLIT_BY, responses);
		boolean readTimestampsOk = TimestampUtils.readTimestampFile(this.timestampsFile, FileUtility.CSV_SPLIT_BY, relativeTimestamps);

		if (readResponesOk && readTimestampsOk)
		{
			sanityCheck = SanityChecker.sanityCheck(responses);
			System.out.println("Sanity Check: " + passed(sanityCheck));
			
			startRun = new Date(responses.get(0).getExperimentStart());
			
			runCompleted = relativeTimestamps.size() == responses.size();
			warmUpRequests = warmUpRequests();
			
			relativeTimestamps = ResponseUtils.cutWarmUpRequests(warmUpRequests, relativeTimestamps);
			responses = ResponseUtils.cutWarmUpRequests(warmUpRequests, responses);
			timestampDiffs = ResponseUtils.getSubmitDiffs(responses, relativeTimestamps);

			timingStats = DiffUtil.createDescriptiveStatistics(timestampDiffs);
			timingOK = (Math.abs(timingStats.getPercentile(PERCENTILE)) <= ALLOWED_PERCENTILE_DELAY);
			System.out.println("Schedule Check: " + passed(timingOK) + " deviation mean: " + timingStats.getMean() 
					+ " std: " + timingStats.getStandardDeviation() + " " + PERCENTILE + "%-percentile: " + timingStats.getPercentile(PERCENTILE)+")");
			System.out.println("Max diff: " + timingStats.getMax());
			
			if (!timingOK)
			{
				System.out.println("Warning: Send timestamps differ significantly from schedule times!");
			}
			responseTimeStats = DiffUtil.createDescriptiveStatistics(responses);
			System.out.println("Response Time (mean): " + responseTimeStats.getMean());
			if (runCompleted && sanityCheck && timingOK) {
				successful = true;
			}
		}
	}

	private int warmUpRequests() {
		int requests = 0;
		Iterator<Long> iterator = relativeTimestamps.iterator();
		while (iterator.hasNext() && iterator.next() < warmUpSeconds * DateUtility.SECOND_TO_MILLI) {
			requests++;
		}
		return requests;
	}
	
	public void save(File file) 
	{
		Properties properties = new Properties();
		properties.setProperty(TIMESTAMP_FILE_KEY, FileUtility.getRelativeFilePath(timestampsFile, file.getParentFile()).toString());
		properties.setProperty(RESPONSE_FILE_KEY, FileUtility.getRelativeFilePath(responseFile, file.getParentFile()).toString());
		properties.setProperty(WARM_UP_SECONDS_KEY, Double.toString(warmUpSeconds));
		FileUtility.saveProperties(properties, file);
	}
	
	public static RunResult read(File file) 
	{
		Properties properties = FileUtility.loadProperties(file);
		File timestampsFile = new File(file.getParentFile(),properties.getProperty(TIMESTAMP_FILE_KEY));
		File responseFile = new File(file.getParentFile(),properties.getProperty(RESPONSE_FILE_KEY));
		double warmUpSeconds = Double.parseDouble(properties.getProperty(WARM_UP_SECONDS_KEY));
		return new RunResult(timestampsFile, responseFile, warmUpSeconds);
	}
	
	public static boolean isRunresult(File file) 
	{
		boolean isRunResult = false;
		if (file.exists() && file.isFile() && file.getName().endsWith(".runresult"))
		{
			isRunResult = true;
		}
		return isRunResult;
	}

	public Date getStartRunTime() {
		return startRun;
	}

	public File getTimestampsFile() {
		return timestampsFile;
	}

	public File getResponseFile() {
		return responseFile;
	}
	
	public DescriptiveStatistics getTimingStats() {
		return timingStats;
	}

	public DescriptiveStatistics getResponseTimeStats() {
		return responseTimeStats;
	}
	
	public boolean isRunSuccessful() {
		return successful;
	}

	public int getWarmUpRequests() {
		return warmUpRequests;
	}

	public List<JMeterResponse> getResponses() {
		return responses;
	}

	public List<Long> getTimestampDiffs() {
		return timestampDiffs;
	}
	
	
	public boolean passedSanityCheck() {
		return sanityCheck;
	}
	
	public boolean passedScheduleCheck() {
		return timingOK;
	}
	
	public boolean isRunCompleted() {
		return runCompleted;
	}

	private static String passed(boolean passed)
	{
		if (passed)
		{
			return "passed";
		} else {
			return "failed";
		}
	}
}
