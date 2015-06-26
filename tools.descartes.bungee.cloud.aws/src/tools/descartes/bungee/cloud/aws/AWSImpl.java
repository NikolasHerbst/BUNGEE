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

package tools.descartes.bungee.cloud.aws;

import java.util.LinkedList;
import java.util.List;

import tools.descartes.bungee.cloud.Bounds;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthResult;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.InstanceState;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;

class AWSImpl {
	private static final String EC2_END_POINT_NAME = "https://ec2.eu-west-1.amazonaws.com";
	private static final String LB_END_POINT_NAME = "elasticloadbalancing.eu-west-1.amazonaws.com";
	private static final String AUTOSCALING_END_POINT_NAME = "autoscaling.eu-west-1.amazonaws.com";


	private AmazonEC2Client ec2;
	private AmazonElasticLoadBalancingClient elasticLB;
	private AmazonAutoScalingClient autoScale;

	private ResourcesInfo lastResourceInfo = new ResourcesInfo();
	
	public AWSImpl() {
		init();
	}
	
	ResourcesInfo getResourceInfo(String hostName) {
		ResourcesInfo resources = new ResourcesInfo();
		boolean ok = false;
		try {
			LoadBalancerDescription lb = getLoadBalancerForHostName(hostName);
			if (lb != null) {
				List<Instance> instances = lb.getInstances();
				Filter filterRunning = new Filter().withName("instance-state-name").withValues("running");
				List<String> instanceIds = new LinkedList<>();
				for (Instance instance : instances)  {
					instanceIds.add(instance.getInstanceId());
				}
				resources.total = lb.getInstances().size();

				DescribeInstanceStatusResult describeInstanceStatus = ec2.describeInstanceStatus(new DescribeInstanceStatusRequest().withInstanceIds(instanceIds).withFilters(filterRunning));
				resources.running = describeInstanceStatus.getInstanceStatuses().size();

				DescribeInstanceHealthRequest request = new DescribeInstanceHealthRequest().withLoadBalancerName(lb.getLoadBalancerName());
				DescribeInstanceHealthResult describeInstanceHealth = elasticLB.describeInstanceHealth(request);
				List<InstanceState> instanceStates = describeInstanceHealth.getInstanceStates();
				for (InstanceState instState : instanceStates)
				{
					if (instState.getState().equals("InService")) {
						resources.inService++;
					};
				}
				ok = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!ok) {
			resources = lastResourceInfo;
		} else {
			lastResourceInfo = resources;
		}
		//System.out.println("size: " + resources.total  + " running: " + resources.running + " inService: " + resources.inService);
		return resources;
	}
	
	private void init() {
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
							"Please make sure that your credentials file is at the correct " +
							"location (C:\\Users\\<Username>\\.aws\\credentials), and is in valid format.",
							e);
		}
		elasticLB = new AmazonElasticLoadBalancingClient(credentials);
		elasticLB.setEndpoint(LB_END_POINT_NAME);
		ec2 = new AmazonEC2Client(credentials);
		ec2.setEndpoint(EC2_END_POINT_NAME);
		autoScale = new AmazonAutoScalingClient(credentials);
		autoScale.setEndpoint(AUTOSCALING_END_POINT_NAME);
	}
	
	LoadBalancerDescription getLoadBalancerForHostName(String hostName) {
		LoadBalancerDescription balancer = null;
		DescribeLoadBalancersResult describeLoadBalancers = elasticLB.describeLoadBalancers();
		List<LoadBalancerDescription> loadBalancerDescriptions = describeLoadBalancers.getLoadBalancerDescriptions();
		for (LoadBalancerDescription loadBalancerDescription: loadBalancerDescriptions) {
			if (loadBalancerDescription.getDNSName().equalsIgnoreCase(hostName)) {
				balancer = loadBalancerDescription;
			}
		}
		return balancer;
	}

	AutoScalingGroup getAutoScaleGroupForHostName(String hostName) {
		AutoScalingGroup autoScaleGroup = null;
		LoadBalancerDescription lb = getLoadBalancerForHostName(hostName);
		if (lb != null) {
			DescribeAutoScalingGroupsResult describeAutoScalingGroups = autoScale.describeAutoScalingGroups();
			for (AutoScalingGroup group : describeAutoScalingGroups.getAutoScalingGroups()) {
				if (group.getLoadBalancerNames().contains(lb.getLoadBalancerName())) {
					autoScaleGroup = group;
				}
			}
		}
		return autoScaleGroup;
	}
	
	public Bounds getScalingBounds(String hostName) {
		Bounds bounds = null;
		AutoScalingGroup group = getAutoScaleGroupForHostName(hostName);
		if (group != null) {
			bounds = new Bounds(group.getMinSize(), group.getMaxSize());
		}
		return bounds;
	}

	public boolean setScalingBounds(String hostName, Bounds bounds) {
		boolean success = false;
		AutoScalingGroup group = getAutoScaleGroupForHostName(hostName);
		if (group != null) {
			UpdateAutoScalingGroupRequest request = new UpdateAutoScalingGroupRequest().withAutoScalingGroupName(group.getAutoScalingGroupName());
			request.setMinSize(bounds.getMin());
			request.setMaxSize(bounds.getMax());
			autoScale.updateAutoScalingGroup(request);
			success = bounds.equals(getScalingBounds(hostName));
		}
		return success;
	}
}
