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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.datapipe.CloudStack.CloudStackAPI;
import tools.descartes.bungee.allocation.ResourceAllocation;
import tools.descartes.bungee.allocation.SupplySeries;
import tools.descartes.bungee.cloud.Bounds;
import tools.descartes.bungee.cloud.CloudInfo;
import tools.descartes.bungee.cloud.cloudstack.CloudstackControllerImpl.CSAutoScaleVMGroup;
import tools.descartes.bungee.cloud.cloudstack.CloudstackControllerImpl.CSLB;
import tools.descartes.bungee.utils.DateUtility;
import tools.descartes.bungee.utils.FileUtility;

public class CloudStackInteraction implements CloudInfo {

	private static SimpleDateFormat cloudstackFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat cloudstackOutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	private static final String RUNNING = "Running";
	private static final String STOPPED = "Stopped";
	private CloudStackAPI client;
	private String serviceofferingid;
	private String templateid;
	private String zoneid;
	private String group;
	private String username;
	private CloudstackControllerImpl cci;

	/**
	 * Creates a CloudStackInteraction instance
	 * 
	 * @param file
	 *            of the observed VMs
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public CloudStackInteraction(File file) {

		Properties cloudProperties = FileUtility.loadProperties(file);

		String secret = cloudProperties.getProperty("secret");
		String apikey = cloudProperties.getProperty("apiKey");
		String apiURL = cloudProperties.getProperty("apiURL");
		this.serviceofferingid = cloudProperties.getProperty("serviceofferingID");
		this.templateid = cloudProperties.getProperty("templateID");
		this.zoneid = cloudProperties.getProperty("zoneID");
		this.group = cloudProperties.getProperty("group");
		this.username = cloudProperties.getProperty("username");
		this.client = new CloudStackAPI(apiURL, secret, apikey);
		this.cci = new CloudstackControllerImpl(file);

	}

	private enum EventState {
		SCHEDULED("Scheduled"), COMPLETED("Completed");

		private EventState(final String text) {
			this.text = text;
		}

		private final String text;

		@Override
		public String toString() {
			return text;
		}
	}

	private interface EventType {
		public String increase();

		public String decrease();
	}

	static class VMCreateDestroy implements EventType {
		public static VMCreateDestroy instance() {
			return new VMCreateDestroy();
		}

		@Override
		public String increase() {
			return "VM.START";
		}

		@Override
		public String decrease() {
			return "VM.STOP";
		}
	}

	static class LBAddRemoveVM implements EventType {
		public static LBAddRemoveVM instance() {
			return new LBAddRemoveVM();
		}

		@Override
		public String increase() {
			return "LB.ASSIGN.TO.RULE";
		}

		@Override
		public String decrease() {
			return "LB.REMOVE.FROM.RULE";
		}
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
	 * @param tag
	 *            of the VMs
	 * @return ArrayList of VMs
	 * @throws Exception
	 */
	public ArrayList<VirtualMachine> getAllVms(String tag) throws Exception {
		return getAllVms(tag, "");
	}

