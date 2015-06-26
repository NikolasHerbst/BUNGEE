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
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

public class WebServer implements Container {
	
	private static final int PORT = 8080;
	private static final int THREAD_POOL_SIZE = 1000;
	private static String ip = "0.0.0.0";
	private final Executor executor;

	public static class RequestTask implements Runnable {
		private final Response response;
		private final Request request;
		private LoadProcessor loadProcessor;
		public RequestTask(Request request, Response response) {
			this.response = response;
			this.request = request;
			this.loadProcessor = new CpuLoadProcessor();
		}

		/**
		 * Handles the request
		 */
		public void run() {
			try {
				long startTime = System.currentTimeMillis();
				long result = loadProcessor.process(request.getQuery());
				long endTime = System.currentTimeMillis();

				writeResponse(startTime, result, endTime);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Writes the response for a request
		 * @param startTime time when the processing of the request started
		 * @param result result of the request
		 * @param endTime time when the processing of the request ended
		 */
		private void writeResponse(long startTime, long result, long endTime) {
			writeResponseHeader(startTime);
			writeResponseBody(startTime, result, endTime);
		}

		/**
		 * Writes the header for the response
		 * @param startTime time when the processing of the request started
		 */
		private void writeResponseHeader(long startTime) {
			response.setValue("Content-Type", "text/plain");
			response.setValue("Server", "ElasticityBenchmark Server Instance (Simple 4.0)");
			response.setDate("Date", startTime);
			response.setDate("Last-Modified", startTime);
		}

		/**
		 * Writes the body of the response
		 * @param startTime time when the processing of the request started
		 * @param result result of the request
		 * @param endTime time when the processing of the request ended
		 */
		private void writeResponseBody(long startTime, long result, long endTime) {
			String clientIP = request.getClientAddress().getHostName();
			try {
				PrintStream body = response.getPrintStream();
				for (Entry<String,String> entry : request.getQuery().entrySet()) {
					body.println(entry.getKey() + " = " + entry.getValue());
				}
				body.println("start = " + startTime);
				body.println("end = " + endTime);
				body.println("duration = " + (endTime - startTime));
				body.println("result = " + result);
				body.println("ip = " + ip);
				body.println("client = " + clientIP);
				body.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
	} 

	/**
	 * Constructs a new BenchmarkServer
	 * @param size	size of the used thread pool
	 * @param port  port at which server listens for requests
	 */
	public WebServer(int size, int port) {
		try {
			InetAddress host = InetAddress.getLocalHost();
			ip = host.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.executor = Executors.newFixedThreadPool(size);
	}

	/**
	 * Starts the server
	 * @throws IOException
	 */
	public void startServer() throws IOException {
		Server server = new ContainerServer(this);
		@SuppressWarnings("resource")
		Connection connection = new SocketConnection(server);
		SocketAddress address = new InetSocketAddress(PORT);
		connection.connect(address);
	}

	public void handle(Request request, Response response) {
		RequestTask task = new RequestTask(request, response);
		executor.execute(task);
	}

	public static void main(String[] list) throws IOException {
		WebServer server = new WebServer(THREAD_POOL_SIZE, PORT);
		server.startServer();
	}
}