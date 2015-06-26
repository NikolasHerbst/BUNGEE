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

public class JobResultImplementation implements JobResult {
	
	enum JobStatus {
		IN_PROGRESS,
		COMPLETED_SUCCESS,
		COMPLETED_FAILED,
		UNKNOWN
	}
	

	private JobStatus status = JobStatus.COMPLETED_FAILED;
	private String jobId;
	private String text;
	

	protected JobResultImplementation(String jobId) {
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}
	
	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public boolean isSuccessful() {
		return (status == JobStatus.COMPLETED_SUCCESS) ;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
