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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class JMeterResponse extends AbstractResponse {
	private long    latency;
	private long    requestProcStart;
	private long    requestProcEnd;
	private long 	requestServiceTime;
	private long    result;
	private String 	label;
	private int     responseCode;
	private String  dataType;
	private String 	serverIP;
	
	public JMeterResponse(long id, long requestSubmitTime,
			long requestReceiveTime, long responseTime, long experimentStart, long latency,
			long requestProcStart, long requestProcEnd,
			long rquestServiceTime, long result , boolean success, int responseCode, String serverIP) {
		super(id, requestSubmitTime, requestReceiveTime, responseTime, success, experimentStart);
		this.latency = latency;
		this.requestProcStart = requestProcStart;
		this.requestProcEnd = requestProcEnd;
		this.requestServiceTime = rquestServiceTime;
		this.result = result;
		this.responseCode = responseCode;
		this.serverIP = serverIP;
	}


	public void setRequestSubmitTime(long requestSubmitTime) {
		this.requestSubmitTime = requestSubmitTime;
	}


	public long getLatency() {
		return latency;
	}

	public long getRequestProcStart() {
		return requestProcStart;
	}
	
	public long getRequestProcEnd() {
		return requestProcEnd;
	}

	public long getRequestServiceTime() {
		return requestServiceTime;
	}

	public long getResult() {
		return result;
	}

	public String getLabel() {
		return label;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getServerIP() {
		return serverIP;
	}
	
	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}
	
	@Override
	public String toString() {
		return " requestSubmitTime="
				+ requestSubmitTime + ", requestReceiveTime="
				+ requestReceiveTime + ", responseTime=" + responseTime
				+ ", latency=" + latency + ", requestProcStart="
				+ requestProcStart + ", requestProcEnd="
				+ requestProcEnd + ", requestServiceTime="
				+ requestServiceTime + ", result=" + result + "]";
	}


	public static boolean readJMeterResponseFile(File csvFile,
			String csvSplitBy, List<JMeterResponse> responses) {
		boolean ok = false;
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(csvFile));
	
			// jump over header line
			line = br.readLine();
	
			while ((line = br.readLine()) != null) {
				String[] responseString = line.split(csvSplitBy);
				long id						= Long.parseLong(responseString[0]);
				long experimentStart		= Long.parseLong(responseString[1]);
				long requestSubmitTime		= Long.parseLong(responseString[2]);
				long requestReceiveTime 	= Long.parseLong(responseString[3]);
				long responseTime		 	= Long.parseLong(responseString[4]);
				long latency				= Long.parseLong(responseString[5]);
				long serverStartWorkTime	= Long.parseLong(responseString[6]);
				long serverEndWorkTime	    = Long.parseLong(responseString[7]);
				long serverWorkDuration     = Long.parseLong(responseString[8]);
				long result 				= Long.parseLong(responseString[9]);
				boolean success				= (Long.parseLong(responseString[10]) == 0);
				int code;
				if (success) {
					code 					= Integer.parseInt(responseString[11]);
				} else {
					code 					= 0;
				}
				// .intern() is very important for saving heap here! without it, the whole responseString
				// is kept in memory, because strings returned by String.split() are only wrappers to original string
				String ip 					= responseString[10].intern();
				JMeterResponse response = new JMeterResponse(id, requestSubmitTime, requestReceiveTime, responseTime, experimentStart, latency, serverStartWorkTime, serverEndWorkTime, serverWorkDuration, result, success, code, ip);
				responses.add(response);
	
			}
			
			Collections.sort(responses);
			
			ok = true;
	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ok;
	}
}
