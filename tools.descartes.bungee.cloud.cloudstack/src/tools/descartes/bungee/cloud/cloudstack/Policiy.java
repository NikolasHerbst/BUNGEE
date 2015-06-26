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

public class Policiy {
	private String counter;
	private String operator;
	private int threshold;
	private int duration;
	private int quiettime;
	static final String QUIETTIME = "quiettime";
	static final String THRESHOLD = "threshold";
	static final String DURATION = "duration";
	static final String OPERATOR = "operator";
	static final String COUNTER = "counter";
	
	public Policiy(String counter, String operator, int threshold, int duration, int quiettime) {
		super();
		this.counter = counter;
		this.operator = operator;
		this.threshold = threshold;
		this.duration = duration;
		this.quiettime = quiettime;
	}

	public String getCounter() {
		return counter;
	}

	public void setCounter(String counter) {
		this.counter = counter;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	int getQuiettime() {
		return quiettime;
	}

	void setQuiettime(int quiettime) {
		this.quiettime = quiettime;
	}
	
}
