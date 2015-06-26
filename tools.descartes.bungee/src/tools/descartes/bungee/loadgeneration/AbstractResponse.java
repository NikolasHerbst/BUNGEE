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

package tools.descartes.bungee.loadgeneration;

public abstract class AbstractResponse implements Comparable<AbstractResponse> {

	protected long id;
	protected long requestSubmitTime;
	protected long requestReceiveTime;
	protected long responseTime;
	protected long experimentStart;
	protected boolean success;
	
	public AbstractResponse(long id, long requestSubmitTime, long requestReceiveTime,
			long responseTime, boolean success, long experimentStart) {
		this.id = id;
		this.requestSubmitTime = requestSubmitTime;
		this.requestReceiveTime = requestReceiveTime;
		this.responseTime = responseTime;
		this.success = success;
		this.experimentStart = experimentStart;
	}

	public long getId() {
		return id;
	}

	public long getRequestSubmitTime() {
		return requestSubmitTime;
	}

	public long getRequestReceiveTime() {
		return requestReceiveTime;
	}

	public long getResponseTime() {
		return responseTime;
	}
	
	public int compareTo(AbstractResponse o) {
	    return (int) (this.id - o.id);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public long getExperimentStart() {
		return experimentStart;
	}

	public void setExpermientStart(long experimentStart) {
		this.experimentStart = experimentStart;
	}
}