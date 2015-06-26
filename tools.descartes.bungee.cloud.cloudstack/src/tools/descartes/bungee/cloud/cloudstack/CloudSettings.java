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
package tools.descartes.bungee.cloud.cloudstack;

import java.io.File;
import java.util.Properties;

import tools.descartes.bungee.utils.FileUtility;

public class CloudSettings {
	private static final String HEALTH_CHECK_UNHEALTHY_THRESHOLD = "healthCheckUnhealthyThreshold";
	private static final String HEALTH_CHECK_HEALTHY_THRESHOLD = "healthCheckHealthyThreshold";
	private static final String HEALTH_CHECK_TIMEOUT = "healthCheckTimeout";
	private static final String HEALTH_CHECK_INTERVAL = "healthCheckInterval";
	private static final String HEALTH_CHECK_PING_PATH = "healthCheckPingPath";
	private static final String SCALE_UP = "scaleUp.";
	private static final String SCALE_DOWN = "scaleDown.";
	private static final String MAX_INSTANCES = "maxInstances";
	private static final String MIN_INSTANCES = "minInstances";
	private static final String DESTROY_VM_GRACE_PERIOD = "destroyVmGracePeriod";
	private static final String EVAL_INTERVAL = "evalInterval";
	private static final String TEMPLATE = "template";
	private static final String OFFERING = "serviceOffering";
	private static final String PUBLIC_PORT = "publicPort";
	private static final String PRIVATE_PORT = "privatePort";
	private static final String ALGORITHM = "algorithm";
	private static final String IP = "ip";
	private static final String NAME = "name";

	public enum Algorithm {
		SOURCE,
		ROUNDROBIN,
		LEASTCONN
	}
	
	private String name;
	private String ip;
	private Algorithm algorithm;
	private int privatePort;
	private int publicPort;
	private String offering;
	private String template;
	private int evalInterval;
	private Policiy scaleUp;
	private Policiy scaleDown;
	private int destroyVmGracePeriod;
	private int minInstances;
	private int maxInstances;
	private String healthCheckPingPath;
	private int healthCheckInterval;
	private int healthCheckTimeout;
	private int healthCheckHealthyThreshold;
	private int healthCheckUnhealthyThreshold;
	
	

	public CloudSettings(String name, String ip, Algorithm algorithm,
			int privatePort, int publicPort, String offering, String template,
			int evalInterval, Policiy scaleUp, Policiy scaleDown,
			int destroyVmGracePeriod, int minInstances, int maxInstances,
			String healthCheckPingPath, int healthCheckInterval,
			int healthCheckTimeout, int healthCheckHealthyThreshold,
			int healthCheckUnhealthyThreshold) {
		super();
		this.name = name;
		this.ip = ip;
		this.algorithm = algorithm;
		this.privatePort = privatePort;
		this.publicPort = publicPort;
		this.offering = offering;
		this.template = template;
		this.evalInterval = evalInterval;
		this.scaleUp = scaleUp;
		this.scaleDown = scaleDown;
		this.destroyVmGracePeriod = destroyVmGracePeriod;
		this.minInstances = minInstances;
		this.maxInstances = maxInstances;
		this.healthCheckPingPath = healthCheckPingPath;
		this.healthCheckInterval = healthCheckInterval;
		this.healthCheckTimeout = healthCheckTimeout;
		this.healthCheckHealthyThreshold = healthCheckHealthyThreshold;
		this.healthCheckUnhealthyThreshold = healthCheckUnhealthyThreshold;
	}

	public String getName() {
		return name;
	}

