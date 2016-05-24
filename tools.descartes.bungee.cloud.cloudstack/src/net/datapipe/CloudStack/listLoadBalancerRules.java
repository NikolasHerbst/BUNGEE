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

import org.w3c.dom.Document;

public class listLoadBalancerRules {
  public static void main(String[] args) throws Exception {
	String secret = "nRl7nsrfjLuIgdLt98mYyEn6ZSanUrBwA3-SX0J2SitkpPLwRLvAYMXUPDfNLZE40u0hBtnX-zkXxKmj_7rIXw";
	String apikey = "t8bRnvTEACYz1SsJNOA_O9KKPb77RNitz5ipcNsNdws1ein1sE2SUAdxJHEz69IvednN7_jT0NZehHxGF6gLgA";
	String apiURL = "http://10.1.1.10:8080/client/api";
	
	CloudStackAPI  client = new CloudStackAPI(apiURL, secret, apikey);
    HashMap<String,String> options = CLI.args_to_options(args);
    //options.put("publicipid", "e955321d-0b17-48c1-8ca5-3f8fb9bf868d");
    Document iso_list = client.listLoadBalancerRules(options);

    String elements[] = {"id", "account", "algorithm", "state", "cidrlist"};

    CLI.printDocument(iso_list, "//loadbalancerrule", elements);
  }
}
