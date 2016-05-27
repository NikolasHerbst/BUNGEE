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

package tools.descartes.bungee.cloud;

public interface CloudInfo {
	/**
	 * Returns the current number of resources (vm instances) which are assigned to a shared public ip.
	 * The ip address is typically the ip address of the load balancer
	 * @param Tag of the VMs
	 * @return number of resources assigned to the public ip
	 */
	public int getNumberOfResources(String tag);
	
	/**
	 * Returns the current number of resources (vm instances) which are assigned to a shared public ip.
	 * The ip address is typically the ip address of the load balancer
	 * @return number of resources assigned to the public ip
	 */
	public int getNumberOfResources();
}
