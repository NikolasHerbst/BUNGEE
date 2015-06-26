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

import java.util.Date;
import java.util.List;

import tools.descartes.bungee.allocation.SupplySeries;

public interface ExtendedCloudInfo extends CloudInfo {
	
	/**
	 * Returns a list of SupplySeries for allocations between start and end date for a given public ip
	 * Every SupplySeries contains allocation events of a single type (scheduled allocation, completed allocation, lb adaption)
	 * The resource amount within the allocations is relative to the resource amount at the startDate
	 * @param startDate beginning of analyzed period of time	
	 * @param endDate end of analyzed period of time
	 * @param ip public ip to which allocated resources are assigned
	 * @return List of SupplySeries, One SupplySeries for every event type
	 */
	public List<SupplySeries> getResourceAllocations(Date startDate, Date endDate, String ip);
}