	public String getIp() {
		return ip;
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	public int getPrivatePort() {
		return privatePort;
	}

	public int getPublicPort() {
		return publicPort;
	}

	public String getOffering() {
		return offering;
	}

	public String getTemplate() {
		return template;
	}

	public int getEvalInterval() {
		return evalInterval;
	}

	public Policiy getScaleUp() {
		return scaleUp;
	}

	public Policiy getScaleDown() {
		return scaleDown;
	}

	public int getDestroyVmGracePeriod() {
		return destroyVmGracePeriod;
	}

	public int getMinInstances() {
		return minInstances;
	}

	public int getMaxInstances() {
		return maxInstances;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}

	public void setPrivatePort(int privatePort) {
		this.privatePort = privatePort;
	}

	public void setPublicPort(int publicPort) {
		this.publicPort = publicPort;
	}

	public void setOffering(String offering) {
		this.offering = offering;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public void setEvalInterval(int evalInterval) {
		this.evalInterval = evalInterval;
	}

	public void setScaleUp(Policiy scaleUp) {
		this.scaleUp = scaleUp;
	}

	public void setScaleDown(Policiy scaleDown) {
		this.scaleDown = scaleDown;
	}

	public void setDestroyVmGracePeriod(int destroyVmGracePeriod) {
		this.destroyVmGracePeriod = destroyVmGracePeriod;
	}

	public void setMinInstances(int minInstances) {
		this.minInstances = minInstances;
	}

	public void setMaxInstances(int maxInstances) {
		this.maxInstances = maxInstances;
	}
	
	public String getHealthCheckPingPath() {
		return healthCheckPingPath;
	}

	public void setHealthCheckPingPath(String healthCheckPingPath) {
		this.healthCheckPingPath = healthCheckPingPath;
	}

	public int getHealthCheckInterval() {
		return healthCheckInterval;
	}

	public void setHealthCheckInterval(int healthCheckInterval) {
		this.healthCheckInterval = healthCheckInterval;
	}

	public int getHealthCheckTimeout() {
		return healthCheckTimeout;
	}

	public void setHealthCheckTimeout(int healthCheckTimeout) {
		this.healthCheckTimeout = healthCheckTimeout;
	}

	public int getHealthCheckHealthyThreshold() {
		return healthCheckHealthyThreshold;
	}

	public void setHealthCheckHealthyThreshold(int healthCheckHealthyThreshold) {
		this.healthCheckHealthyThreshold = healthCheckHealthyThreshold;
	}

	public int getHealthCheckUnhealthyThreshold() {
		return healthCheckUnhealthyThreshold;
	}

	public void setHealthCheckUnhealthyThreshold(int healthCheckUnhealthyThreshold) {
		this.healthCheckUnhealthyThreshold = healthCheckUnhealthyThreshold;
	}

	public static CloudSettings load(File file)
	{
		Properties prop = FileUtility.loadProperties(file);
		Policiy policyUp = new Policiy(prop.getProperty(SCALE_UP + Policiy.COUNTER), prop.getProperty(SCALE_UP + Policiy.OPERATOR), Integer.parseInt(prop.getProperty(SCALE_UP + Policiy.THRESHOLD)), Integer.parseInt(prop.getProperty(SCALE_UP + Policiy.DURATION)), Integer.parseInt(prop.getProperty(SCALE_UP + Policiy.QUIETTIME)));
		Policiy policyDown = new Policiy(prop.getProperty(SCALE_DOWN + Policiy.COUNTER), prop.getProperty(SCALE_DOWN + Policiy.OPERATOR), Integer.parseInt(prop.getProperty(SCALE_DOWN + Policiy.THRESHOLD)), Integer.parseInt(prop.getProperty(SCALE_DOWN + Policiy.DURATION)), Integer.parseInt(prop.getProperty(SCALE_DOWN + Policiy.QUIETTIME)));
		return new CloudSettings(prop.getProperty(NAME), prop.getProperty(IP), Algorithm.valueOf(prop.getProperty(ALGORITHM)), 
				Integer.parseInt(prop.getProperty(PRIVATE_PORT)), Integer.parseInt(prop.getProperty(PUBLIC_PORT)), prop.getProperty(OFFERING), 
				prop.getProperty(TEMPLATE), Integer.parseInt(prop.getProperty(EVAL_INTERVAL)), policyUp, policyDown, 
				Integer.parseInt(prop.getProperty(DESTROY_VM_GRACE_PERIOD)), Integer.parseInt(prop.getProperty(MIN_INSTANCES)), Integer.parseInt(prop.getProperty(MAX_INSTANCES)), prop.getProperty(HEALTH_CHECK_PING_PATH),Integer.parseInt(prop.getProperty(HEALTH_CHECK_INTERVAL)), Integer.parseInt(prop.getProperty(HEALTH_CHECK_TIMEOUT)), Integer.parseInt(prop.getProperty(HEALTH_CHECK_HEALTHY_THRESHOLD)), Integer.parseInt(prop.getProperty(HEALTH_CHECK_UNHEALTHY_THRESHOLD)));
	}
	
	public void save(File file)
	{
		Properties prop = new Properties();
		prop.setProperty(NAME, name);
		prop.setProperty(IP, ip);
		prop.setProperty(ALGORITHM, algorithm.toString());
		prop.setProperty(PRIVATE_PORT, Integer.toString(privatePort));
		prop.setProperty(PUBLIC_PORT, Integer.toString(publicPort));
		prop.setProperty(OFFERING, offering);
		prop.setProperty(TEMPLATE, template);
		prop.setProperty(EVAL_INTERVAL, Integer.toString(evalInterval));
		prop.setProperty(SCALE_UP + Policiy.COUNTER, scaleUp.getCounter());
		prop.setProperty(SCALE_UP + Policiy.OPERATOR, scaleUp.getOperator());
		prop.setProperty(SCALE_UP + Policiy.THRESHOLD, Integer.toString(scaleUp.getThreshold()));
		prop.setProperty(SCALE_UP + Policiy.DURATION, Integer.toString(scaleUp.getDuration()));
		prop.setProperty(SCALE_UP + Policiy.QUIETTIME, Integer.toString(scaleUp.getQuiettime()));
		prop.setProperty(SCALE_DOWN + Policiy.COUNTER, scaleDown.getCounter());
		prop.setProperty(SCALE_DOWN + Policiy.OPERATOR, scaleDown.getOperator());
		prop.setProperty(SCALE_DOWN + Policiy.THRESHOLD, Integer.toString(scaleDown.getThreshold()));
		prop.setProperty(SCALE_DOWN + Policiy.DURATION, Integer.toString(scaleDown.getDuration()));
		prop.setProperty(SCALE_DOWN + Policiy.QUIETTIME, Integer.toString(scaleDown.getQuiettime()));
		prop.setProperty(DESTROY_VM_GRACE_PERIOD, Integer.toString(destroyVmGracePeriod));
		prop.setProperty(MIN_INSTANCES, Integer.toString(minInstances));
		prop.setProperty(MAX_INSTANCES, Integer.toString(maxInstances));
		prop.setProperty(HEALTH_CHECK_PING_PATH, healthCheckPingPath);
		prop.setProperty(HEALTH_CHECK_INTERVAL, Integer.toString(healthCheckInterval));
		prop.setProperty(HEALTH_CHECK_TIMEOUT, Integer.toString(healthCheckTimeout));
		prop.setProperty(HEALTH_CHECK_HEALTHY_THRESHOLD, Integer.toString(healthCheckHealthyThreshold));
		prop.setProperty(HEALTH_CHECK_UNHEALTHY_THRESHOLD, Integer.toString(healthCheckUnhealthyThreshold));
		FileUtility.saveProperties(prop, file);
	}
	
	
	
}
