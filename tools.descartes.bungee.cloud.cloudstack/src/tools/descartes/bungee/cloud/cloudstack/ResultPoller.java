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

import java.util.Timer;
import java.util.TimerTask;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.datapipe.CloudStack.CloudStackAPI;

import org.w3c.dom.Document;

public class ResultPoller {

	private static final int POLLING_INTERVAL_MS = 1000;

	class Task extends TimerTask
	{
		@Override public void run()
		{
			JobResultImplementation result = getJobResult();
			if (result.getStatus() != JobResultImplementation.JobStatus.IN_PROGRESS)
			{
				listener.jobFinished(result);
				timer.cancel();
			}
			
		}
	}


	private String jobId;
	private CloudStackAPI client;
	private ResultListener listener;
	private Timer timer;

	public ResultPoller(CloudStackAPI client) {
		super();
		
		this.client = client;
	}

	public void start(String jobId, ResultListener listener)
	{
		this.jobId = jobId;
		this.listener = listener;
		timer = new Timer();
		timer.schedule( new Task(), 0, POLLING_INTERVAL_MS);

	}
	
	private JobResultImplementation getJobResult() {
		JobResultImplementation result = new JobResultImplementation(jobId);
		try {
			Document job_result = client.queryAsyncJobResult(jobId);
			
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression jobstatus_xp = xpath.compile("/queryasyncjobresultresponse/jobstatus/text()");
			String jobstatus = (String)jobstatus_xp.evaluate(job_result, XPathConstants.STRING);


			if(jobstatus.equals("1")) {
				result.setStatus(JobResultImplementation.JobStatus.COMPLETED_SUCCESS);
			} else if(jobstatus.equals("0")) {
				System.out.println("still running");
				result.setStatus(JobResultImplementation.JobStatus.IN_PROGRESS);
			} else if(jobstatus.equals("2")) {
				XPathExpression errorcode_xp = xpath.compile("/queryasyncjobresultresponse/jobresult/errorcode/text()");
				XPathExpression errortext_xp = xpath.compile("/queryasyncjobresultresponse/jobresult/errortext/text()");
				String errorcode = (String)errorcode_xp.evaluate(job_result, XPathConstants.STRING);
				String errortext = (String)errortext_xp.evaluate(job_result, XPathConstants.STRING);
				result.setStatus(JobResultImplementation.JobStatus.COMPLETED_FAILED);
				if(errorcode.length() > 0 || errortext.length() > 0) {
					result.setText("Error " + errorcode + ": " + errortext);
				}
			} else {
				result.setStatus(JobResultImplementation.JobStatus.UNKNOWN);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
