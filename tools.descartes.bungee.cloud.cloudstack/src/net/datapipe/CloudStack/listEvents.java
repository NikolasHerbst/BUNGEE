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

import net.datapipe.CloudStack.CLI;
import net.datapipe.CloudStack.CloudStack;

import org.w3c.dom.Document;

public class listEvents {
  public static void main(String[] args) throws Exception {
    String secret = "Bq3MyuwiR5f3XMnxw7bjqATMz8xDRZ5-1qDMHG1Ca-SccftJ9lsdJlir4ag0UfTBtY1-OCRtPflBUiJZIym8rQ"; //System.getenv("ESTRAT_SECRET");
	String apikey = "yLqaWu0DyNXEtN_QD5ixyR1aonR-t6fP20zk0POqvjMmwzniMywH2dBx2_yOB9qKnGNCe5gS-jY94pmSHRwApQ"; //System.getenv("ESTRAT_APIKEY");
	String apiURL = "http://141.21.72.8:8080/client/api";
	
	CloudStack  client = new CloudStackAPI(apiURL, secret, apikey);
    HashMap<String,String> options = CLI.args_to_options(args);
    options.put("type","LB.ASSIGN.TO.RULE");
    
    Document iso_list = client.listEvents(options);

    String elements[] = {"id", "account", "created", "state", "type", "description"};

    CLI.printDocument(iso_list, "//event", elements);
  }
}