	/**
	 * https://cloudstack.apache.org/api/apidocs-4.8/root_admin/
	 * listVirtualMachines.html
	 * 
	 * @param tag
	 *            of the VMs
	 * @param state
	 *            of the VMs
	 * @return List of VMs
	 * @throws Exception
	 */
	public ArrayList<VirtualMachine> getAllVms(String tag, String state) throws Exception {
		HashMap<String, String> options = new HashMap<>();
		if (!tag.equals("")) {
			options.put("name", tag);
		}
		if (!state.equals("")) {
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
		System.out.println("required instances: " + requiredSize);
		if(requiredSize < 0){
			stopInstances(-requiredSize, tag);
		}
		if (vms.size() >= requiredSize) {
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
	 * @param amount
	 *            of Instances
	 * @param tag
	 *            for filtering
	 * @throws Exception
	 */
	public void stopInstances(int amount, String tag) throws Exception {
		int currentSize = getNumberOfResources(tag);
		//System.out.println("running instances: " + currentSize);
		ArrayList<VirtualMachine> vms = getAllVms(tag, RUNNING);
		if (vms.size() > amount) {
			for (int i = 0; i < amount; i++) {
				stopVM(vms.get(i).getId());
			}
		} else {
			throw new IllegalArgumentException("Too less Instances");
		}

	}

	public List<SupplySeries> getResourceAllocations(Date startDate, Date endDate, String ip) {
		List<SupplySeries> supply = new LinkedList<SupplySeries>();
		HashMap<String, String> options = new HashMap<>();
		options.put("startdate", cloudstackFormat.format(startDate.getTime()));
		options.put("enddate", cloudstackFormat.format(endDate.getTime()));
		Document events;
		try {
			events = client.listEvents(options);
			List<ResourceAllocation> scheduledVMs = getResourceAllocationFromReply(events, startDate, endDate,
					VMCreateDestroy.instance(), EventState.SCHEDULED);
			List<ResourceAllocation> completedVMs = getResourceAllocationFromReply(events, startDate, endDate,
					VMCreateDestroy.instance(), EventState.COMPLETED);
			List<ResourceAllocation> completedLBVMs = getResourceAllocationFromReply(events, startDate, endDate,
					LBAddRemoveVM.instance(), EventState.COMPLETED);

			supply.add(new SupplySeries(scheduledVMs, SupplySeries.TYPE.VM_SCHEDULED));
			supply.add(new SupplySeries(completedVMs, SupplySeries.TYPE.VM_COMPLETED));
			supply.add(new SupplySeries(completedLBVMs, SupplySeries.TYPE.LB_RULE_ADAPTION));
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < supply.size(); i++) {
			List<ResourceAllocation> allo = supply.get(i).getAllocations();
			System.out.println(supply.get(i).getName());
			for (int k = 0; k < allo.size(); k++) {
				System.out.println(allo.get(k));
			}
			System.out.println("-------------------------");
		}

		return supply;
	}

	private  List<ResourceAllocation> getResourceAllocationFromReply(Document reply, Date start, Date end,
			EventType type, EventState status) {
		List<ResourceAllocation> allocations = new LinkedList<ResourceAllocation>();

		/*
		 * Cloudstack returns second precision only -> cut precision of start
		 * and end date to second to simplify date comparisons
		 */
		start = DateUtility.toSecondPrecision(start);
		end = DateUtility.toSecondPrecision(end);

		int currentAmount = 0;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile("//event");
			NodeList item_list = (NodeList) expr.evaluate(reply, XPathConstants.NODESET);
			for (int i = item_list.getLength() - 1; i >= 0; i--) {
				Node item = item_list.item(i);
				String dateString = getPropertyValue(xpath, item, "created");
				Date date = cloudstackOutputFormat.parse(dateString);
				if (DateUtility.beforeOrEqual(start, date) && DateUtility.afterOrEqual(end, date)) {
					String user = getPropertyValue(xpath, item, "username");
					if (user.equals(username)) {
						String eventState = getPropertyValue(xpath, item, "state");
						if (eventState.equals(status.toString())) {
							String eventType = getPropertyValue(xpath, item, "type");
							if (eventType.equals(type.increase()) || eventType.equals(type.decrease())) {
								if (eventType.equals(type.increase())) {
									currentAmount++;
								} else if (eventType.equals(type.decrease())) {
									currentAmount--;
								}
								allocations.add(new ResourceAllocation(date, currentAmount));
							}
						}
					}
				}
			}
		} catch (XPathExpressionException | ParseException e) {
			e.printStackTrace();
		}
		return allocations;
	}

	private static String getPropertyValue(XPath xpath, Node item, String property_name)
			throws XPathExpressionException {
		XPathExpression xp_property = xpath.compile(property_name + "/text()");
		String property_value = (String) xp_property.evaluate(item, XPathConstants.STRING);
		return property_value;
	}

	

	public Bounds getScalingBounds(String hostName) {
		return cci.getScalingBounds(hostName);
	}

	public void disableAutoScaleGroup(String hostName) {
		cci.disableAutoScaleGroup(hostName);
	}

	public boolean updateAutoScaleVMGroup(String hostName, int min, int max) {
		return cci.updateAutoScaleVMGroup(hostName, min, max);
	}

	public void enableAutoScaleGroup(String hostName) {
		cci.enableAutoScaleGroup(hostName);
	}

	
	public static void main(String[] args) throws Exception {
		CloudStackInteraction management;

		management = new CloudStackInteraction(new File(FileUtility.FILE_LOCATION, "propertyFiles/cloudstack.prop"));
		
		//management.startInstances(5, "bungee");
		
		 System.out.println("running instances:"+management.getNumberOfResources("Bungee"));

		// management.deployVM("Test123");
		// management.destroyVM("29f2d2db-0c8d-44ed-8fe9-f6fac31307cb", true);
		// management.stopVM("5e6e2f1d-c3b2-45b9-9134-7c474c076e27");
		// management.startVM("5e6e2f1d-c3b2-45b9-9134-7c474c076e27");

		// management.startInstances(4, "Bungee");
		//management.stopInstances(1, "Bungee");
		// System.out.println("finish");
		/*DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		Date startDate = null;
		Date endDate = null;
		try {
			startDate = format.parse("31.05.2016 00:00");
			endDate = format.parse("31.05.2016 19:30");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		management.getResourceAllocations(startDate, endDate, "");*/

	}
}
