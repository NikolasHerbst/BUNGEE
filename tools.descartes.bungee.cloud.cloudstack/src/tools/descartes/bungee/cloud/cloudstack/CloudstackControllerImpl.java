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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import net.datapipe.CloudStack.CloudStackAPI;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tools.descartes.bungee.allocation.AllocationSeries;
import tools.descartes.bungee.allocation.ResourceAllocation;
import tools.descartes.bungee.allocation.SupplySeries;
import tools.descartes.bungee.cloud.Bounds;
import tools.descartes.bungee.utils.DateUtility;
import tools.descartes.bungee.utils.FileUtility;

public class CloudstackControllerImpl implements ResultListener {

	private CloudStackAPI client;
	private static SimpleDateFormat cloudstackFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat cloudstackOutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private JobResult result = null;
	private CloudStackInteraction csi;

	public enum ScaleDirection {
		UP("up"), DOWN("down");

		private ScaleDirection(final String text) {
			this.text = text;
		}

		private final String text;

		@Override
		public String toString() {
			return text;
		}
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

	private class CSIp {
		public String id;
		public String zoneId;
	}

	public class CSLB {
		public String id;
		public String publicIpId;
		public String zoneId;
	}

	private class CSServiceOffering {
		public String id;

	}

	private class CSTemplate {
		public String id;
	}

	public class CSCondtion {
		public String id;
		public String relationalOperator;
		public int threshold;
		public String counterId;
	}

	public class CSPolicy {
		public String id;
		public String conditionIds;
		public int duration;
		public int quiettime;
	}

	public class CSAutoScaleVMGroup {
		public String id;
		public int interval;
		public int maxMembers;
		public int minMembers;
		public String vmProfileId;
		public String lbRuleId;
		public String scaleUpPolicies;
		public String scaleDownPolicies;
	}

	public CloudstackControllerImpl(String apiURL, String secret, String apikey) {
		super();
		this.client = new CloudStackAPI(apiURL, secret, apikey);
		;
	}

	public CloudstackControllerImpl(Properties properties) {
		super();
		this.client = new CloudStackAPI(properties.getProperty("apiURL"), properties.getProperty("secret"),
				properties.getProperty("apiKey"));
	}

	public CloudstackControllerImpl(File propertiesFile) {
		this(FileUtility.loadProperties(propertiesFile));
		csi = new CloudStackInteraction(propertiesFile);
	}

