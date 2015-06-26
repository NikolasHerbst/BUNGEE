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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ResponseUtils {
	
	private static List<Long> getRelativeSubmitTimes(
			List<? extends AbstractResponse> responses) {
		
		List<Long> relativeResponseTimestamps = new LinkedList<Long>();
		if (responses.size() > 0) {
			for (AbstractResponse response : responses)
			{
				relativeResponseTimestamps.add(response.getRequestSubmitTime() - response.getExperimentStart());
			}
		}
		return relativeResponseTimestamps;
	}
	
	public static List<Long> getSubmitDiffs(
			List<? extends AbstractResponse> responses, List<Long> relativeTimestamps) {
		List<Long> relativeResponseTimestamps = getRelativeSubmitTimes(
				responses);
		List<Long> timestampDiff = new LinkedList<Long>();
		Iterator<Long> timestamp = relativeTimestamps.iterator();
		Iterator<Long> responseTimestamp = relativeResponseTimestamps.iterator();
		while (timestamp.hasNext() && responseTimestamp.hasNext())
		{
			long diff = responseTimestamp.next() - timestamp.next();
			timestampDiff.add(diff);
		}
		return timestampDiff;
	}
	
	public static <T> List<T> cutWarmUpRequests(
			int numberOfWarmUpRequests, List<T> responses) {
		return responses.subList(Math.min(numberOfWarmUpRequests,responses.size()), responses.size());
	}
}
