package net.datapipe.CloudStack;

import java.util.HashMap;

import org.w3c.dom.Document;

public class listZones {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document iso_list = client.listZones(options);

    String elements[] = {"id", "name", "zonetoken"};

    CLI.printDocument(iso_list, "//zone", elements);
  }
}
