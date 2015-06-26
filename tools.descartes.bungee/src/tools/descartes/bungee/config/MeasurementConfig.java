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
package tools.descartes.bungee.config;

import java.io.File;
import java.util.Properties;

import tools.descartes.bungee.analysis.IntensityDemandMapping;
import tools.descartes.bungee.loadprofile.DlimAdapter;
import tools.descartes.bungee.loadprofile.LoadProfile;
import tools.descartes.bungee.utils.FileUtility;

public class MeasurementConfig {

	private static final String SECONDS_PER_TIME_UNIT_KEY = "secondsPerTimeUnit";
	private static final String RELATIVE_PATHES_KEY = "relativePathes";
	private static final String WARM_UP_SECONDS_KEY = "warmUpSeconds";
	private static final String INTENSITY_MODEL_FILE_KEY = "intensityModelFile";
	private static final String NUMBER_OF_BENCHMARKED_RESOURCES_KEY = "numberOfBenchmarkedResources";
	private static final String MAX_INTENSITY = "maxIntensity";
	private static final String MAPPING_FILE_KEY = "mappingFile";
	private static final String REQUEST_PROPERTIES_FILE_KEY = "requestPropertiesFile";
	private static final String HOST_PROPERTIES_FILE_KEY = "hostPropertiesFile";
	
	
	private Host host;
	private Request request;
	private LoadProfile loadProfile;
	private IntensityDemandMapping mapping;
	int numberOfBenchmarkedResources;
	double maxIntensity;
	int warmUpDurationInSeconds;
	
	public MeasurementConfig(Host host, Request request, LoadProfile loadProfile,
			IntensityDemandMapping mapping, int numberOfBenchmarkedResources,
			double maxIntensity, int warmUpDurationInSeconds) {
		super();
		this.host = host;
		this.request = request;
		this.loadProfile = loadProfile;
		this.mapping = mapping;
		this.numberOfBenchmarkedResources = numberOfBenchmarkedResources;
		this.maxIntensity = maxIntensity;
		this.warmUpDurationInSeconds = warmUpDurationInSeconds;
	}
	
	

	public Host getHost() {
		return host;
	}

	public Request getRequest() {
		return request;
	}

	public LoadProfile getLoadProfile() {
		return loadProfile;
	}


	public IntensityDemandMapping getMapping() {
		return mapping;
	}

	public int getNumberOfBenchmarkedResources() {
		return numberOfBenchmarkedResources;
	}

	public double getMaxIntensity() {
		return maxIntensity;
	}

	public int getWarmUpDurationInSeconds() {
		return warmUpDurationInSeconds;
	}


	public static MeasurementConfig load(File file)
	{
		Properties measurementProps = FileUtility.loadProperties(file);
		boolean relativePathes = Boolean.parseBoolean(measurementProps.getProperty(RELATIVE_PATHES_KEY,"false"));
		double secondsPerTimeUnit = Double.parseDouble(measurementProps.getProperty(SECONDS_PER_TIME_UNIT_KEY,"1.0"));
		String prepend = "";
		if (relativePathes)
		{
			prepend = file.getParent();
		}
		
		File hostPropertiesFile 	= new File(prepend,  measurementProps.getProperty(HOST_PROPERTIES_FILE_KEY));
		File requestPropertiesFile 	= new File(prepend,  measurementProps.getProperty(REQUEST_PROPERTIES_FILE_KEY));
		File mappingFile 			= new File(prepend,  measurementProps.getProperty(MAPPING_FILE_KEY));
		File modelFile 				= new File(prepend,  measurementProps.getProperty(INTENSITY_MODEL_FILE_KEY));
		
		Host host = Host.load(hostPropertiesFile);
		Request request = Request.load(requestPropertiesFile);
		DlimAdapter intensityModel = DlimAdapter.read(modelFile, secondsPerTimeUnit);
		IntensityDemandMapping mapping = IntensityDemandMapping.read(mappingFile);
		
		int numberOfBenchmarkedResources = Integer.parseInt(measurementProps.getProperty(NUMBER_OF_BENCHMARKED_RESOURCES_KEY,"1"));
		double maxIntensity = Double.parseDouble(measurementProps.getProperty(MAX_INTENSITY,Double.toString(intensityModel.getMaxIntensity())));
		int warmUpDurationInSeconds = Integer.parseInt(measurementProps.getProperty(WARM_UP_SECONDS_KEY,"0"));
		
		return new MeasurementConfig(host, request, intensityModel, mapping, numberOfBenchmarkedResources, maxIntensity, warmUpDurationInSeconds);
	}
	
	public void save(File file)
	{
		File inputFolder = new File(file.getParent(),"input");
		inputFolder.mkdirs();

		File hostFile = new File(inputFolder, "host.prop");
		File requestFile = new File(inputFolder, "request.prop");
		File mappingFile = new File(inputFolder, "mapping.mapping");
		File modelFile = new File(inputFolder, "intensityModel.dlim");
		
		host.save(hostFile);
		request.save(requestFile);
		mapping.save(mappingFile);
		
		double secondsPerTimeUnit = 1;
		if (loadProfile instanceof DlimAdapter) {
			DlimAdapter dlim = (DlimAdapter) loadProfile;
			dlim.save(modelFile);
			secondsPerTimeUnit = dlim.getSecondsPerTimeUnit();
		}

		File relativePath = new File(inputFolder.getName());
		Properties benchmarkProperties = new Properties();
		benchmarkProperties.put("relativePathes", Boolean.toString(true));
		benchmarkProperties.put(HOST_PROPERTIES_FILE_KEY, new File(relativePath, hostFile.getName()).toString());
		benchmarkProperties.put(REQUEST_PROPERTIES_FILE_KEY, new File(relativePath, requestFile.getName()).toString());
		benchmarkProperties.put(MAPPING_FILE_KEY, new File(relativePath, mappingFile.getName()).toString());
		benchmarkProperties.put(INTENSITY_MODEL_FILE_KEY, new File(relativePath, modelFile.getName()).toString());
		benchmarkProperties.put(MAX_INTENSITY, Double.toString(maxIntensity));
		benchmarkProperties.put(NUMBER_OF_BENCHMARKED_RESOURCES_KEY, Integer.toString(numberOfBenchmarkedResources));
		benchmarkProperties.put(WARM_UP_SECONDS_KEY, Integer.toString(warmUpDurationInSeconds));
		benchmarkProperties.put(SECONDS_PER_TIME_UNIT_KEY, Double.toString(secondsPerTimeUnit));
		FileUtility.saveProperties(benchmarkProperties, file);
	}
}
