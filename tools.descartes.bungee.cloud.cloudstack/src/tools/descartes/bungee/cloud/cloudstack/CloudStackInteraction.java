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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import org.w3c.dom.Document;

import net.datapipe.CloudStack.CloudStackAPI;
import tools.descartes.bungee.cloud.CloudInfo;
import tools.descartes.bungee.config.Host;
import tools.descartes.bungee.utils.FileUtility;

public class CloudStackInteraction implements CloudInfo {

	private static final String RUNNING = "Running";
	private static final String STOPPED = "Stopped";
	private CloudStackAPI client;
	private String serviceofferingid;
	private String templateid;
	private String zoneid;
	private String group;

	/**
	 * Creates a CloudStackInteraction instance
	 * 
	 * @param group
	 *            of the observed VMs
	 * @throws FileNotFoundException 
	 * @throws Exception
	 */
	public CloudStackInteraction(String group){

		Properties cloudProperties = FileUtility.loadProperties(new File(FileUtility.FILE_LOCATION, "propertyFiles/cloudstack.prop"));
				
		
		String secret = cloudProperties.getProperty("secret");
		String apikey = cloudProperties.getProperty("apiKey");
		String apiURL = cloudProperties.getProperty("apiURL");
		this.serviceofferingid = cloudProperties.getProperty("serviceofferingID");
		this.templateid = cloudProperties.getProperty("templateID");
		this.zoneid = cloudProperties.getProperty("zoneID");
		this.group = group;
		this.client = new CloudStackAPI(apiURL, secret, apikey);

	}

	/**
	 * Deploys a VM https://cloudstack.apache.org/api/apidocs-4.8/root_admin/
	 * deployVirtualMachine.html
	 * 
	 * @param name
	 *            of the new VM
	 * @throws Exception
	 */
	public void deployVM(String name) throws Exception {
		HashMap<String, String> options = new HashMap<>();
		options.put("name", name);
		options.put("group", group);
		client.deployVirtualMachine(serviceofferingid, templateid, zoneid, options);
	}

	/**
	 * Destoys a VM https://cloudstack.apache.org/api/apidocs-4.8/root_admin/
	 * destroyVirtualMachine.html
	 * 
	 * @param id
	 *            of the VM
	 * @param expunge
	 *            (true) or not (false)
	 * @throws Exception
	 */
	public void destroyVM(String id, boolean expunge) throws Exception {
		client.destroyVirtualMachine(id, expunge);
	}

	/**
	 * Stops a VM https://cloudstack.apache.org/api/apidocs-4.8/root_admin/
	 * stopVirtualMachine.html
	 * 
	 * @param id
	 *            of the VM
	 * @throws Exception
	 */
	public void stopVM(String id) throws Exception {
		HashMap<String, String> options = new HashMap<>();
		client.stopVirtualMachine(id, options);
	}

	/**
	 * Starts the VM https://cloudstack.apache.org/api/apidocs-4.8/root_admin/
	 * startVirtualMachine.html
	 * 
	 * @param id
	 *            of the VM
	 * @throws Exception
	 */
	public void startVM(String id) throws Exception {
		client.startVirtualMachine(id);
	}

	/**
	 * Starts the VM https://cloudstack.apache.org/api/apidocs-4.8/root_admin/
	 * startVirtualMachine.html
	 * 
	 * @param id
	 *            of the VM
	 * @param hostid
	 *            of the host on which the VM should start
	 * @throws Exception
	 */
	public void startVM(String id, String hostid) throws Exception {
		client.startVirtualMachine(id, hostid);
	}

	/**
	 * Starts all VMs
	 * 
	 * @param vms
	 *            ArrayList of VMs that should start
	 * @throws Exception
	 */
	public void startAllVMs(ArrayList<VirtualMachine> vms) throws Exception {
		for (VirtualMachine vm : vms) {
			startVM(vm.getId());
		}
	}

	/**
	 * Stops all VMs
	 * 
	 * @param vms
	 *            A ArrayList of VMs that should stop
	 * @throws Exception
	 */
	public void stopAllVMs(ArrayList<VirtualMachine> vms) throws Exception {
		for (VirtualMachine vm : vms) {
			stopVM(vm.getId());
		}
	}

	/**
	 * https://cloudstack.apache.org/api/apidocs-4.8/root_admin/
	 * listVirtualMachines.html
	 * 
	 * @param tag of the VMs
	 * @return ArrayList of VMs
	 * @throws Exception
	 */
	public ArrayList<VirtualMachine> getAllVms(String tag) throws Exception {
		return getAllVms(tag, "");
	}
	
	/**
	 https://cloudstack.apache.org/api/apidocs-4.8/root_admin/listVirtualMachines.html
	 * @param tag of the VMs
	 * @param state of the VMs
	 * @return List of VMs
	 * @throws Exception
	 */
	public ArrayList<VirtualMachine> getAllVms(String tag, String state) throws Exception {
		HashMap<String, String> options = new HashMap<>();
		if (!tag.equals("")) {
			options.put("name", tag);
		}
		if(!state.equals("")){
			options.put("state", state);
		}
		options.put("groupid", group);
		Document doc = client.listVirtualMachines(options);

		ArrayList<VirtualMachine> vms = VMStates.read(doc);

		return vms;
	}

	@Override
	public int getNumberOfResources(String tag) {
		try {
			return getAllVms(tag, RUNNING).size();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int getNumberOfResources() {
		try {
			return getAllVms("", RUNNING).size();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Starts cloudSize-amount of Instances with the specified tag
	 * 
	 * @param cloudSize
	 *            amount of Instances
	 * @param tag
	 *            for filtering
	 * @throws Exception
	 */
	public void startInstances(int cloudSize, String tag) throws Exception {
		int currentSize = getNumberOfResources(tag);
		System.out.println("running instances: " + currentSize);
		int requiredSize = cloudSize - currentSize;
		ArrayList<VirtualMachine> vms = getAllVms(tag, STOPPED);
		System.out.println("stopped instances: " + vms.size());
		System.out.println("required instances: "+requiredSize);
		if (vms.size() > requiredSize) {
			for (int i = 0; i < requiredSize; i++) {
				startVM(vms.get(i).getId());
			}
		} else {
			throw new IllegalArgumentException("Too less Instances");
		}

	}
	
	/**
	 * Stops amount of Instances with the specified tag
	 * 
	 * @param amount of Instances
	 * @param tag
	 *            for filtering
	 * @throws Exception
	 */
	public void stopInstances(int amount, String tag) throws Exception {
		int currentSize = getNumberOfResources(tag);
		System.out.println("running instances: " + currentSize);
		ArrayList<VirtualMachine> vms = getAllVms(tag, RUNNING);
		if (vms.size() > amount) {
			for (int i = 0; i < amount; i++) {
				stopVM(vms.get(i).getId());
			}
		} else {
			throw new IllegalArgumentException("Too less Instances");
		}

	}
	

	public static void main(String[] args) throws Exception {
		CloudStackInteraction management;

		management = new CloudStackInteraction("1");
		System.out.println("running instances: "+management.getNumberOfResources("Bungee"));

		// management.deployVM("Test123");
		// management.destroyVM("29f2d2db-0c8d-44ed-8fe9-f6fac31307cb", true);
		// management.stopVM("5e6e2f1d-c3b2-45b9-9134-7c474c076e27");
		// management.startVM("5e6e2f1d-c3b2-45b9-9134-7c474c076e27");

		//management.startInstances(4, "Bungee");
		//management.stopInstances(3, "Bungee");
		// System.out.println("finish");

	}

}
