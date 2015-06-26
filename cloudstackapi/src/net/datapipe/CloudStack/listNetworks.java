package net.datapipe.CloudStack;

import java.util.HashMap;

import org.w3c.dom.Document;

public class listNetworks {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document network_list = client.listNetworks(options);

    String elements[] = {"id", "name", "zoneid", "displaytext"};

    CLI.printDocument(network_list, "//network", elements);
  }
}
