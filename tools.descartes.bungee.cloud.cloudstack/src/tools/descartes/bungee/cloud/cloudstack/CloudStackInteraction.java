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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.w3c.dom.Document;

import net.datapipe.CloudStack.CloudStackAPI;
import tools.descartes.bungee.cloud.CloudInfo;
import tools.descartes.bungee.utils.FileUtility;

public class CloudStackInteraction implements CloudInfo {

	private CloudStackAPI client;
	private String serviceofferingid;
	private String templateid;
	private String zoneid;
	private String group;
	
	/**
	 * Creates a CloudStackInteraction instance
	 * @param group of the observed VMs
	 * @throws Exception
	 */
	public CloudStackInteraction(String group) throws Exception {
		
		Scanner sc = new Scanner(new File(FileUtility.FILE_LOCATION, "propertyFiles/cloudstack.prop"));

		String secret = sc.nextLine().split("=")[1];
		String apikey = sc.nextLine().split("=")[1];
		String apiURL = sc.nextLine().split("=")[1].replace("\\", "");
		this.serviceofferingid = sc.nextLine().split("=")[1];
		this.templateid = sc.nextLine().split("=")[1];
		this.zoneid = sc.nextLine().split("=")[1];
		sc.close();
		this.group = group;
		this.client = new CloudStackAPI(apiURL, secret, apikey);

	}

	
	/**
	 * Deploys a VM
	 * https://cloudstack.apache.org/api/apidocs-4.8/root_admin/
	 * deployVirtualMachine.html
	 * @param name of the new VM
	 * @throws Exception
	 */
	public void deployVM(String name) throws Exception{
		HashMap<String, String> options = new HashMap<>();
		options.put("name", name);
		options.put("group", group);
		client.deployVirtualMachine(serviceofferingid, templateid, zoneid, options);
	}

	/**
	 * Destoys a VM
	 * https://cloudstack.apache.org/api/apidocs-4.8/root_admin/destroyVirtualMachine.html
	 * @param id of the VM
	 * @param expunge (true) or not (false)
	 * @throws Exception
	 */
	public void destroyVM(String id, boolean expunge) throws Exception{
		client.destroyVirtualMachine(id,expunge);
	}
	
	/**
	 * Stops a VM
	 * https://cloudstack.apache.org/api/apidocs-4.8/root_admin/stopVirtualMachine.html
	 * @param id of the VM
	 * @throws Exception
	 */
	public void stopVM(String id) throws Exception{
		HashMap<String, String> options = new HashMap<>();
		client.stopVirtualMachine(id, options);
	}
	
	/**
	 * Starts the VM
	 * https://cloudstack.apache.org/api/apidocs-4.8/root_admin/startVirtualMachine.html
	 * @param id of the VM
	 * @throws Exception 
	 */
	public void startVM(String id) throws Exception{
		client.startVirtualMachine(id);
	}
	
	/**
	 * Starts the VM
	 * https://cloudstack.apache.org/api/apidocs-4.8/root_admin/startVirtualMachine.html
	 * @param id of the VM
	 * @param hostid of the host on which the VM should start
	 * @throws Exception
	 */
	public void startVM(String id, String hostid) throws Exception{
		client.startVirtualMachine(id, hostid);
	}
	
	/**
	 * Starts all VMs
	 * @param vms ArrayList of VMs that should start
	 * @throws Exception
	 */
	public void startAllVMs(ArrayList<VirtualMachine> vms) throws Exception{
		for(VirtualMachine vm: vms){
			startVM(vm.getId());
		}
	}
	
	/**
	 * Stops all VMs
	 * @param vms A ArrayList of VMs that should stop
	 * @throws Exception
	 */
	public void stopAllVMs(ArrayList<VirtualMachine> vms) throws Exception{
		for(VirtualMachine vm: vms){
			stopVM(vm.getId());
		}
	}
	
	/**
	 * https://cloudstack.apache.org/api/apidocs-4.8/root_admin/listVirtualMachines.html
	 * @param tag of the VMs
	 * @return ArrayList of VMs
	 * @throws Exception
	 */
	public ArrayList<VirtualMachine> readVmStatus(String tag) throws Exception {
		HashMap<String, String> options = new HashMap<>();
		if(!tag.equals("")){
			options.put("name", tag);
		}
		options.put("groupid", group);
		options.put("state", "Running");
		Document doc = client.listVirtualMachines(options);
		

		ArrayList<VirtualMachine> vms = VMStates.read(doc);

		return vms;
	}

	@Override
	public int getNumberOfResources(String tag) {
		try {
			return readVmStatus(tag).size();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public int getNumberOfResources(){
		try {
			return readVmStatus("").size();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	
	
	public static void main(String[] args) throws Exception {
		CloudStackInteraction management = new CloudStackInteraction("1");
		//management.deployVM("Test123");
		//management.destroyVM("29f2d2db-0c8d-44ed-8fe9-f6fac31307cb", true);
	    //management.stopVM("5e6e2f1d-c3b2-45b9-9134-7c474c076e27");
		//management.startVM("5e6e2f1d-c3b2-45b9-9134-7c474c076e27");
		System.out.println(management.getNumberOfResources("Bungee"));
		
	}

}
