package net.datapipe.CloudStack;

import org.w3c.dom.Document;

import java.util.HashMap;

public class listServiceOfferings {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document services_list_doc = client.listServiceOfferings(options);

    String elements[] = {"id", "name", "cpunumber", "cpuspeed", "memory", "displaytext"};

    CLI.printDocument(services_list_doc, "//serviceoffering", elements);
  }
}
