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
package net.datapipe.CloudStack;

import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

public class updateAutoScalePolicy {
	public static void main(String[] args) throws Exception {
		String secret = "ezTQdv-TEOLAj8f1u_2XBnHIpA6GMK_jZULHvPCr8KeVpdCsD8ZcOUz6kFkaigpzBUkalLsofTikd_cFfVHyNQ"; //System.getenv("ESTRAT_SECRET");
		String apikey = "ow6-UWbqwiieO-zFgSqbIdB1P8fAXjfWW_gBaKzFEyy9i7siOn9H4GePg7x1jjhHFaYf0yEjcE18DvrKA4wpDw"; //System.getenv("ESTRAT_APIKEY");
		String apiURL = "http://141.21.72.8:8080/client/api";

		String id = "1a9c11cf-94b7-4b6f-9cc6-3dc35907ec56";

		CloudStackAPI  client = new CloudStackAPI(apiURL, secret, apikey);
		HashMap<String,String> options = CLI.args_to_options(args);
		options.put("duration", Integer.toString(5));
		Document iso_list = client.updateAutoScalePolicy(id, options);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression xp_property = xpath.compile("/updateautoscalepolicyresponse/jobid/text()");
		String jobId = (String)xp_property.evaluate(iso_list, XPathConstants.STRING);

		Document job_result = client.queryAsyncJobResult(jobId);

		XPathExpression jobstatus_xp = xpath.compile("/queryasyncjobresultresponse/jobstatus/text()");
		String jobstatus = (String)jobstatus_xp.evaluate(job_result, XPathConstants.STRING);

		XPathExpression jobid_xp = xpath.compile("/queryasyncjobresultresponse/jobid/text()");
		String jobid_response = (String)jobid_xp.evaluate(job_result, XPathConstants.STRING);

		System.out.print("jobid = "+jobid_response);
		if(jobstatus.equals("1")) {
			System.out.print(" completed ok");
		} else if(jobstatus.equals("0")) {
			System.out.print(" still running");
		} else if(jobstatus.equals("2")) {
			System.out.println(" failed");
			XPathExpression errorcode_xp = xpath.compile("/queryasyncjobresultresponse/jobresult/errorcode/text()");
			XPathExpression errortext_xp = xpath.compile("/queryasyncjobresultresponse/jobresult/errortext/text()");
			String errorcode = (String)errorcode_xp.evaluate(job_result, XPathConstants.STRING);
			String errortext = (String)errortext_xp.evaluate(job_result, XPathConstants.STRING);
			if(errorcode.length() > 0 || errortext.length() > 0) {
				System.out.print("errorcode = "+errorcode+"  errortext = "+errortext);
			}
		} else {
			System.out.print(" unknown status = "+jobstatus);
		}

		System.out.println("");

		System.out.println("JobId: " + jobId);
	}
}