	public List<String> getResourceIds(String ip) {
		List<String> ids = new LinkedList<String>();
		CSLB csLB = getLBForIP(ip);
		HashMap<String, String> options = new HashMap<>();
		options.put("state", "running");
		try {
			Document events = client.listLoadBalancerRuleInstances(csLB.id, options);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression count_xp = xpath.compile("//loadbalancerruleinstance");
			NodeList item_list = (NodeList) count_xp.evaluate(events, XPathConstants.NODESET);
			for (int i = 0; i < item_list.getLength(); i++) {
				Node item = item_list.item(i);
				ids.add(getPropertyValue(xpath, item, "id"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ids;
	}

	public int getNumberOfResources(String tag) {
		try {
			return csi.getNumberOfResources(tag);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
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
			for(int k = 0; k < allo.size(); k++){
				System.out.println(allo.get(k));
			}
			System.out.println("-------------------------");
		}

		return supply;
	}

	public String getLBId(String ip) {
		String id = null;
		CSLB lb = getLBForIP(ip);
		if (lb != null) {
			id = lb.id;
		}
		return id;
	}

	private CSLB getLBForIP(String ip) {
		CSIp csIP = getIPbyIP(ip);
		return getLBForIPId(csIP.id);
	}

	public String getAutoScaleGroupId(String ip) {
		String id = null;
		CSAutoScaleVMGroup group = getAutoScaleGroupForIp(ip);
		if (group != null) {
			id = group.id;
		}
		return id;
	}

	public Bounds getScalingBounds(String ip) {
		Bounds bounds = null;
		CSAutoScaleVMGroup group = getAutoScaleGroupForIp(ip);
		if (group != null) {
			bounds = new Bounds(group.minMembers, group.maxMembers);
		}
		return bounds;
	}

	private CSAutoScaleVMGroup getAutoScaleGroupForIp(String ip) {
		CSLB csLB = getLBForIP(ip);
		return getAutoScaleGroupForLBId(csLB.id);
	}

	public boolean disableAutoScaleGroup(String ip) {
		CSAutoScaleVMGroup group = getAutoScaleGroupForIp(ip);

		disableAutoScaleGroupForId(group.id, this);
		return waitForAsyncResult();
	}

	public boolean enableAutoScaleGroup(String ip) {
		CSAutoScaleVMGroup group = getAutoScaleGroupForIp(ip);
		enableAutoScaleGroupForId(group.id, this);
		return waitForAsyncResult();
	}

	public CSLB createLoadBalancerRule(String name, String ip, String algorithm, int privatePort, int publicPort) {
		String lbId = createLoadBalancerRule(name, ip, algorithm, privatePort, publicPort, this);
		waitForAsyncResult();
		return getLBbyId(lbId);
	}

	public boolean deleteLoadBalancerRuleByIp(String ip) {
		CSLB lb = getLBForIP(ip);
		return deleteLoadBalancerRule(lb.id, true);
	}

	private boolean deleteLoadBalancerRule(String id, boolean deleteHealthChecks) {
		if (deleteHealthChecks) {
			String healthCheckId = getLBHealthCheckPolicyByLBId(id);
			if (healthCheckId != "") {
				System.out.println(
						"Deleted healthCheck: " + healthCheckId + ": " + deleteLBHealthCheckPolicy(healthCheckId));
			}
		}
		deleteLoadBalancerRule(id, this);
		return waitForAsyncResult();
	}

	public String createAutoScaleVMGroup(CloudSettings cs) {

		CSLB csLB = createLoadBalancerRule(cs.getName(), cs.getIp(), cs.getAlgorithm().toString().toLowerCase(),
				cs.getPrivatePort(), cs.getPublicPort());
		createLBHealthCheckPolicy(csLB.id, cs.getHealthCheckPingPath(), cs.getHealthCheckInterval(),
				cs.getHealthCheckTimeout(), cs.getHealthCheckHealthyThreshold(), cs.getHealthCheckUnhealthyThreshold());
		String vmProfileId = createAutoScaleVMProfile(cs.getOffering(), cs.getTemplate(), cs.getIp());

		Policiy scaleUp = cs.getScaleUp();
		Policiy scaleDown = cs.getScaleDown();

		String scaleUpConditionId = createConditionUseCounterKeyword(scaleUp.getCounter(), scaleUp.getOperator(),
				scaleUp.getThreshold()).id;
		String scaleDownConditionId = createConditionUseCounterKeyword(scaleDown.getCounter(), scaleDown.getOperator(),
				scaleDown.getThreshold()).id;

		CSPolicy policyUp = createAutoScalePolicy(ScaleDirection.UP, scaleUpConditionId, scaleUp.getDuration(),
				scaleUp.getQuiettime());
		CSPolicy policyDown = createAutoScalePolicy(ScaleDirection.DOWN, scaleDownConditionId, scaleDown.getDuration(),
				scaleDown.getQuiettime());
		return createAutoScaleVMGroup(csLB.id, cs.getMinInstances(), cs.getMaxInstances(), policyUp.id, policyDown.id,
				vmProfileId, cs.getEvalInterval()).id;
	}

	public boolean updateAutoScaleVMGroup(String ip, int minMembers, int maxMembers, int interval) {
		CSAutoScaleVMGroup autoScaleGroupForIp = getAutoScaleGroupForIp(ip);
		return updateAutoScaleVMGroup(autoScaleGroupForIp.id, minMembers, maxMembers,
				autoScaleGroupForIp.scaleUpPolicies, autoScaleGroupForIp.scaleDownPolicies, interval);
	}

	public boolean updateAutoScaleVMGroup(String ip, int minMembers, int maxMembers) {
		CSAutoScaleVMGroup autoScaleGroupForIp = getAutoScaleGroupForIp(ip);
		return updateAutoScaleVMGroup(autoScaleGroupForIp.id, minMembers, maxMembers,
				autoScaleGroupForIp.scaleUpPolicies, autoScaleGroupForIp.scaleDownPolicies,
				autoScaleGroupForIp.interval);
	}

	public boolean deleteAutoScaleVMGroupByIp(String ip) {
		CSAutoScaleVMGroup group = getAutoScaleGroupForIp(ip);
		boolean success = deleteAutoScaleVMGroup(group.id, true);
		// TODO: not necessary, if deleteAutoScaleVMGroup deletes lb already.
		deleteLoadBalancerRuleByIp(ip);
		return success;
	}

	private boolean deleteAutoScaleVMGroup(String id, boolean recursive) {
		CSAutoScaleVMGroup group = getAutoScaleGroupById(id);
		deleteAutoScaleVMGroup(id, this);
		boolean success = waitForAsyncResult();
		if (recursive) {
			System.out.println(
					"Deleted profile: " + group.vmProfileId + ": " + deleteAutoScaleVmProfile(group.vmProfileId));
			System.out.println("Deleted policyUp: " + group.scaleUpPolicies + ": "
					+ deleteAutoScalePolicy(group.scaleUpPolicies, true));
			System.out.println("Deleted policyDown: " + group.scaleDownPolicies + ": "
					+ deleteAutoScalePolicy(group.scaleDownPolicies, true));
			// TODO: lbRuleId is null... don't know why, Cloudstack bug?
			// System.out.println("Deleted lb: " + group.lbRuleId + ": " +
			// deleteLoadBalancerRule(group.lbRuleId));
		}
		return success;
	}

	private String createLBHealthCheckPolicy(String lbruleid, String pingpath, int intervaltime, int timeout,
			int healthythreshold, int unhealthythreshold) {
		String id = createLBHealthCheckPolicy(lbruleid, pingpath, intervaltime, timeout, healthythreshold,
				unhealthythreshold, this);
		waitForAsyncResult();
		return id;
	}

	private CSPolicy createAutoScalePolicy(ScaleDirection direction, String conditionIds, int duration, int quiettime) {
		String id = createAutoScalePolicy(direction, conditionIds, duration, quiettime, this);
		waitForAsyncResult();
		return getPolicyById(id);
	}

	public boolean deleteAutoScalePolicy(String id, boolean recursive) {
		if (recursive) {
			CSPolicy policy = getPolicyById(id);
			deleteCondition(policy.conditionIds);
		}
		deleteAutoScalePolicy(id, this);
		return waitForAsyncResult();
	}

	public boolean deleteLBHealthCheckPolicy(String id) {
		deleteLBHealthCheckPolicy(id, this);
		return waitForAsyncResult();
	}

	private CSAutoScaleVMGroup createAutoScaleVMGroup(String lbRuleId, int minMembers, int maxMembers,
			String scaleUpPolicyIds, String scaleDownPolicyIds, String vmProfileId, int interval) {
		String id = createAutoScaleVMGroup(lbRuleId, minMembers, maxMembers, scaleUpPolicyIds, scaleDownPolicyIds,
				vmProfileId, interval, this);
		waitForAsyncResult();
		return getAutoScaleGroupById(id);
	}

	private boolean updateAutoScaleVMGroup(String vmGroupId, int minMembers, int maxMembers, String scaleUpPolicyIds,
			String scaleDownPolicyIds, int interval) {
		updateAutoScaleVMGroup(vmGroupId, minMembers, maxMembers, scaleUpPolicyIds, scaleDownPolicyIds, interval, this);
		return waitForAsyncResult();
	}

	public CSCondtion createConditionUseCounterKeyword(String counterKeyword, String relationalOperator,
			int threshold) {
		String conditionId = createConditionUseCounterKeyword(counterKeyword, relationalOperator, threshold, this);
		waitForAsyncResult();
		return getConditionById(conditionId);
	}

	public CSCondtion createConditionUseCounterId(String counterId, String relationalOperator, int threshold) {
		String conditionId = createConditionUseCounterId(counterId, relationalOperator, threshold, this);
		waitForAsyncResult();
		return getConditionById(conditionId);
	}

	public boolean deleteCondition(String id) {
		deleteCondition(id, this);
		return waitForAsyncResult();
	}

	public String createAutoScaleVMProfile(String serviceOffering, String templateName, String ip) {
		String vmProfileId = createAutoScaleVMProfile(serviceOffering, templateName, ip, this);
		waitForAsyncResult();
		return vmProfileId;
	}

	public boolean deleteAutoScaleVmProfile(String id) {
		deleteAutoScaleVmProfile(id, this);
		return waitForAsyncResult();
	}

	public String getScalePolicy(String ip, ScaleDirection direction) {
		CSAutoScaleVMGroup group = getAutoScaleGroupForIp(ip);
		return getScalePolicyForGroupId(group.id, direction);
	}

	public String setScaleUpPolicyDuration(String ip, ScaleDirection direction, int durationInSeconds,
			ResultListener listener) {
		String policyId = getScalePolicy(ip, direction);
		return setPolicyDurationForId(policyId, durationInSeconds, listener);
	}

	public void setPolicyThreshold(String ip, ScaleDirection direction, int threshold, ResultListener listener) {
		String policyId = getScalePolicy(ip, direction);
		setConditionThresholdForId(policyId, threshold, listener);
	}

	public boolean setPolicyThreshold(String ip, ScaleDirection direction, int threshold) {
		String policyId = getScalePolicy(ip, direction);
		setConditionThresholdForId(policyId, threshold, this);
		return waitForAsyncResult();
	}

	public boolean setScalePolicyDuration(String ip, ScaleDirection direction, int durationInSeconds) {
		setScaleUpPolicyDuration(ip, direction, durationInSeconds, this);
		return waitForAsyncResult();
	}

	private boolean waitForAsyncResult() {
		while (result == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		boolean successful = result.isSuccessful();
		result = null;
		return successful;
	}

	@Override
	public void jobFinished(JobResult result) {
		this.result = result;
	}

	private String createConditionUseCounterKeyword(String counterKeyword, String relationalOperator, int threshold,
			ResultListener listener) {
		HashMap<String, String> options = new HashMap<>();
		options.put("keyword", counterKeyword);
		String counterId = getCounterByOptions(options);
		return createConditionUseCounterId(counterId, relationalOperator, threshold, listener);
	}

	private String createConditionUseCounterId(String counterId, String relationalOperator, int threshold,
			ResultListener listener) {
		String resourceId = "";
		HashMap<String, String> options = new HashMap<>();
		try {
			Document reply = client.createCondition(counterId, relationalOperator, threshold, options);
			registerListenerAsyncJobResult(listener, reply);
			resourceId = getResourceId(reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resourceId;
	}

	private void deleteCondition(String id, ResultListener listener) {
		try {
			Document reply = client.deleteCondition(id);
			registerListenerAsyncJobResult(listener, reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteLoadBalancerRule(String id, ResultListener listener) {
		try {
			Document reply = client.deleteLoadBalancerRule(id);
			registerListenerAsyncJobResult(listener, reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteLBHealthCheckPolicy(String id, ResultListener listener) {
		try {
			Document reply = client.deleteLBHealthCheckPolicy(id);
			registerListenerAsyncJobResult(listener, reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String createAutoScaleVMGroup(String lbRuleId, int minMembers, int maxMembers, String scaleUpPolicyIds,
			String scaleDownPolicyIds, String vmProfileId, int interval, ResultListener listener) {
		String resourceId = "";
		HashMap<String, String> options = new HashMap<>();
		options.put("interval", Integer.toString(interval));
		try {
			Document reply = client.createAutoScaleVmGroup(lbRuleId, minMembers, maxMembers, scaleDownPolicyIds,
					scaleUpPolicyIds, vmProfileId, options);
			registerListenerAsyncJobResult(listener, reply);
			resourceId = getResourceId(reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resourceId;
	}

	private String updateAutoScaleVMGroup(String vmGroupId, int minMembers, int maxMembers, String scaleUpPolicyIds,
			String scaleDownPolicyIds, int interval, ResultListener listener) {
		String resourceId = "";
		HashMap<String, String> options = new HashMap<>();
		System.out.println("interval: " + interval);
		options.put("interval", Integer.toString(interval));
		options.put("minmembers", Integer.toString(minMembers));
		options.put("maxmembers", Integer.toString(maxMembers));
		options.put("scaledownpolicyids", scaleDownPolicyIds);
		options.put("scaleuppolicyids", scaleUpPolicyIds);
		try {
			Document reply = client.updateAutoScaleVMGroup(vmGroupId, options);
			registerListenerAsyncJobResult(listener, reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resourceId;
	}

	private String createAutoScaleVMProfile(String serviceOffering, String templateName, String ip,
			ResultListener listener) {
		CSIp csIP = getIPbyIP(ip);
		String resourceId = "";
		HashMap<String, String> options = new HashMap<>();
		options.put("name", serviceOffering);
		CSServiceOffering offering = getServiceOfferingbyOptions(options);
		options.clear();
		options.put("name", templateName);
		CSTemplate template = getTemplateByOptions(options);
		options.clear();
		options.put("counterparam[0].name", "snmpcommunity");
		options.put("counterparam[0].value", "public");
		options.put("counterparam[1].name", "snmpport");
		options.put("counterparam[1].value", "161");
		try {
			Document reply = client.createAutoScaleVmProfile(offering.id, template.id, csIP.zoneId, options);
			registerListenerAsyncJobResult(listener, reply);
			resourceId = getResourceId(reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resourceId;
	}

	private void deleteAutoScaleVmProfile(String id, ResultListener listener) {
		try {
			Document reply = client.deleteAutoScaleVmProfile(id);
			registerListenerAsyncJobResult(listener, reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String createAutoScalePolicy(ScaleDirection direction, String conditionIds, int duration, int quiettime,
			ResultListener listener) {
		String resourceId = "";
		HashMap<String, String> options = new HashMap<>();
		options.put("quiettime", Integer.toString(quiettime));
		try {
			Document reply = client.createAutoScalePolicy("scale" + direction, conditionIds, duration, options);
			registerListenerAsyncJobResult(listener, reply);
			resourceId = getResourceId(reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resourceId;
	}

	private String createLBHealthCheckPolicy(String lbruleid, String pingpath, int intervaltime, int timeout,
			int healthythreshold, int unhealthythreshold, ResultListener listener) {
		String resourceId = "";
		HashMap<String, String> options = new HashMap<>();
		options.put("pingpath", pingpath);
		options.put("intervaltime", Integer.toString(intervaltime));
		options.put("responsetimeout", Integer.toString(timeout));
		options.put("healthythreshold", Integer.toString(healthythreshold));
		options.put("unhealthythreshold", Integer.toString(unhealthythreshold));
		try {
			Document reply = client.createLBHealthCheckPolicy(lbruleid, options);
			registerListenerAsyncJobResult(listener, reply);
			resourceId = getResourceId(reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resourceId;
	}

	private void deleteAutoScalePolicy(String id, ResultListener listener) {
		try {
			Document reply = client.deleteAutoScalePolicy(id);
			registerListenerAsyncJobResult(listener, reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private CSPolicy getPolicyById(String id) {
		HashMap<String, String> options = new HashMap<>();
		options.put("id", id);
		return getPolicyByOptions(options);
	}

	private CSPolicy getPolicyByOptions(HashMap<String, String> options) {
		CSPolicy csPolicy = new CSPolicy();
		try {
			Document reply = client.listAutoScalePolicies(options);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//autoscalepolicy");

			NodeList item_list = (NodeList) expr.evaluate(reply, XPathConstants.NODESET);
			if (item_list.getLength() == 1) {
				Node item = item_list.item(0);
				csPolicy.id = getPropertyValue(xpath, item, "id");
				csPolicy.conditionIds = getPropertyValue(xpath, item, "conditions/id");
				csPolicy.duration = Integer.parseInt(getPropertyValue(xpath, item, "duration"));
				csPolicy.quiettime = Integer.parseInt(getPropertyValue(xpath, item, "quiettime"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return csPolicy;
	}

	private void deleteAutoScaleVMGroup(String id, ResultListener listener) {
		try {
			Document reply = client.deleteAutoScaleVmGroup(id);
			registerListenerAsyncJobResult(listener, reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String createLoadBalancerRule(String name, String ip, String algorithm, int privatePort, int publicPort,
			ResultListener listener) {
		CSIp csIP = getIPbyIP(ip);
		String resourceId = "";
		HashMap<String, String> options = new HashMap<>();
		options.put("publicIpId", csIP.id);
		options.put("openfirewall", "false");
		try {
			Document reply = client.createLoadBalancerRule(algorithm, name, Integer.toString(privatePort),
					Integer.toString(publicPort), options);
			registerListenerAsyncJobResult(listener, reply);
			resourceId = getResourceId(reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resourceId;
	}

	private void disableAutoScaleGroupForId(String autoScaleId, ResultListener listener) {
		try {
			Document reply = client.disableAutoScaleVMGroup(autoScaleId);
			registerListenerAsyncJobResult(listener, reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void enableAutoScaleGroupForId(String autoScaleId, ResultListener listener) {
		try {
			Document reply = client.enableAutoScaleVMGroup(autoScaleId);
			registerListenerAsyncJobResult(listener, reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String setPolicyDurationForId(String policyId, int durationInSeconds, ResultListener listener) {
		String jobId = "";
		HashMap<String, String> options = new HashMap<>();
		options.put("duration", Integer.toString(durationInSeconds));
		try {
			Document reply = client.updateAutoScalePolicy(policyId, options);
			jobId = registerListenerAsyncJobResult(listener, reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jobId;
	}

	private void setConditionThresholdForId(String policyId, int threshold, ResultListener listener) {
		CSPolicy policy = getPolicyById(policyId);
		System.out.println("Policy id:" + policy.id);
		CSCondtion condition = getConditionById(policy.conditionIds);
		System.out.println("condition id:" + condition.id);
		CSCondtion newCondition = createConditionUseCounterId(condition.counterId, condition.relationalOperator,
				threshold);
		System.out.println("newcondition id:" + newCondition.id);
		HashMap<String, String> options = new HashMap<>();
		options.put("conditionids", newCondition.id);
		result = null;
		try {
			Document reply = client.updateAutoScalePolicy(policyId, options);
			registerListenerAsyncJobResult(this, reply);
			boolean success = waitForAsyncResult();
			System.out.println("update success..." + success);
			if (success) {
				// delete the old condition now
				deleteCondition(condition.id, listener);
			} else {
				// return result of failed update
				registerListenerAsyncJobResult(listener, reply);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String registerListenerAsyncJobResult(ResultListener listener, Document reply)
			throws XPathExpressionException {
		String jobId;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression xp_property = xpath.compile("/*/jobid/text()");
		jobId = (String) xp_property.evaluate(reply, XPathConstants.STRING);

		// poll result with Result poller and notify listener, when finished
		ResultPoller poller = new ResultPoller(client);
		poller.start(jobId, listener);
		return jobId;
	}

	private String getResourceId(Document reply) throws XPathExpressionException {
		String jobId;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression xp_property = xpath.compile("/*/id/text()");
		jobId = (String) xp_property.evaluate(reply, XPathConstants.STRING);
		return jobId;
	}

	private String getScalePolicyForGroupId(String groupId, ScaleDirection direction) {
		String policyId = "";
		HashMap<String, String> options = new HashMap<>();
		options.put("id", groupId);
		try {
			Document reply = client.listAutoScaleVMGroups(options);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression xp_property = xpath.compile(
					"/listautoscalevmgroupsresponse/autoscalevmgroup/scale" + direction + "policies/id/text()");
			policyId = (String) xp_property.evaluate(reply, XPathConstants.STRING);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return policyId;
	}

	private CSAutoScaleVMGroup getAutoScaleGroupForLBId(String lbId) {
		HashMap<String, String> options = new HashMap<>();
		options.put("lbruleid", lbId);
		return getAutoScaleVMGroupByOptions(options);
	}

	private CSAutoScaleVMGroup getAutoScaleGroupById(String id) {
		HashMap<String, String> options = new HashMap<>();
		options.put("id", id);
		return getAutoScaleVMGroupByOptions(options);
	}

	private CSAutoScaleVMGroup getAutoScaleVMGroupByOptions(HashMap<String, String> options) {
		CSAutoScaleVMGroup group = new CSAutoScaleVMGroup();
		try {
			Document reply = client.listAutoScaleVMGroups(options);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//autoscalevmgroup");

			NodeList item_list = (NodeList) expr.evaluate(reply, XPathConstants.NODESET);
			if (item_list.getLength() == 1) {
				Node item = item_list.item(0);
				group.id = getPropertyValue(xpath, item, "id");
				group.lbRuleId = getPropertyValue(xpath, item, "lbruleid");
				// TODO: Unknown why lbruleid is empty always
				// System.out.println("Rule: " + group.lbRuleId );
				group.minMembers = Integer.parseInt(getPropertyValue(xpath, item, "minmembers"));
				group.maxMembers = Integer.parseInt(getPropertyValue(xpath, item, "maxmembers"));
				group.vmProfileId = getPropertyValue(xpath, item, "vmprofileid");
				group.interval = Integer.parseInt(getPropertyValue(xpath, item, "interval"));
				group.scaleUpPolicies = getPropertyValue(xpath, item, "scaleuppolicies/id");
				group.scaleDownPolicies = getPropertyValue(xpath, item, "scaledownpolicies/id");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return group;
	}

	private CSLB getLBForIPId(String ipId) {
		HashMap<String, String> options = new HashMap<>();
		options.put("publicipid", ipId);
		return getLBbyOptions(options);
	}

	private CSLB getLBbyId(String id) {
		HashMap<String, String> options = new HashMap<>();
		options.put("id", id);
		return getLBbyOptions(options);
	}

	private CSTemplate getTemplateByOptions(HashMap<String, String> options) {
		CSTemplate csTemplate = new CSTemplate();
		try {
			Document reply = client.listTemplates("self", options);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//template");

			NodeList item_list = (NodeList) expr.evaluate(reply, XPathConstants.NODESET);
			if (item_list.getLength() == 1) {
				Node item = item_list.item(0);
				csTemplate.id = getPropertyValue(xpath, item, "id");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return csTemplate;
	}

	private CSCondtion getConditionById(String id) {
		HashMap<String, String> options = new HashMap<>();
		options.put("id", id);
		CSCondtion csCondition = new CSCondtion();
		try {
			Document reply = client.listConditions(options);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//condition");

			NodeList item_list = (NodeList) expr.evaluate(reply, XPathConstants.NODESET);
			if (item_list.getLength() == 1) {
				Node item = item_list.item(0);
				csCondition.id = getPropertyValue(xpath, item, "id");
				csCondition.relationalOperator = getPropertyValue(xpath, item, "relationaloperator");
				csCondition.threshold = Integer.parseInt(getPropertyValue(xpath, item, "threshold"));
				XPathExpression xp_property = xpath.compile("//condition/counter/id/text()");
				csCondition.counterId = (String) xp_property.evaluate(reply, XPathConstants.STRING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return csCondition;
	}

	private String getLBHealthCheckPolicyByLBId(String lbId) {
		HashMap<String, String> options = new HashMap<String, String>();
		String counterId = "";
		try {
			Document reply = client.listLBHealthCheckPolicies(lbId, options);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//healthcheckpolicy");

			NodeList item_list = (NodeList) expr.evaluate(reply, XPathConstants.NODESET);
			if (item_list.getLength() == 1) {
				Node item = item_list.item(0);
				counterId = getPropertyValue(xpath, item, "id");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return counterId;
	}

	private String getCounterByOptions(HashMap<String, String> options) {
		String counterId = "";
		try {
			Document reply = client.listCounters(options);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//counter");

			NodeList item_list = (NodeList) expr.evaluate(reply, XPathConstants.NODESET);
			if (item_list.getLength() == 1) {
				Node item = item_list.item(0);
				counterId = getPropertyValue(xpath, item, "id");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return counterId;
	}

	private CSLB getLBbyOptions(HashMap<String, String> options) {
		CSLB csLB = new CSLB();
		try {
			Document reply = client.listLoadBalancerRules(options);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//loadbalancerrule");

			NodeList item_list = (NodeList) expr.evaluate(reply, XPathConstants.NODESET);
			if (item_list.getLength() == 1) {
				Node item = item_list.item(0);
				csLB.id = getPropertyValue(xpath, item, "id");
				csLB.publicIpId = getPropertyValue(xpath, item, "publicipid");
				csLB.zoneId = getPropertyValue(xpath, item, "zoneid");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return csLB;
	}

	private CSIp getIPbyIP(String ip) {
		HashMap<String, String> options = new HashMap<>();
		options.put("ipaddress", ip);
		return getIPbyOptions(options);
	}

	private CSServiceOffering getServiceOfferingbyOptions(HashMap<String, String> options) {
		CSServiceOffering csOffering = new CSServiceOffering();
		try {
			Document reply = client.listServiceOfferings(options);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//serviceoffering");

			NodeList item_list = (NodeList) expr.evaluate(reply, XPathConstants.NODESET);
			if (item_list.getLength() == 1) {
				Node item = item_list.item(0);
				csOffering.id = getPropertyValue(xpath, item, "id");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return csOffering;
	}

	private CSIp getIPbyOptions(HashMap<String, String> options) {
		CSIp csIP = new CSIp();
		try {
			Document reply = client.listPublicIpAddresses(options);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//publicipaddress");

			NodeList item_list = (NodeList) expr.evaluate(reply, XPathConstants.NODESET);
			if (item_list.getLength() == 1) {
				Node item = item_list.item(0);
				csIP.id = getPropertyValue(xpath, item, "id");
				csIP.zoneId = getPropertyValue(xpath, item, "zoneid");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return csIP;
	}

	private static String getPropertyValue(XPath xpath, Node item, String property_name)
			throws XPathExpressionException {
		XPathExpression xp_property = xpath.compile(property_name + "/text()");
		String property_value = (String) xp_property.evaluate(item, XPathConstants.STRING);
		return property_value;
	}

	private static List<ResourceAllocation> getResourceAllocationFromReply(Document reply, Date start, Date end,
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
		} catch (XPathExpressionException | ParseException e) {
			e.printStackTrace();
		}
		return allocations;
	}

	public static void main(String[] args) {
		File cloudStackPropsFile 	= new File(FileUtility.FILE_LOCATION, "propertyFiles/cloudstack.prop");
		CloudstackControllerImpl cs = new CloudstackControllerImpl(cloudStackPropsFile);
		DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		Date startDate = null;
		Date endDate = null;
		try {
			startDate = format.parse("31.05.2016 00:00");
			endDate = format.parse("31.05.2016 19:30");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cs.getResourceAllocations(startDate, endDate, "");
		
	}
}
