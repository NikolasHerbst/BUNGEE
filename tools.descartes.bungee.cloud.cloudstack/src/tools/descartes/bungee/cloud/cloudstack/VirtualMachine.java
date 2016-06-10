/*******************************************************************************
Copyright 2016 André Bauer, Nikolas Herbst

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

import java.util.Locale;

/**
 * @author root
 *
 */
public class VirtualMachine {

	private String id;
	private String name;
	private int cpunumber;
	private double cpuused;
	private String state;
	private String ipaddress;
	private String hostid;

	public VirtualMachine(String[] properties) {
		
		setId(properties[0]);
		setName(properties[1]);
		setCpunumber(properties[2]);
		setCpuused(properties[3]);
		setState(properties[4]);
		setHostid(properties[5]);
		setIpaddress(properties[6]);

	}

	@Override
	public String toString() {
		return String.format(Locale.US, VMStates.format, getId(), getName(), getIpaddress(), getCpunumber(), getCpuused(), getState());
	
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCpunumber() {
		return cpunumber;
	}

	public void setCpunumber(String cpunumber) {
		this.cpunumber = Integer.parseInt(cpunumber);
	}
	
	public void setCpunumber(int cpunumber) {
		this.cpunumber = cpunumber;
	}

	public double getCpuused() {
		return cpuused;
	}

	public void setCpuused(String cpuused) {
		if(cpuused.equals("")) cpuused = "0.0";
		this.cpuused = Double.parseDouble(cpuused.replace("%", ""));
	}
	
	public void setCpuused(double cpuused) {
		this.cpuused = cpuused;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public String getHostid() {
		return hostid;
	}

	public void setHostid(String hostid) {
		this.hostid = hostid;
	}
	
	

}
