package net.datapipe.CloudStack;

import org.w3c.dom.Document;

import java.util.HashMap;

public class listNetworkOfferings {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document network_list_doc = client.listNetworkOfferings(options);

    String elements[] = {"id", "availability", "name", "traffictype", "displaytext"};

    CLI.printDocument(network_list_doc, "//networkoffering", elements);
  }
}
