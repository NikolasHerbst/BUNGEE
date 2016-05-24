/*******************************************************************************
Copyright 2015 Andreas Weber, Nikolas Herbst
Update 2016 André Bauer

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

package net.datapipe.CloudStack;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;


public class CloudStackAPI extends CloudStack {
	private String apiURL;

	public CloudStackAPI(String apiURL, String new_secret, String new_apikey) {
		super(new_secret, new_apikey);
		this.apiURL = apiURL;
	}

	@Override
	protected HttpMethod makeHttpGet(LinkedList<NameValuePair> queryValues) throws Exception {
		String query_signature = sign_request(queryValues);
		queryValues.add(new NameValuePair("signature",query_signature));

		HttpMethod method = new GetMethod(apiURL);
		method.setFollowRedirects(true);
		method.setQueryString(queryValues.toArray(new NameValuePair[0]));

		return method;
	}
	
	public Document destroyVirtualMachine(String id, boolean expunge) throws Exception {
	    LinkedList<NameValuePair> arguments = newQueryValues("destroyVirtualMachine",null);
	    arguments.add(new NameValuePair("id",id));
	    arguments.add(new NameValuePair("expunge",""+expunge));
	    return Request(arguments);
	  }

	public Document listApis(HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("listApis",optional);
		return Request(arguments);
	}
	
	public Document listAutoScaleVMGroups(HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("listAutoScaleVmGroups",optional);
		return Request(arguments);
	}

	public Document disableAutoScaleVMGroup(String autoScaleVMGroupId) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("disableAutoScaleVmGroup",null);
		arguments.add(new NameValuePair("id",autoScaleVMGroupId));
		return Request(arguments);
	}
	
	public Document updateAutoScaleVMGroup(String autoScaleVMGroupId, HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("updateAutoScaleVmGroup",optional);
		arguments.add(new NameValuePair("id",autoScaleVMGroupId));
		return Request(arguments);
	}

	public Document enableAutoScaleVMGroup(String autoScaleVMGroupId) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("enableAutoScaleVmGroup",null);
		arguments.add(new NameValuePair("id",autoScaleVMGroupId));
		return Request(arguments);
	}

	public Document listAutoScalePolicies(HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("listAutoScalePolicies",optional);
		return Request(arguments);
	}
	
	public Document listLBHealthCheckPolicies(String lbId, HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("listLBHealthCheckPolicies",optional);
		arguments.add(new NameValuePair("lbruleid",lbId));
		return Request(arguments);
	}
	
	public Document listAutoScaleVmProfiles(HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("listAutoScaleVmProfiles",optional);
		return Request(arguments);
	}
	
	public Document listCounters(HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("listCounters",optional);
		return Request(arguments);
	}
	
	public Document listConditions(HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("listConditions",optional);
		return Request(arguments);
	}

	public Document updateAutoScalePolicy(String id, HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("updateAutoScalePolicy",optional);
		arguments.add(new NameValuePair("id",id));
		return Request(arguments);
	}
	
	public Document createAutoScaleVmProfile(String serviceOfferingId, String templateId, String zoneId, HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("createAutoScaleVmProfile", optional);
		arguments.add(new NameValuePair("serviceofferingid",serviceOfferingId));
		arguments.add(new NameValuePair("templateid",templateId));
		arguments.add(new NameValuePair("zoneid",zoneId));
		return Request(arguments);
	}
	
	public Document createLBHealthCheckPolicy(String lbruleid, HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("createLBHealthCheckPolicy", optional);
		arguments.add(new NameValuePair("lbruleid",lbruleid));
		return Request(arguments);
	}
	
	public Document deleteLBHealthCheckPolicy(String id) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("deleteLBHealthCheckPolicy",null);
		arguments.add(new NameValuePair("id",id));
		return Request(arguments);
	}
	
	public Document deleteAutoScaleVmProfile(String id) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("deleteAutoScaleVmProfile",null);
		arguments.add(new NameValuePair("id",id));
		return Request(arguments);
	}
	
	public Document createAutoScaleVmGroup(String lbRuleId, int minMembers, int maxMembers, String scaleDownPolicyIds, String scaleUpPolicyIds, String vmProfileId, HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("createAutoScaleVmGroup", optional);
		arguments.add(new NameValuePair("lbruleid",lbRuleId));
		arguments.add(new NameValuePair("maxmembers",Integer.toString(maxMembers)));
		arguments.add(new NameValuePair("minmembers",Integer.toString(minMembers)));
		arguments.add(new NameValuePair("scaledownpolicyids",scaleDownPolicyIds));
		arguments.add(new NameValuePair("scaleuppolicyids",scaleUpPolicyIds));
		arguments.add(new NameValuePair("vmprofileid",vmProfileId));
		return Request(arguments);
	}
	
	public Document deleteAutoScaleVmGroup(String id) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("deleteAutoScaleVmGroup",null);
		arguments.add(new NameValuePair("id",id));
		return Request(arguments);
	}
	
	public Document createAutoScalePolicy(String action, String conditionIds, int duration, HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("createAutoScalePolicy", optional);
		arguments.add(new NameValuePair("action", action));
		arguments.add(new NameValuePair("conditionids", conditionIds));
		arguments.add(new NameValuePair("duration", Integer.toString(duration)));
		return Request(arguments);
	}
	
	public Document deleteAutoScalePolicy(String id) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("deleteAutoScalePolicy",null);
		arguments.add(new NameValuePair("id",id));
		return Request(arguments);
	}
	
	public Document createCondition(String counterId, String relationalOperator, int threshold, HashMap<String,String> optional) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("createCondition", optional);
		arguments.add(new NameValuePair("counterid", counterId));
		arguments.add(new NameValuePair("relationaloperator", relationalOperator));
		arguments.add(new NameValuePair("threshold", Integer.toString(threshold)));
		return Request(arguments);
	}
	
	public Document deleteCondition(String id) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("deleteCondition",null);
		arguments.add(new NameValuePair("id",id));
		return Request(arguments);
	}

	public Document startVirtualMachine(String id, String hostid) throws Exception {
		LinkedList<NameValuePair> arguments = newQueryValues("startVirtualMachine",null);
	    arguments.add(new NameValuePair("id",id));
	    arguments.add(new NameValuePair("hostid",hostid));
	    return Request(arguments);
	}

}
