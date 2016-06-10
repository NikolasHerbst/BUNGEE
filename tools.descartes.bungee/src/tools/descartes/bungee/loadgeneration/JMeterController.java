/*******************************************************************************
Copyright 2015 Andreas Weber, Nikolas Herbst
Edited by André Bauer, 2016

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;

import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.config.Request;
import tools.descartes.bungee.utils.FileUtility;
import tools.descartes.bungee.utils.TimestampUtils;

public class JMeterController {
	
	private static final String VM_ARGS = "-Xms1024m -Xmx1024m";

	private static final double SCALE_UP_REQUESTS = 5;
	
	private boolean noGui;
	private File 	jMeterPath;
	private File 	jmxFile; 
	private File 	properyFile;
	
	public JMeterController(File jmeterProperties) {
		this(FileUtility.loadProperties(jmeterProperties));
	}
	
	
	public JMeterController(Properties jmeterProperties) {
		super();
		this.noGui = Boolean.parseBoolean(jmeterProperties.getProperty("noGui"));
		this.jMeterPath = new File(jmeterProperties.getProperty("jMeterPath"));
		this.jmxFile = new File(jmeterProperties.getProperty("jmxFile"));
		this.properyFile = new File(jmeterProperties.getProperty("propertyFile"));
		
	}

	public void runJMeter(Host host, Request request, File timestampFile, File outputFile) {
		int threads = getNumberOfThreadsForTimestampFile(timestampFile, request.getTimeout());
		System.out.println("Required Threads: " + threads);
		threads = (int) (threads * SCALE_UP_REQUESTS);
		System.out.println("Used Threads: " + threads);
		runJMeter(outputFile, host.getHostName(), host.getPort(), host.getPath(), 
				threads, request.getProblemSize(),  request.getTimeout(), timestampFile);
	}


	private int getNumberOfThreadsForTimestampFile(File timestampFile, int timeout) {
		List<Long> relativeTimestamps = new LinkedList<Long>();
		TimestampUtils.readTimestampFile(timestampFile, FileUtility.CSV_SPLIT_BY, relativeTimestamps);
		Queue<Long> concurrentRequests = new LinkedList<Long>();
		int threads = 0;
		for (Long timestamp : relativeTimestamps) {
			concurrentRequests.offer(timestamp);
			Long oldestTimestamp = concurrentRequests.peek();
			while (oldestTimestamp != null && oldestTimestamp + timeout < timestamp)
			{
				concurrentRequests.poll();
				oldestTimestamp = concurrentRequests.peek();
			}
			threads = Math.max(threads, concurrentRequests.size());
		}
		return threads;
	}
	
	private void runJMeter(File outputFile, String hostname,  int port, String path,
			int threads, int problemSize,  int timeout, File timestampFile) {

		//remapIp
		hostname = IPMap.getInstance().getPublicIP(hostname);
		
		// generate parameters for run
		String params = generateJMeterParameters(noGui, jmxFile,
				timestampFile, outputFile, properyFile, hostname, port, path, threads,
				problemSize, timeout);
		
		// get duration of run
		List<Long> relativeTimestamps = new LinkedList<Long>();
		TimestampUtils.readTimestampFile(timestampFile, FileUtility.CSV_SPLIT_BY, relativeTimestamps);
		long duration = 0;
		if (relativeTimestamps.size() > 0) {
			duration = relativeTimestamps.get(relativeTimestamps.size() -1) + timeout;
		}
		long start = System.currentTimeMillis();
		// run JMeter
		try {
			ProgressController progressController = new ProgressUI();
			System.out.println("java " + VM_ARGS +" -jar " + enquote(jMeterPath.toString()) + params);
			Process process = Runtime.getRuntime().exec ("java " + VM_ARGS +" -jar " + enquote(jMeterPath.toString()) + params);
			progressController.processStarted(process, start + duration);
			 // getInputStream gives an Input stream connected to
		    // the process p's standard output. Just use it to make
		    // a BufferedReader to readLine() what the program writes out.
			BufferedReader is = new BufferedReader(new InputStreamReader(process.getInputStream()));
		    String line;
			while ((line = is.readLine()) != null) {
				System.out.println(line);
				progressController.currentProgress((System.currentTimeMillis() - start) / (double) duration);
			}
		    System.out.flush();
		    try {
		      process.waitFor();  // wait for process to complete
		    } catch (InterruptedException e) {
		      System.err.println(e);  // "Can'tHappen"
		      return;
		    }
		    progressController.finished();
		    System.out.println("JMeter is done, exit status was " + process.exitValue());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String generateJMeterParameters(boolean noGui, File jmxFile, File timestampFile,
			File outputFile, File properyFile, String hostname, int port, String path, int threads, int problemSize, int timeout) {
		properyFile = FileUtility.getAbsolutePath(properyFile);
		jmxFile = FileUtility.getAbsolutePath(jmxFile);
		String params = " -t " + enquote(jmxFile.toString()) 
				               + " -p " + enquote(properyFile.toString())
							   + " -JtimestampFile=" + enquote(timestampFile.toString())
							   + " -JoutputFile=" + enquote(outputFile.toString())
							   + " -Jhostname=" + hostname
							   + " -Jport=" + port
							   + " -Jpath=" + path
				               + " -JnumberOfThreads=" + threads
				               + " -JproblemSize=" + problemSize
							   + " -Jtimeout=" + timeout ;
		if (noGui) {
			params += " -n";
		}
		return params;
	}
	
	private static String enquote(String text) {
		return "\"" + text + "\"";
	}
}
