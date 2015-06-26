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

import tools.descartes.bungee.utils.FileUtility;

public class Request {
	private static final String PROBLEM_SIZE = "problemSize";
	private static final String TIMEOUT = "timeout";
	
	private int problemSize;
	private int timeout;
	
	public Request(int problemSize, int timeout) {
		super();
		this.problemSize = problemSize;
		this.timeout = timeout;
	}

	public int getProblemSize() {
		return problemSize;
	}

	public int getTimeout() {
		return timeout;
	}

	public static Request load(File file)
	{
		Properties requestProperties = FileUtility.loadProperties(file);
		return new Request(Integer.parseInt(requestProperties.getProperty(PROBLEM_SIZE)), Integer.parseInt(requestProperties.getProperty(TIMEOUT)));
	}
	
	public void save(File file)
	{
		Properties requestProperties = new Properties();
		requestProperties.setProperty(PROBLEM_SIZE, Integer.toString(problemSize));
		requestProperties.setProperty(TIMEOUT, Integer.toString(timeout));
		FileUtility.saveProperties(requestProperties, file);
	}
}
