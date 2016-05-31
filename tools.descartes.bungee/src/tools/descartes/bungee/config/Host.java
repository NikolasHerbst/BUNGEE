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

package tools.descartes.bungee.config;

import java.io.File;
import java.util.Properties;

import tools.descartes.bungee.utils.FileUtility;

public class Host {
	private static final String HOSTNAME = "hostname";
	private static final String PORT = "port";
	private static final String PATH = "path";	
	private static final String TAG = "tag";
	
	
	private String hostName;
	private int port;
	private String path;
	private String tag;
	
	public Host(String hostName, int port, String path, String tag) {
		super();
		this.hostName = hostName;
		this.port = port;
		this.path = path;
		this.tag = tag;
	}
	
	public Host(String hostName, int port) {
		super();
		this.hostName = hostName;
		this.port = port;
		this.path = "";
		this.tag = "";
	}

	
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getPath() {
		return path;
	}



	public void setPath(String path) {
		this.path = path;
	}



	public String getHostName() {
		return hostName;
	}

	public int getPort() {
		return port;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public static Host load(File file)
	{
		Properties hostProperties = FileUtility.loadProperties(file);
		return new Host(hostProperties.getProperty(HOSTNAME), Integer.parseInt(hostProperties.getProperty(PORT)), hostProperties.getProperty(PATH), hostProperties.getProperty(TAG));
	}
	
	public void save(File file)
	{
		Properties hostProperties = new Properties();
		hostProperties.setProperty(HOSTNAME, hostName);
		hostProperties.setProperty(PORT, Integer.toString(port));
		hostProperties.setProperty(PATH, path);
		hostProperties.setProperty(TAG, tag);
		FileUtility.saveProperties(hostProperties, file);
	}
}
