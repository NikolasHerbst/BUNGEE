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

import java.util.List;

public class SanityChecker {

	// Account for imperfect syncing: clocks may be out of sync for ALLOWED_CLOCK_DELAY_MILLIS milliseconds
	private static final long ALLOWED_CLOCK_DELAY_MILLIS=1000;

	public static boolean sanityCheck(List<JMeterResponse> responses) {
		boolean ok = true;
		for (JMeterResponse response : responses)
		{
			boolean success = response.isSuccess();
			boolean responseTimeCorrect = (response.getRequestReceiveTime() - response.getRequestSubmitTime() == response.getResponseTime());
			boolean latencySmallerResponseTime = response.getLatency() <= response.getResponseTime();
			boolean workTimeCorrect = !success || (response.getRequestProcEnd() - response.getRequestProcStart() == response.getRequestServiceTime());
			boolean submitSmallerWorkStart = !success || response.getRequestSubmitTime() <= response.getRequestProcStart() + ALLOWED_CLOCK_DELAY_MILLIS;
			boolean receiveBiggerWorkEnd = !success || response.getRequestReceiveTime()	>= response.getRequestProcEnd() - ALLOWED_CLOCK_DELAY_MILLIS;
			
			ok = ok && responseTimeCorrect && workTimeCorrect && latencySmallerResponseTime && submitSmallerWorkStart && receiveBiggerWorkEnd;

			if (!responseTimeCorrect) {
				System.out.println("Response Time not valid for: ");
				System.out.println(response);
			}
			if (!workTimeCorrect) {
				System.out.println("Work Time not valid for: ");
				System.out.println(response);
			}
			if (!latencySmallerResponseTime) {
				System.out.println("Latency is not smaller than response time for: ");
				System.out.println(response);
			}
			if (!submitSmallerWorkStart) {
				System.out.println("Submit time is not smaller than start work time for: ");
				System.out.println(response);
				System.out.println("Diff: " + (response.getRequestSubmitTime() - response.getRequestProcStart()));
			}
			if (!receiveBiggerWorkEnd) {
				System.out.println("Receive time is not bigger than work end time for: ");
				System.out.println(response);
				System.out.println("Diff: " + (response.getRequestProcEnd() - response.getRequestReceiveTime()));
			}

		}
		return ok;
	}
}
