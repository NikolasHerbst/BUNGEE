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
package tools.descartes.bungee.server;

import java.util.Random;

import org.simpleframework.http.Query;

public class CpuLoadProcessor implements LoadProcessor {
	
	private Random rd;
	
	public CpuLoadProcessor() {
		this.rd = new Random();
	}
	
	
	@Override
	public long process(Query query) {
		int problemSize = 10000000;
		int size = query.getInteger("size");  
		if (size != 0)
		{	
			problemSize = size;
		}

		long fibonacci = fibonacci(problemSize);
		return fibonacci;
	}

	/**
	 * Calculates the nth element of the fibonacci series (some randomization is added to the series)
	 * @param n 
	 * @return nth element of the fibonacci series
	 */
	private long fibonacci(int n) {
		long a=rd.nextInt(100);
		long b=rd.nextInt(100);
		long c=a+b;
		for(int i=2; i<n; i++){
			a=b;
			b=c;
			c=a+b+rd.nextInt(100);
		}
		return c;
	}

}
