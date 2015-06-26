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

package tools.descartes.bungee.cloud;

public interface CloudInfo {
	/**
	 * Returns the current number of resources (vm instances) which are assigned to a shared public ip.
	 * The ip address is typically the ip address of the load balancer
	 * @param ip public ip address of the load balancer which forwards requests to the vm instances
	 * @return number of resources assigned to the public ip
	 */
	public int getNumberOfResources(String ip);
}
