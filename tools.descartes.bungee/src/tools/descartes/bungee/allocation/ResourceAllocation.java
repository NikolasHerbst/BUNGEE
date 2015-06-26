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

package tools.descartes.bungee.allocation;

import java.util.Date;

/**
 * Represents the amount of resources required/allocated/.. at a given point in time.
 *
 */
public class ResourceAllocation implements Cloneable {
	private Date date;
	private int currentAmount;
	
	public ResourceAllocation(Date date, int currentAmount) {
		super();
		this.date = date;
		this.currentAmount = currentAmount;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getCurrentAmount() {
		return currentAmount;
	}
	
	public void setCurrentAmount(int amount) {
		currentAmount = amount;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
	        return true;
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	    final ResourceAllocation other = (ResourceAllocation) obj;
	    return (date.equals(other.date) && currentAmount == other.currentAmount);
	}

	@Override
	public String toString() {
		return "ResourceAllocation [date=" + date.getTime() + ", currentAmount="
				+ currentAmount + "]";
	}
	
	@Override
	public ResourceAllocation clone() {
		ResourceAllocation clone = new ResourceAllocation(new Date(date.getTime()), currentAmount);
        return clone;
    }
}
