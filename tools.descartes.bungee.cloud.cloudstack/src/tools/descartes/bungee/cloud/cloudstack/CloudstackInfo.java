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

package tools.descartes.bungee.cloud.cloudstack;

import java.util.Date;
import java.util.List;

import tools.descartes.bungee.allocation.SupplySeries;
import tools.descartes.bungee.cloud.ExtendedCloudInfo;

public class CloudstackInfo implements ExtendedCloudInfo {

	private CloudstackControllerImpl cloudstackImpl;
	/**
	 * @param cloudstackImpl
	 */
	public CloudstackInfo(CloudstackControllerImpl cloudstackImpl) {
		this.cloudstackImpl = cloudstackImpl;
	}

	@Override
	public int getNumberOfResources(String tag) {
		return cloudstackImpl.getNumberOfResources(tag);
	}
	
	@Override
	public int getNumberOfResources() {
		return cloudstackImpl.getNumberOfResources("");
	}

	@Override
	public List<SupplySeries> getResourceAllocations(Date startDate,
			Date endDate, String ip) {
		return cloudstackImpl.getResourceAllocations(startDate, endDate, ip);
	}

	

}
